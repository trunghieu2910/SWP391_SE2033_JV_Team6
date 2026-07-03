// Đọc thông tin tạm được lưu ở bước 1 (google-login.js)
const idToken  = sessionStorage.getItem("pendingIdToken");
const email    = sessionStorage.getItem("pendingEmail");
const fullName = sessionStorage.getItem("pendingFullName");

// Nếu vào thẳng trang này mà không qua bước đăng nhập Google → đá về /login
if (!idToken) {
    window.location.href = "/login";
}

const welcomeText = document.getElementById("welcomeText");
if (welcomeText && fullName) {
    welcomeText.textContent = `Chào ${fullName} (${email})! Vui lòng cung cấp thêm thông tin để hoàn tất đăng ký.`;
}

const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

const form       = document.getElementById("completeForm");
const submitBtn  = document.getElementById("submitBtn");
const errorBox   = document.getElementById("errorBox");

// ── Chỉ cho phép nhập số: mọi ký tự không phải chữ số bị "nuốt" ngay khi gõ ──
function restrictToDigits(inputEl, maxLength) {
    inputEl.addEventListener("input", () => {
        let digitsOnly = inputEl.value.replace(/\D/g, ""); // \D = không phải 0-9
        if (maxLength) digitsOnly = digitsOnly.slice(0, maxLength);
        inputEl.value = digitsOnly;
    });
}
restrictToDigits(document.getElementById("phoneNumber"), 10);
restrictToDigits(document.getElementById("nationalID"), 12);

function showError(message) {
    errorBox.textContent = message;
    errorBox.style.display = "block";
}

form.addEventListener("submit", async () => {
    errorBox.style.display = "none";

    const userName    = document.getElementById("userName").value.trim();
    const phoneNumber = document.getElementById("phoneNumber").value.trim();
    const nationalID  = document.getElementById("nationalID").value.trim();

    if (!/^\d{10}$/.test(phoneNumber)) {
        showError("Số điện thoại phải đúng 10 chữ số.");
        return;
    }
    if (!/^\d{12}$/.test(nationalID)) {
        showError("Số CCCD phải đúng 12 chữ số.");
        return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = "Đang xử lý...";

    try {
        const res = await fetch("/auth/google/complete-session", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {}),
            },
            body: JSON.stringify({ idToken, userName, phoneNumber, nationalID }),
        });

        const data = await res.json();

        if (res.ok && data.status === "OK") {
            // Dọn dữ liệu tạm trước khi rời trang
            sessionStorage.removeItem("pendingIdToken");
            sessionStorage.removeItem("pendingEmail");
            sessionStorage.removeItem("pendingFullName");
            window.location.href = data.redirect;
        } else {
            showError(data.message || "Có lỗi xảy ra. Vui lòng thử lại.");
            submitBtn.disabled = false;
            submitBtn.textContent = "Hoàn tất đăng ký";
        }
    } catch (err) {
        showError("Không thể kết nối máy chủ. Vui lòng thử lại.");
        submitBtn.disabled = false;
        submitBtn.textContent = "Hoàn tất đăng ký";
    }
});