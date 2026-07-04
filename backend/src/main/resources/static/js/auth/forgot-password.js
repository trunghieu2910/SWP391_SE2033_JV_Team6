(function () {
    // ── Chỉ cho phép nhập số ở ô OTP: ký tự không phải chữ số bị "nuốt" ngay khi gõ ──
    const otpInput = document.getElementById('otp');
    if (otpInput) {
        otpInput.addEventListener('input', () => {
            otpInput.value = otpInput.value.replace(/\D/g, '').slice(0, 6);
        });
    }

    const OTP_TTL = 120; // giây — khớp với OTP_EXPIRE_MINUTES = 2 bên backend
    const countdownEl = document.getElementById('otp-countdown');
    const resendBtn = document.getElementById('resend-btn');
    if (!countdownEl || !resendBtn) return;

    let remaining = OTP_TTL;

    const format = (s) => {
        const m = String(Math.floor(s / 60)).padStart(2, '0');
        const sec = String(s % 60).padStart(2, '0');
        return `${m}:${sec}`;
    };

    const tick = () => {
        if (remaining <= 0) {
            countdownEl.textContent = 'đã hết hạn';
            resendBtn.disabled = false;
            return;
        }
        countdownEl.textContent = format(remaining);
        remaining -= 1;
        setTimeout(tick, 1000);
    };

    tick();
})();