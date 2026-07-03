// Tự động gắn nút "hiện/ẩn mật khẩu" cho mọi <input type="password">
// nằm trong 1 wrapper có class "password-input-wrap".
// Dùng chung cho: đăng nhập, đặt lại mật khẩu, đổi mật khẩu... — chỉ cần bọc đúng cấu trúc HTML.
(function () {
    const EYE_ICON =
        `<svg class="icon-eye" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/>
            <circle cx="12" cy="12" r="3"/>
        </svg>
        <svg class="icon-eye-off" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17.94 17.94A10.94 10.94 0 0 1 12 19c-7 0-11-7-11-7a18.5 18.5 0 0 1 5.06-5.94M9.9 4.24A10.94 10.94 0 0 1 12 4c7 0 11 7 11 7a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
            <line x1="1" y1="1" x2="23" y2="23"/>
        </svg>`;

    document.querySelectorAll('.password-input-wrap').forEach((wrap) => {
        const input = wrap.querySelector('input[type="password"]');
        if (!input) return;

        const btn = document.createElement('button');
        btn.type = 'button'; // bắt buộc — nếu không sẽ submit form khi bấm
        btn.className = 'password-toggle-btn';
        btn.setAttribute('aria-label', 'Hiện mật khẩu');
        btn.innerHTML = EYE_ICON;

        btn.addEventListener('click', () => {
            const isVisible = input.type === 'text';
            input.type = isVisible ? 'password' : 'text';
            btn.classList.toggle('is-visible', !isVisible);
            btn.setAttribute('aria-label', isVisible ? 'Hiện mật khẩu' : 'Ẩn mật khẩu');
        });

        wrap.appendChild(btn);
    });
})();