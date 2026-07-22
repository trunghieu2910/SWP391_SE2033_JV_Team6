// Đọc thông tin tạm được lưu ở bước 1 (google-login.js)
const idToken  = sessionStorage.getItem("pendingIdToken");
const email    = sessionStorage.getItem("pendingEmail");
const fullName = sessionStorage.getItem("pendingFullName");
const provinceDistrictMap = {
    'An Giang': ['Long Xuyên', 'Châu Đốc', 'Châu Phú', 'Tân Châu', 'An Phú', 'Phú Tân', 'Thoại Sơn', 'Tịnh Biên', 'Tri Tôn'],
    'Bà Rịa - Vũng Tàu': ['Vũng Tàu', 'Bà Rịa', 'Long Điền', 'Xuyên Mộc', 'Đất Đỏ', 'Phú Mỹ', 'Châu Đức', 'Côn Đảo'],
    'Bạc Liêu': ['Bạc Liêu', 'Hòa Bình', 'Hồng Dân', 'Phước Long', 'Vĩnh Lợi', 'Giá Rai'],
    'Bắc Giang': ['Bắc Giang', 'Yên Thế', 'Tân Yên', 'Lạng Giang', 'Lục Nam', 'Hiệp Hòa', 'Việt Yên', 'Sơn Động'],
    'Bắc Kạn': ['Bắc Kạn', 'Na Rì', 'Ngân Sơn', 'Ba Bể', 'Chợ Đồn', 'Pác Nặm', 'Bạch Thông', 'Cho Đơn'],
    'Bắc Ninh': ['Bắc Ninh', 'Từ Sơn', 'Tiên Du', 'Gia Bình', 'Lương Tài', 'Thuận Thành', 'Quế Võ'],
    'Bến Tre': ['Bến Tre', 'Châu Thành', 'Chợ Lách', 'Mỏ Cày Bắc', 'Mỏ Cày Nam', 'Giồng Trôm', 'Ba Tri', 'Thạnh Phú'],
    'Bình Dương': ['Thủ Dầu Một', 'Dĩ An', 'Thuận An', 'Bến Cát', 'Tân Uyên', 'Phú Giáo', 'Bắc Tân Uyên'],
    'Bình Định': ['Quy Nhơn', 'An Lão', 'Hoài Nhơn', 'Hoài Ân', 'Phù Mỹ', 'Tuy Phước', 'Vân Canh', 'Vĩnh Thạnh'],
    'Bình Phước': ['Đồng Xoài', 'Bình Long', 'Phước Long', 'Chơn Thành', 'Lộc Ninh', 'Bù Đăng', 'Bù Gia Mập', 'Hớn Quản'],
    'Bình Thuận': ['Phan Thiết', 'La Gi', 'Tuy Phong', 'Bắc Bình', 'Hàm Thuận Bắc', 'Hàm Thuận Nam', 'Tánh Linh', 'Đức Linh'],
    'Cà Mau': ['Cà Mau', 'U Minh', 'Thới Bình', 'Trần Văn Thời', 'Cái Nước', 'Năm Căn', 'Đầm Dơi', 'Ngọc Hiển'],
    'Cần Thơ': ['Ninh Kiều', 'Bình Thủy', 'Cái Răng', 'Ô Môn', 'Thốt Nốt', 'Phong Điền', 'Cờ Đỏ', 'Vĩnh Thạnh'],
    'Cao Bằng': ['Cao Bằng', 'Bảo Lâm', 'Hạ Lang', 'Nguyên Bình', 'Phục Hòa', 'Quảng Uyên', 'Thạch An', 'Trùng Khánh'],
    'Đà Nẵng': ['Hải Châu', 'Thanh Khê', 'Sơn Trà', 'Ngũ Hành Sơn', 'Liên Chiểu', 'Cẩm Lệ', 'Hòa Vang', 'Hoàng Sa'],
    'Đắk Lắk': ['Buôn Ma Thuột', 'Buôn Hồ', 'Ea Hleo', 'Ea Súp', 'Krông Pắc', 'Krông Năng', 'Krông Bông', 'M Đức', 'Cư Mgar', 'Ea Kar'],
    'Đắk Nông': ['Gia Nghĩa', 'Cư Jút', 'Đắk Mil', 'Đắk R’ Lấp', 'Đắk Song', 'Krông Nô', 'Tuy Đức'],
    'Điện Biên': ['Điện Biên Phủ', 'Mường Lay', 'Điện Biên', 'Mường Nhé', 'Tủa Chùa', 'Tuần Giáo', 'Điện Biên Đông', 'Nậm Pồ'],
    'Đồng Nai': ['Biên Hòa', 'Long Khánh', 'Nhơn Trạch', 'Trảng Bom', 'Vĩnh Cửu', 'Xuân Lộc', 'Định Quán', 'Long Thành', 'Thống Nhất'],
    'Đồng Tháp': ['Cao Lãnh', 'Sa Đéc', 'Hồng Ngự', 'Lai Vung', 'Tân Hồng', 'Tam Nông', 'Tháp Mười', 'Thanh Bình', 'Hồng Ngự'],
    'Gia Lai': ['Pleiku', 'An Khê', 'Ayun Pa', 'Chư Păh', 'Chư Prông', 'Đăk Pơ', 'Kbang', 'Krông Pa', 'Mang Yang', 'Phú Thiện'],
    'Hà Giang': ['Hà Giang', 'Đồng Văn', 'Mèo Vạc', 'Yên Minh', 'Quản Bạ', 'Vị Xuyên', 'Bắc Mê', 'Hoàng Su Phì', 'Xín Mần', 'Quang Bình'],
    'Hà Nam': ['Phủ Lý', 'Duy Tiên', 'Kim Bảng', 'Lý Nhân', 'Thanh Liêm'],
    'Hà Nội': ['Ba Đình', 'Hoàn Kiếm', 'Hai Bà Trưng', 'Đống Đa', 'Cầu Giấy', 'Thanh Xuân', 'Long Biên', 'Nam Từ Liêm', 'Bắc Từ Liêm', 'Hà Đông', 'Sóc Sơn', 'Đan Phượng', 'Gia Lâm', 'Hoài Đức', 'Mê Linh', 'Thường Tín', 'Phúc Thọ', 'Thạch Thất', 'Chương Mỹ'],
    'Hà Tĩnh': ['Hà Tĩnh', 'Hồng Lĩnh', 'Kỳ Anh', 'Cẩm Xuyên', 'Hương Khê', 'Thạch Hà', 'Vũ Quang', 'Can Lộc', 'Nghi Xuân'],
    'Hải Dương': ['Hải Dương', 'Chí Linh', 'Nam Sách', 'Kinh Môn', 'Kim Thành', 'Gia Lộc', 'Tứ Kỳ', 'Thanh Hà', 'Ninh Giang'],
    'Hải Phòng': ['Hồng Bàng', 'Ngô Quyền', 'Lê Chân', 'Kiến An', 'Hải An', 'Đồ Sơn', 'An Dương', 'Dương Kinh', 'An Lão', 'Vĩnh Bảo', 'Tiên Lãng', 'Thuỷ Nguyên'],
    'Hậu Giang': ['Vị Thanh', 'Ngã Bảy', 'Long Mỹ', 'Phụng Hiệp', 'Châu Thành', 'Châu Thành A'],
    'Hòa Bình': ['Hòa Bình', 'Đà Bắc', 'Kỳ Sơn', 'Lạc Sơn', 'Lạc Thủy', 'Mai Châu', 'Tân Lạc', 'Yên Thủy'],
    'Hưng Yên': ['Hưng Yên', 'Mỹ Hào', 'Ân Thi', 'Khoái Châu', 'Kim Động', 'Phù Cừ', 'Tiên Lữ', 'Văn Lâm', 'Yên Mỹ'],
    'Khánh Hòa': ['Nha Trang', 'Cam Ranh', 'Cam Lâm', 'Diên Khánh', 'Khánh Sơn', 'Khánh Vĩnh', 'Trường Sa'],
    'Kiên Giang': ['Rạch Giá', 'Hà Tiên', 'Kiên Lương', 'An Biên', 'An Minh', 'Châu Thành', 'Giồng Riềng', 'Go Quao', 'Gò Công', 'Tân Hiệp', 'Vĩnh Thuận', 'U Minh Thượng'],
    'Kon Tum': ['Kon Tum', 'Đắk Glei', 'Đắk Hà', 'Sa Thầy', 'Tu Mơ Rôn', 'Ngọc Hồi'],
    'Lâm Đồng': ['Đà Lạt', 'Bảo Lộc', 'Di Linh', 'Đơn Dương', 'Đức Trọng', 'Lạc Dương', 'Lâm Hà', 'Bảo Lâm', 'Cát Tiên'],
    'Thành phố Hồ Chí Minh': ['Quận 1', 'Quận 3', 'Quận 4', 'Quận 5', 'Quận 6', 'Quận 7', 'Quận 8', 'Quận 10', 'Quận 11', 'Quận 12', 'Bình Tân', 'Bình Thạnh', 'Gò Vấp', 'Phú Nhuận', 'Tân Bình', 'Tân Phú', 'Thủ Đức', 'Hóc Môn', 'Củ Chi', 'Bình Chánh', 'Nhà Bè', 'Cần Giờ'],
    'Lai Châu': ['Lai Châu', 'Tam Đường', 'Mường Tè', 'Sìn Hồ', 'Phong Thổ', 'Than Uyên', 'Nậm Nhùn'],
    'Lạng Sơn': ['Lạng Sơn', 'Đình Lập', 'Bắc Sơn', 'Tràng Định', 'Văn Lãng', 'Cao Lộc', 'Hữu Lũng', 'Chi Lăng'],
    'Lào Cai': ['Lào Cai', 'Bát Xát', 'Mường Khương', 'Bảo Thắng', 'Bảo Yên', 'Sa Pa', 'Văn Bàn'],
    'Long An': ['Tân An', 'Bến Lức', 'Cần Giuộc', 'Cần Đước', 'Châu Thành', 'Đức Hòa', 'Đức Huệ', 'Mộc Hóa', 'Tân Hưng', 'Tân Thạnh', 'Tân Trụ', 'Văn Thân', 'Vĩnh Hưng'],
    'Nam Định': ['Nam Định', 'Giao Thủy', 'Hải Hậu', 'Mỹ Lộc', 'Nam Trực', 'Nghĩa Hưng', 'Trực Ninh', 'Xuân Trường', 'Vụ Bản', 'Ý Yên'],
    'Nghệ An': ['Vinh', 'Cửa Lò', 'Hoàng Mai', 'Nghi Lộc', 'Quỳnh Lưu', 'Thái Hoà', 'Anh Sơn', 'Con Cuông', 'Diễn Châu', 'Hưng Nguyên', 'Kỳ Sơn', 'Nam Đàn', 'Nậm Nhùn', 'Quế Phong', 'Tân Kỳ', 'Thanh Chương', 'Tương Dương', 'Yên Thành'],
    'Ninh Bình': ['Ninh Bình', 'Tam Điệp', 'Gia Viễn', 'Hoa Lư', 'Kim Sơn', 'Nho Quan', 'Yên Khánh', 'Yên Mô'],
    'Ninh Thuận': ['Phan Rang - Tháp Chàm', 'Bác Ái', 'Ninh Hải', 'Ninh Phước', 'Thuận Bắc', 'Thuận Nam'],
    'Phú Thọ': ['Việt Trì', 'Phú Thọ', 'Hạ Hoà', 'Cẩm Khê', 'Thanh Ba', 'Thanh Sơn', 'Tam Nông', 'Tân Sơn', 'Yên Lập', 'Lâm Thao', 'Văn Chấn', 'Đoan Hùng', 'Thanh Thuỷ'],
    'Phú Yên': ['Tuy Hòa', 'Đông Hòa', 'Sông Cầu', 'Tuy An', 'Phú Hòa', 'Sông Hinh', 'Tây Hòa'],
    'Quảng Bình': ['Đồng Hới', 'Ba Đồn', 'Bố Trạch', 'Lệ Thủy', 'Minh Hóa', 'Quảng Ninh', 'Quảng Trạch', 'Tuyên Hóa'],
    'Quảng Nam': ['Tam Kỳ', 'Hội An', 'Điện Bàn', 'Duy Xuyên', 'Nam Giang', 'Nông Sơn', 'Phú Ninh', 'Phước Sơn', 'Quế Sơn', 'Thăng Bình', 'Tiên Phước', 'Bắc Trà My', 'Nam Trà My'],
    'Quảng Ngãi': ['Quảng Ngãi', 'Bình Sơn', 'Đức Phổ', 'Lý Sơn', 'Minh Long', 'Mộ Đức', 'Nghĩa Hành', 'Sơn Tịnh', 'Tây Trà', 'Trà Bồng', 'Tư Nghĩa'],
    'Quảng Ninh': ['Hạ Long', 'Cẩm Phả', 'Uông Bí', 'Móng Cái', 'Đầm Hà', 'Cô Tô', 'Đông Triều', 'Quảng Yên', 'Vân Đồn', 'Tiên Yên', 'Ba Chẽ', 'Hoành Bồ', 'Bình Liêu'],
    'Quảng Trị': ['Đông Hà', 'Quảng Trị', 'Cam Lộ', 'Cồn Cỏ', 'Đăk Rông', 'Gio Linh', 'Hải Lăng', 'Horath', 'Triệu Phong', 'Vĩnh Linh'],
    'Sóc Trăng': ['Sóc Trăng', 'Long Phú', 'Mỹ Tú', 'Mỹ Xuyên', 'Ngã Năm', 'Thạnh Trị', 'Trần Đề', 'Vĩnh Châu'],
    'Sơn La': ['Sơn La', 'Mai Sơn', 'Quỳnh Nhai', 'Thuận Châu', 'Mộc Châu', 'Yên Châu', 'Phù Yên', 'Sông Mã', 'Bắc Yên', 'Vân Hồ'],
    'Tây Ninh': ['Tây Ninh', 'Tân Biên', 'Tân Châu', 'Dương Minh Châu', 'Châu Thành', 'Hòa Thành', 'Bến Cầu', 'Gò Dầu'],
    'Thái Bình': ['Thái Bình', 'Đông Hưng', 'Hưng Hà', 'Kiến Xương', 'Quỳnh Phụ', 'Thái Thụy', 'Vũ Thư'],
    'Thái Nguyên': ['Thái Nguyên', 'Sông Công', 'Đại Từ', 'Định Hóa', 'Phú Lương', 'Phổ Yên', 'Võ Nhai', 'Đồng Hỷ'],
    'Thanh Hóa': ['Thanh Hóa', 'Bỉm Sơn', 'Sầm Sơn', 'Thọ Xuân', 'Triệu Sơn', 'Nông Cống', 'Như Xuân', 'Như Thanh', 'Lang Chánh', 'Hà Trung', 'Hậu Lộc', 'Hoằng Hóa', 'Nga Sơn', 'Ngọc Lặc', 'Thạch Thành', 'Vĩnh Lộc', 'Quan Hóa', 'Quan Sơn', 'Mường Lát', 'Cẩm Thủy', 'Thường Xuân', 'Đông Sơn', 'Bá Thước'],
    'Thừa Thiên Huế': ['Huế', 'Hương Thủy', 'Hương Trà', 'Phong Điền', 'Quảng Điền', 'A Lưới', 'Phú Lộc', 'Nam Đông'],
    'Tiền Giang': ['Mỹ Tho', 'Cai Lậy', 'Cai Lậy', 'Tân Phước', 'Châu Thành', 'Gò Công', 'Tân Phú Đông', 'Chợ Gạo', 'Gò Công Tây', 'Gò Công Đông'],
    'Trà Vinh': ['Trà Vinh', 'Càng Long', 'Cầu Kè', 'Tiểu Cần', 'Châu Thành', 'Duyên Hải', 'Trà Cú'],
    'Tuyên Quang': ['Tuyên Quang', 'Na Hang', 'Chiêm Hóa', 'Hàm Yên', 'Lâm Bình', 'Yên Sơn'],
    'Vĩnh Long': ['Vĩnh Long', 'Bình Minh', 'Long Hồ', 'Mang Thít', 'Tam Bình', 'Trà Ôn', 'Vũng Liêm'],
    'Vĩnh Phúc': ['Vĩnh Yên', 'Phúc Yên', 'Bình Xuyên', 'Lập Thạch', 'Sông Lô', 'Tam Dương', 'Tam Đảo', 'Vĩnh Tường', 'Yên Lập'],
    'Yên Bái': ['Yên Bái', 'Lục Yên', 'Mù Cang Chải', 'Trạm Tấu', 'Văn Chấn', 'Văn Yên', 'Yên Bình']
};

