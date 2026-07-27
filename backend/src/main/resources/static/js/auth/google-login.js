import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.0/firebase-app.js";
import { getAuth, GoogleAuthProvider, signInWithPopup }
    from "https://www.gstatic.com/firebasejs/10.12.0/firebase-auth.js";

const firebaseConfig = {
    apiKey:     "AIzaSyBQZMaY2_1gdl-LynAG6k5TDmLXobd7_xw",
    authDomain: "medai-diagnosis-5efd5.firebaseapp.com",
    projectId:  "medai-diagnosis-5efd5",
    appId:      "1:172414625801:web:c2390692e107c4f27a30d8",
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const provider = new GoogleAuthProvider();
provider.setCustomParameters({
    prompt: 'select_account'
});

// Đọc CSRF token từ <meta> do Thymeleaf render (xem head của login.html)
// Bắt buộc phải có, nếu không request POST sẽ bị Spring Security chặn 403
const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

document.getElementById("googleLoginBtn").addEventListener("click", async () => {
    try {
        const result = await signInWithPopup(auth, provider);
        const idToken = await result.user.getIdToken();

        const res = await fetch("/auth/google/session", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {}),
            },
            body: JSON.stringify({ idToken }),
        });
        const data = await res.json();

        if (data.status === "OK") {
            window.location.href = data.redirect;
        } else if (data.status === "NEED_MORE_INFO") {
            // Lưu tạm idToken + email để dùng ở form bổ sung thông tin
            sessionStorage.setItem("pendingIdToken", idToken);
            sessionStorage.setItem("pendingEmail", data.email);
            sessionStorage.setItem("pendingFullName", data.fullName);
            window.location.href = "/auth/google-complete";
        } else if (data.status === "BANNED") {
            window.location.href = "/auth/login?error=banned";
        } else if (data.status === "LOCKED") {
            window.location.href = "/auth/login?error=locked";
        }  else {
            window.location.href = "/auth/login?error=invalid";
        }
    } catch (err) {
        console.error(">>> LỖI THỰC SỰ TỪ FIREBASE:", err); // In lỗi ra Console
        if (err.code === "auth/popup-closed-by-user") return;
        window.location.href = "/auth/login?error=invalid";
    }
});