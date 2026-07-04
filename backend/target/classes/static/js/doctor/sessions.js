// sessions.js

document.addEventListener('DOMContentLoaded', function() {

    // ===== AUTO CLOSE ALERT =====
    const alerts = document.querySelectorAll('.alert-success, .alert-danger');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(function() {
                alert.style.display = 'none';
            }, 500);
        }, 4500);
    });

    console.log('🚀 Doctor Sessions page loaded');
});

// ===== STATUS MODAL =====
let selectedStatusSessionId = null;
let selectedStatusCurrent = null;

function openStatusModal(button) {
    selectedStatusSessionId = button.getAttribute('data-session-id');
    selectedStatusCurrent = button.getAttribute('data-current-status');

    document.getElementById('statusSessionId').textContent = selectedStatusSessionId;
    document.getElementById('statusSelect').value = selectedStatusCurrent;

    document.getElementById('statusModal').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function closeStatusModal() {
    document.getElementById('statusModal').style.display = 'none';
    document.body.style.overflow = '';
}

function confirmStatusUpdate() {
    const newStatus = document.getElementById('statusSelect').value;

    if (newStatus === selectedStatusCurrent) {
        alert('Trạng thái mới trùng với trạng thái hiện tại.');
        return;
    }

    // Tạo form và submit
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/doctor/sessions/' + selectedStatusSessionId + '/status';

    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'status';
    input.value = newStatus;

    form.appendChild(input);
    document.body.appendChild(form);
    form.submit();
}

// ===== SHARE MODAL =====
let selectedShareSessionId = null;
let selectedShareCurrent = null;

function openShareModal(button) {
    selectedShareSessionId = button.getAttribute('data-session-id');
    selectedShareCurrent = button.getAttribute('data-is-shared') === 'true';

    document.getElementById('shareSessionId').textContent = selectedShareSessionId;

    const message = document.getElementById('shareMessage');
    if (selectedShareCurrent) {
        message.innerHTML = 'Bạn có chắc chắn muốn <strong>gỡ công bố</strong> ca chẩn đoán #<span id="shareSessionId">' + selectedShareSessionId + '</span>?';
        document.getElementById('shareConfirmBtn').textContent = 'Gỡ công bố';
        document.getElementById('shareConfirmBtn').className = 'btn-action btn-danger';
    } else {
        message.innerHTML = 'Bạn có chắc chắn muốn <strong>công bố</strong> ca chẩn đoán #<span id="shareSessionId">' + selectedShareSessionId + '</span>?';
        document.getElementById('shareConfirmBtn').textContent = 'Công bố';
        document.getElementById('shareConfirmBtn').className = 'btn-action btn-primary';
    }

    document.getElementById('shareModal').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function closeShareModal() {
    document.getElementById('shareModal').style.display = 'none';
    document.body.style.overflow = '';
}

function confirmShareUpdate() {
    const isShared = !selectedShareCurrent;

    // Tạo form và submit
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/doctor/sessions/' + selectedShareSessionId + '/share';

    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'isShared';
    input.value = isShared;

    form.appendChild(input);
    document.body.appendChild(form);
    form.submit();
}

// Đóng modal khi nhấn ESC
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeStatusModal();
        closeShareModal();
    }
});

// Đóng modal khi click bên ngoài
document.addEventListener('click', function(e) {
    const statusModal = document.getElementById('statusModal');
    if (e.target === statusModal) {
        closeStatusModal();
    }
    const shareModal = document.getElementById('shareModal');
    if (e.target === shareModal) {
        closeShareModal();
    }
});