// Nếu vào thẳng trang này mà không qua bước đăng nhập Google → đá về /login
if (!idToken) {
    window.location.href = "/auth/login";
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

const provinceSelect = document.getElementById("province");
const districtSelect = document.getElementById("district");

// Đổ danh sách tỉnh/thành vào dropdown khi trang load
Object.keys(provinceDistrictMap).forEach((provinceName) => {
    const opt = document.createElement("option");
    opt.value = provinceName;
    opt.textContent = provinceName;
    provinceSelect.appendChild(opt);
});

// Khi chọn tỉnh/thành → đổ danh sách quận/huyện tương ứng, mở khóa dropdown
provinceSelect.addEventListener("change", () => {
    const selectedProvince = provinceSelect.value;

    districtSelect.innerHTML = '<option value="">-- Chọn quận/huyện --</option>';

    if (selectedProvince && provinceDistrictMap[selectedProvince]) {
        provinceDistrictMap[selectedProvince].forEach((districtName) => {
            const opt = document.createElement("option");
            opt.value = districtName;
            opt.textContent = districtName;
            districtSelect.appendChild(opt);
        });
        districtSelect.disabled = false;
    } else {
        districtSelect.disabled = true;
    }
});

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

// Tùy chỉnh thông báo lỗi validate HTML5 cho số điện thoại
const phoneInput = document.getElementById("phoneNumber");
phoneInput.addEventListener("invalid", () => {
    if (phoneInput.validity.patternMismatch || phoneInput.validity.valueMissing) {
        phoneInput.setCustomValidity("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.");
    }
});
phoneInput.addEventListener("input", () => {
    phoneInput.setCustomValidity(""); // reset lỗi mỗi khi người dùng gõ lại
});

function showError(message) {
    errorBox.textContent = message;
    errorBox.style.display = "block";
}

form.addEventListener("submit", async () => {
    errorBox.style.display = "none";

    const userName    = document.getElementById("userName").value.trim();
    const phoneNumber = document.getElementById("phoneNumber").value.trim();
    const nationalID  = document.getElementById("nationalID").value.trim();
    const gender      = document.getElementById("gender").value;
    const dob         = document.getElementById("dob").value;

    const province      = document.getElementById("province").value;
    const district       = document.getElementById("district").value;
    const addressDetail  = document.getElementById("addressDetail").value.trim();

    if (!/^0\d{9}$/.test(phoneNumber)) {
        showError("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.");
        return;
    }
    if (!/^\d{12}$/.test(nationalID)) {
        showError("Số CCCD phải đúng 12 chữ số.");
        return;
    }
    if (!gender) {
        showError("Vui lòng chọn giới tính.");
        return;
    }
    if (!dob) {
        showError("Vui lòng nhập ngày sinh.");
        return;
    }
    if (new Date(dob) >= new Date()) {
        showError("Ngày sinh phải là ngày trong quá khứ.");
        return;
    }
    // MỚI: validate 3 field địa chỉ
    if (!province) {
        showError("Vui lòng chọn tỉnh/thành phố.");
        return;
    }
    if (!district) {
        showError("Vui lòng chọn quận/huyện.");
        return;
    }
    if (!addressDetail) {
        showError("Vui lòng nhập địa chỉ cư trú.");
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
            // MỚI: gửi province, district, addressDetail thay vì address
            body: JSON.stringify({
                idToken, userName, phoneNumber, nationalID, gender, dob,
                province, district, addressDetail
            }),
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