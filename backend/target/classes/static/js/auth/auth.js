document.addEventListener('click', function (event) {
  const toggle = event.target.closest('[data-toggle-target]');
  if (!toggle) return;

  const targetId = toggle.getAttribute('data-toggle-target');
  const input = document.getElementById(targetId);
  if (!input) return;

  const isPassword = input.type === 'password';
  input.type = isPassword ? 'text' : 'password';
  toggle.classList.toggle('active', isPassword);
});

const registerForm = document.getElementById('registerForm');
if (registerForm) {
  registerForm.addEventListener('submit', function (event) {
    const password = document.getElementById('password');
    const confirmPassword = document.getElementById('confirmPassword');
    const errorEl = document.getElementById('confirmPasswordError');

    if (password && confirmPassword && errorEl) {
      if (password.value !== confirmPassword.value) {
        event.preventDefault();
        errorEl.textContent = 'Mật khẩu xác nhận không khớp.';
        errorEl.style.display = 'block';
      } else {
        errorEl.style.display = 'none';
      }
    }
  });
}
