/* Drug List JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    initializeDrugList();
});

function initializeDrugList() {
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            const searchInput = document.querySelector('input[name="search"]');
            if (!searchInput.value.trim()) {
                e.preventDefault();
            }
        });
    }

    initializeDrugStatusFilter();
}

function initializeDrugStatusFilter() {
    const statusFilter = document.getElementById('drugStatusFilter');
    if (!statusFilter) return;

    statusFilter.addEventListener('change', applyDrugStatusFilter);
    applyDrugStatusFilter();
}

function applyDrugStatusFilter() {
    const statusFilter = document.getElementById('drugStatusFilter');
    const tableBody = document.getElementById('drugListTableBody');
    const emptyRow = document.getElementById('drugListEmptyRow');
    if (!statusFilter || !tableBody) return;

    const selectedStatus = statusFilter.value;
    const rows = Array.from(tableBody.querySelectorAll('tr'))
        .filter(row => row.id !== 'drugListEmptyRow' && row.querySelectorAll('td').length === 6);

    let visibleCount = 0;
    rows.forEach(row => {
        const statusText = normalizeDrugListText(row.querySelector('td:last-child')?.textContent || '');
        const isInactive = statusText.includes('ngung');
        const isActive = statusText.includes('dang dung');
        const visible = !selectedStatus ||
            (selectedStatus === 'active' && isActive) ||
            (selectedStatus === 'inactive' && isInactive);

        row.style.display = visible ? '' : 'none';
        if (visible) visibleCount++;
    });

    if (emptyRow) {
        emptyRow.style.display = rows.length > 0 && visibleCount === 0 ? '' : 'none';
    }
}

function normalizeDrugListText(value) {
    return value
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/đ/g, 'd')
        .trim();
}

function editDrug(drugId) {
    // Navigate to drug edit form
    window.location.href = `/pharmacist/drug-detail/${drugId}`;
}

function deleteDrug(drugId) {
    if (confirm('Bạn chắc chắn muốn xóa thuốc này?')) {
        // Send delete request
        fetch(`/pharmacist/drug/${drugId}`, {
            method: 'DELETE',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => {
            if (response.ok) {
                location.reload();
            } else {
                alert('Lỗi khi xóa thuốc');
            }
        })
        .catch(error => console.error('Error:', error));
    }
}

function filterByCategory(categoryId) {
    window.location.href = `/pharmacist/drug-list?categoryId=${categoryId}`;
}

// Pagination
function goToPage(pageNumber) {
    window.location.href = `/pharmacist/drug-list?page=${pageNumber}`;
}
