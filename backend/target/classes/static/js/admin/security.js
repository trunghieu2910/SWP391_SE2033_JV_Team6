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
            const pattern = /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
            if (this.value && !pattern.test(this.value)) {
                this.style.borderColor = '#ef4444';
            } else {
                this.style.borderColor = '';
            }
        });
    }
});