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

const password = document.getElementById('password');
const confirmPassword = document.getElementById('confirmPassword');
const confirmPasswordError = document.getElementById('confirmPasswordError');

function validatePasswordMatch() {
  if (!password || !confirmPassword || !confirmPasswordError) return;
  
  if (confirmPassword.value === '') {
    confirmPasswordError.style.display = 'none';
    confirmPassword.classList.remove('has-error');
    return;
  }
  
  if (password.value !== confirmPassword.value) {
    confirmPasswordError.textContent = 'Mật khẩu xác nhận không khớp.';
    confirmPasswordError.style.display = 'block';
    confirmPassword.classList.add('has-error');
  } else {
    confirmPasswordError.style.display = 'none';
    confirmPassword.classList.remove('has-error');
  }
}

if (password && confirmPassword) {
  password.addEventListener('input', validatePasswordMatch);
  confirmPassword.addEventListener('input', validatePasswordMatch);
}

const registerForm = document.getElementById('registerForm');
if (registerForm) {
  registerForm.addEventListener('submit', function (event) {
    if (password && confirmPassword && confirmPasswordError) {
      if (password.value !== confirmPassword.value) {
        event.preventDefault();
        confirmPasswordError.textContent = 'Mật khẩu xác nhận không khớp.';
        confirmPasswordError.style.display = 'block';
        confirmPassword.classList.add('has-error');
      } else {
        confirmPasswordError.style.display = 'none';
        confirmPassword.classList.remove('has-error');
      }
    }
  });
}
