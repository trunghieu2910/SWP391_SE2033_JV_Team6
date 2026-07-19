sessionStorage.setItem('doctorSessionsListUrl', window.location.href);

document.addEventListener('DOMContentLoaded', function() {

    // ============================================
    // AUTO CLOSE ALERT
    // ============================================
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

// ============================================
// STATUS MODAL
// ============================================
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

    if (!newStatus) {
        alert('Vui lòng chọn trạng thái mới.');
        return;
    }

    const form = document.getElementById('statusForm');
    form.action = '/doctor/sessions/' + selectedStatusSessionId + '/status';
    document.getElementById('statusFormInput').value = newStatus;
    form.submit();
}

// ============================================
// SHARE MODAL
// ============================================
let selectedShareSessionId = null;
let selectedShareCurrent = null;

function openShareModal(button) {
    selectedShareSessionId = button.getAttribute('data-session-id');
    selectedShareCurrent = button.getAttribute('data-is-shared') === 'true';

    document.getElementById('shareSessionId').textContent = selectedShareSessionId;

    const message = document.getElementById('shareMessage');
    const confirmBtn = document.getElementById('shareConfirmBtn');

    if (selectedShareCurrent) {
        message.innerHTML = 'Bạn có chắc chắn muốn <strong>gỡ công bố</strong> ca chẩn đoán #<span id="shareSessionId">' + selectedShareSessionId + '</span>?';
        confirmBtn.textContent = 'Gỡ công bố';
        confirmBtn.className = 'btn-action btn-danger';
    } else {
        message.innerHTML = 'Bạn có chắc chắn muốn <strong>công bố</strong> ca chẩn đoán #<span id="shareSessionId">' + selectedShareSessionId + '</span>?';
        confirmBtn.textContent = 'Công bố';
        confirmBtn.className = 'btn-action btn-primary';
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

    const form = document.getElementById('shareForm');
    form.action = '/doctor/sessions/' + selectedShareSessionId + '/share';
    document.getElementById('shareFormInput').value = isShared;
    form.submit();
}

// ============================================
// CLOSE MODALS ON ESCAPE KEY
// ============================================
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeStatusModal();
        closeShareModal();
    }
});

// ============================================
// CLOSE MODALS ON OUTSIDE CLICK
// ============================================
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

// ============================================
// KEYBOARD SHORTCUTS
// ============================================
document.addEventListener('keydown', function(e) {
    // Ctrl + F: Focus vào input tìm kiếm
    if (e.ctrlKey && e.key === 'f') {
        e.preventDefault();
        const searchInput = document.querySelector('input[name="keyword"]');
        if (searchInput) {
            searchInput.focus();
        }
    }
});