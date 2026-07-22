// create-doctor.js

document.addEventListener('DOMContentLoaded', function() {
    // === FILE UPLOAD ===
    const fileInput = document.getElementById('certificateFile');
    const fileInfo = document.getElementById('fileInfo');
    const fileName = document.getElementById('fileName');
    const uploadArea = document.getElementById('fileUploadArea');

    if (fileInput) {
        fileInput.addEventListener('change', function() {
            if (this.files?.length) {
                fileInfo.style.display = 'flex';
                fileName.textContent = 'Đã chọn: ' + this.files[0].name;
            } else {
                fileInfo.style.display = 'none';
            }
        });

        if (uploadArea) {
            ['dragover', 'dragleave'].forEach(evt => {
                uploadArea.addEventListener(evt, e => {
                    e.preventDefault();
                    evt === 'dragover' ? uploadArea.classList.add('dragover') : uploadArea.classList.remove('dragover');
                });
            });

            uploadArea.addEventListener('drop', function(e) {
                e.preventDefault();
                this.classList.remove('dragover');
                if (e.dataTransfer.files.length) {
                    fileInput.files = e.dataTransfer.files;
                    fileInfo.style.display = 'flex';
                    fileName.textContent = 'Đã chọn: ' + e.dataTransfer.files[0].name;
                }
            });
        }
    }

    // === OTP TIMER ===
    const timerEl = document.getElementById('timerCountdown');
    if (timerEl) {
        let interval = null;
        let isResending = false;
        let isStarted = false;

        const remainingInput = document.getElementById('remainingTime');
        let initialSeconds = remainingInput ? parseInt(remainingInput.value) : 120;
        if (isNaN(initialSeconds) || initialSeconds <= 0) {
            initialSeconds = 120;
        }

        const updateDisplay = (time) => {
            const m = String(Math.floor(time / 60)).padStart(2, '0');
            const s = String(time % 60).padStart(2, '0');
            timerEl.textContent = m + ':' + s;
            timerEl.style.color = time <= 30 ? '#ef4444' : '';
        };

        const startTimer = (seconds) => {
            if (isStarted) return;
            clearInterval(interval);
            let time = seconds || 120;
            updateDisplay(time);
            isStarted = true;
            const btn = document.getElementById('resendOtpBtn');
            if (btn) { btn.disabled = true; btn.innerHTML = '<i class="fa-solid fa-rotate"></i> Gửi lại mã OTP'; }
            document.getElementById('resendMessage').style.display = 'none';

            interval = setInterval(() => {
                time--;
                updateDisplay(time);
                if (time <= 0) {
                    clearInterval(interval);
                    timerEl.textContent = '00:00';
                    const btn = document.getElementById('resendOtpBtn');
                    if (btn) {
                        btn.disabled = false;
                        btn.innerHTML = '<i class="fa-solid fa-rotate"></i> Gửi lại mã OTP';
                    }
                    isStarted = false;
                }
            }, 1000);
        };

        startTimer(initialSeconds);

        // === SUBMIT OTP - KHÔNG DÙNG AJAX ===
        const otpForm = document.getElementById('otpForm');
        const otpInput = document.getElementById('otpInput');
        const errorDiv = document.getElementById('otpError');
        const errorMessage = document.getElementById('otpErrorMessage');

        if (otpForm) {
            otpForm.addEventListener('submit', function(e) {
                const otp = otpInput.value.trim();

                // Validate OTP
                if (otp.length !== 6 || !/^\d{6}$/.test(otp)) {
                    e.preventDefault();
                    showError('Vui lòng nhập đúng mã OTP gồm 6 chữ số.');
                    return;
                }

                // Nếu OTP hợp lệ, form sẽ submit bình thường (không preventDefault)
                // Lưu ý: không có e.preventDefault() ở đây
                // Controller sẽ xử lý và redirect với flash message
            });
        }

        function showError(message) {
            errorMessage.textContent = message;
            errorDiv.style.display = 'block';
            errorDiv.classList.add('show');
            setTimeout(function() {
                errorDiv.style.display = 'none';
                errorDiv.classList.remove('show');
            }, 4000);
        }

        // === ROLE CHANGE LOGIC ===
        const roleSelect = document.getElementById('roleName');
        const fileGroup = document.querySelector('.file-group');
        
        if (roleSelect && fileGroup && fileInput) {
            roleSelect.addEventListener('change', function() {
                if (this.value === 'DOCTOR') {
                    fileGroup.style.display = 'block';
                    fileInput.setAttribute('required', 'required');
                } else {
                    fileGroup.style.display = 'none';
                    fileInput.removeAttribute('required');
                    removeFile();
                }
            });
            // Trigger change on load to set initial state
            roleSelect.dispatchEvent(new Event('change'));
        }

        // === Resend OTP ===
        document.getElementById('resendOtpBtn')?.addEventListener('click', function() {
            if (isResending) return;
            isResending = true;
            this.disabled = true;
            this.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang gửi...';

            fetch('/admin/create-staff/resend-otp', { method: 'POST', headers: { 'Content-Type': 'application/json' } })
                .then(r => r.json())
                .then(data => {
                    if (data.success) {
                        const msg = document.getElementById('resendMessage');
                        msg.style.display = 'block';
                        setTimeout(() => msg.style.display = 'none', 3000);
                        clearInterval(interval);
                        isStarted = false;
                        startTimer(120);
                        errorDiv.style.display = 'none';
                        errorDiv.classList.remove('show');
                    } else {
                        alert(data.message || 'Không thể gửi lại OTP.');
                        this.disabled = false;
                    }
                })
                .catch(() => alert('Đã xảy ra lỗi. Vui lòng thử lại.'))
                .finally(() => {
                    isResending = false;
                    this.innerHTML = '<i class="fa-solid fa-rotate"></i> Gửi lại mã OTP';
                });
        });

        // === OTP Input ===
        if (otpInput) {
            otpInput.addEventListener('input', function() {
                this.value = this.value.replace(/\D/g, '');
                errorDiv.style.display = 'none';
                errorDiv.classList.remove('show');
                if (this.value.length === 6) {
                    document.getElementById('confirmBtn')?.focus();
                }
            });
            setTimeout(() => otpInput.focus(), 500);
        }
    }
});

// Remove file
function removeFile() {
    const input = document.getElementById('certificateFile');
    if (input) { input.value = ''; document.getElementById('fileInfo').style.display = 'none'; }
}