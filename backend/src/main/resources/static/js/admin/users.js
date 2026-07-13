// users.js

document.addEventListener('DOMContentLoaded', function() {
    // ===== AUTO CLOSE ALERT AFTER 5 SECONDS =====
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

    // ===== MODAL HANDLING =====
    // Close modal on escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeStatusModal();
        }
    });

    // Close modal when clicking outside
    document.addEventListener('click', function(e) {
        const modal = document.getElementById('statusModal');
        if (e.target === modal) {
            closeStatusModal();
        }
    });
});

// ===== ĐÓNG ALERT KHI BẤM NÚT X =====
function closeAlert(button) {
    const alert = button.closest('.alert');
    if (alert) {
        alert.style.transition = 'opacity 0.3s ease';
        alert.style.opacity = '0';
        setTimeout(function() {
            alert.style.display = 'none';
        }, 300);
    }
}

// ===== MODAL FUNCTIONS =====
let selectedUserId = null;
let selectedUserStatus = null;
let selectedUserName = '';

function openStatusModal(button) {
    try {
        selectedUserId = button.getAttribute('data-id');
        selectedUserName = button.getAttribute('data-name');
        selectedUserStatus = button.getAttribute('data-status');

        console.log('🆔 User ID:', selectedUserId);
        console.log('👤 User Name:', selectedUserName);
        console.log('📊 User Status:', selectedUserStatus);

        if (!selectedUserId) {
            console.error('❌ Không tìm thấy userId');
            return;
        }

        const modal = document.getElementById('statusModal');
        const form = document.getElementById('statusForm');
        const title = document.getElementById('modalTitle');
        const desc = document.getElementById('modalDescription');
        const statusSelect = document.getElementById('statusSelect');
        const reasonInput = document.getElementById('reasonInput');
        const userIdInput = document.getElementById('formUserIdInput');

        // Set userId vào hidden input
        if (userIdInput) {
            userIdInput.value = selectedUserId;
        }

        // Reset form
        if (reasonInput) {
            reasonInput.value = '';
            reasonInput.style.borderColor = '';
            reasonInput.style.borderWidth = '';
        }

        // Set form action
        if (form) {
            form.action = '/admin/users/' + selectedUserId + '/status';
        }

        // Set modal content
        if (title) title.innerText = 'Cập nhật trạng thái';
        if (desc) desc.innerText = 'Thay đổi trạng thái cho tài khoản "' + selectedUserName + '"';
        
        // Select current status
        if (statusSelect && selectedUserStatus) {
            statusSelect.value = selectedUserStatus;
        }

        // ✅ Hiển thị modal
        if (modal) {
            modal.style.display = 'flex';
            modal.style.pointerEvents = 'auto';
            document.body.style.overflow = 'hidden';
        }

    } catch (error) {
        console.error('❌ Lỗi khi mở modal:', error);
        alert('Có lỗi xảy ra khi mở modal. Vui lòng thử lại.');
    }
}

function closeStatusModal() {
    try {
        const modal = document.getElementById('statusModal');
        if (modal) {
            modal.style.display = 'none';
            modal.style.pointerEvents = 'none';
        }
        document.body.style.overflow = '';

        const reasonInput = document.getElementById('reasonInput');
        if (reasonInput) {
            reasonInput.value = '';
            reasonInput.style.borderColor = '';
            reasonInput.style.borderWidth = '';
        }
    } catch (error) {
        console.error('❌ Lỗi khi đóng modal:', error);
    }
}

function submitStatusForm() {
    try {
        const form = document.getElementById('statusForm');
        const reason = document.getElementById('reasonInput');

        if (!reason || !reason.value || reason.value.trim().length < 5) {
            alert('Lý do thay đổi trạng thái phải từ 5 ký tự trở lên.');
            if (reason) {
                reason.focus();
                reason.style.borderColor = '#ef4444';
                reason.style.borderWidth = '2px';
                setTimeout(() => {
                    reason.style.borderColor = '';
                    reason.style.borderWidth = '';
                }, 2000);
            }
            return;
        }

        console.log('📤 Submitting form for user:', selectedUserId);
        if (form) {
            form.submit();
        }
    } catch (error) {
        console.error('❌ Lỗi khi submit form:', error);
        alert('Có lỗi xảy ra khi gửi form. Vui lòng thử lại.');
    }
}

console.log('👥 Users page loaded');