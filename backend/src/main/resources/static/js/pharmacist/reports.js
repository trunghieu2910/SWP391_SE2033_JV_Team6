/* Reports JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    initializeReports();
});

function initializeReports() {
    const filterForm = document.querySelector('.filter-form');
    if (filterForm) {
        filterForm.addEventListener('submit', validateReportFilter);
    }

    // Set default dates
    const today = new Date();
    const monthAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000);

    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');

    if (startDateInput && !startDateInput.value) {
        startDateInput.valueAsDate = monthAgo;
    }
    if (endDateInput && !endDateInput.value) {
        endDateInput.valueAsDate = today;
    }
}

function validateReportFilter(e) {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!startDate || !endDate) {
        e.preventDefault();
        alert('Vui lòng chọn khoảng ngày');
        return false;
    }

    if (new Date(endDate) < new Date(startDate)) {
        e.preventDefault();
        alert('Ngày kết thúc phải lớn hơn ngày bắt đầu');
        return false;
    }

    return true;
}

function exportReport() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!startDate || !endDate) {
        alert('Vui lòng chọn khoảng ngày');
        return;
    }

    window.location.href = `/pharmacist/export-report?startDate=${startDate}&endDate=${endDate}`;
}

function printReport() {
    window.print();
}

// Format date for display
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}
