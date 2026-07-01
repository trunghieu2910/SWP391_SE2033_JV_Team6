// users.js
document.addEventListener('DOMContentLoaded', function() {
    // Close modal on escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeStatusModal();
        }
    });
});

function openStatusModal(button) {
    const userId = button.getAttribute('data-id');
    const fullName = button.getAttribute('data-name');
    const currentStatus = button.getAttribute('data-status');

    const modal = document.getElementById('statusModal');
    const form = document.getElementById('statusForm');
    const title = document.getElementById('modalTitle');
    const desc = document.getElementById('modalDescription');
    const statusInput = document.getElementById('formStatusInput');
    const submitBtn = document.getElementById('modalSubmitBtn');
    const submitText = document.getElementById('modalSubmitText');

    form.action = `/admin/users/${userId}/status`;

    if (currentStatus === 'BANNED') {
        title.innerText = 'Mở khóa tài khoản';
        desc.innerText = `Bạn có chắc chắn muốn mở khóa cho "${fullName}"?`;
        statusInput.value = 'ACTIVE';
        submitText.innerText = 'Mở khóa ngay';
        submitBtn.className = 'btn-action btn-success';
    } else {
        title.innerText = 'Khóa tài khoản';
        desc.innerText = `Bạn có chắc chắn muốn khóa "${fullName}"?`;
        statusInput.value = 'BANNED';
        submitText.innerText = 'Khóa tài khoản';
        submitBtn.className = 'btn-action btn-danger';
    }

    modal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

function closeStatusModal() {
    const modal = document.getElementById('statusModal');
    modal.classList.remove('show');
    document.body.style.overflow = '';
    document.getElementById('reasonInput').value = '';
}

function submitStatusForm() {
    const form = document.getElementById('statusForm');
    const reason = document.getElementById('reasonInput');

    if (!reason.value) {
        reason.focus();
        reason.style.borderColor = '#ef4444';
        setTimeout(() => {
            reason.style.borderColor = '';
        }, 2000);
        return;
    }

    form.submit();
}