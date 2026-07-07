(function () {
    const otpInput = document.getElementById('otp');
    if (otpInput) {
        otpInput.addEventListener('input', () => {
            otpInput.value = otpInput.value.replace(/\D/g, '').slice(0, 6);
        });
    }

    const timerEl = document.getElementById('otp-timer');
    const countdownEl = document.getElementById('otp-countdown');
    const resendBtn = document.getElementById('resend-btn');
    if (!timerEl || !countdownEl || !resendBtn) return;

    let remaining = parseInt(timerEl.dataset.remaining, 10);
    if (isNaN(remaining) || remaining < 0) remaining = 0;

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