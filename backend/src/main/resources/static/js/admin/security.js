// security.js
document.addEventListener('DOMContentLoaded', function() {
    // Auto-submit filter on enter key
    const filterInputs = document.querySelectorAll('.date-filter-form input');
    filterInputs.forEach(input => {
        input.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                this.closest('form').submit();
            }
        });
    });

    // IP validation
    const ipInput = document.querySelector('input[name="ipAddress"]');
    if (ipInput) {
        ipInput.addEventListener('input', function() {
            // Kiểm tra cả IPv4 và IPv6
            const ipv4Pattern = /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
            const ipv6Pattern = /^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$/;

            if (this.value && !ipv4Pattern.test(this.value) && !ipv6Pattern.test(this.value)) {
                this.style.borderColor = '#ef4444';
                this.style.borderWidth = '2px';
            } else if (this.value) {
                this.style.borderColor = '#10b981';
                this.style.borderWidth = '2px';
            } else {
                this.style.borderColor = '';
                this.style.borderWidth = '';
            }
        });
    }
});

// ============================================
// UNBLOCK MODAL FUNCTIONS
// ============================================
function openUnblockModal(ipAddress) {
    // Xóa dấu nháy đơn nếu có
    ipAddress = ipAddress.replace(/'/g, '');
    document.getElementById('unblockIpAddress').textContent = ipAddress;
    document.getElementById('unblockIpInput').value = ipAddress;
    document.getElementById('unblockForm').action = '/admin/security/unlock-ip';
    document.getElementById('unblockModal').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function closeUnblockModal() {
    document.getElementById('unblockModal').style.display = 'none';
    document.body.style.overflow = '';
}

// Đóng modal khi click bên ngoài
document.addEventListener('click', function(e) {
    var modal = document.getElementById('unblockModal');
    if (e.target === modal) {
        closeUnblockModal();
    }
});

// Đóng modal khi nhấn ESC
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeUnblockModal();
    }
});

console.log('🔒 Security page loaded');