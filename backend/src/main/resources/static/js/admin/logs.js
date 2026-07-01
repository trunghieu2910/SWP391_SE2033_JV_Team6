// logs.js
document.addEventListener('DOMContentLoaded', function() {
    // Auto-submit filter on enter key
    const filterInputs = document.querySelectorAll('.filter-grid input, .filter-grid select');
    filterInputs.forEach(input => {
        input.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                this.closest('form').submit();
            }
        });
    });
});