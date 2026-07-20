/* Import History JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    initializeImportHistory();
});

function initializeImportHistory() {
    // Initialize import history page
    initializeImportHistorySearchAndSort();
}

function initializeImportHistorySearchAndSort() {
    const searchInput = document.getElementById('importHistorySearchInput');
    const sortSelect = document.getElementById('importHistorySortSelect');
    const tableBody = document.getElementById('importHistoryTableBody');

    if (!tableBody) return;

    const rows = Array.from(tableBody.querySelectorAll('tr'))
        .filter(row => row.id !== 'importHistoryEmptyRow' && row.querySelectorAll('td').length === 8);

    rows.forEach((row, index) => {
        const cells = row.querySelectorAll('td');
        row.dataset.originalIndex = String(index);
        row.dataset.drugName = normalizeImportHistoryText(cells[0]?.textContent || '');
        row.dataset.batchNumber = normalizeImportHistoryText(cells[1]?.textContent || '');
        row.dataset.expiryTime = String(parseImportHistoryDate(cells[3]?.textContent || ''));
        row.dataset.stock = String(parseImportHistoryNumber(cells[7]?.textContent || ''));
    });

    if (searchInput) {
        searchInput.addEventListener('input', applyImportHistorySearchAndSort);
    }

    if (sortSelect) {
        sortSelect.addEventListener('change', applyImportHistorySearchAndSort);
    }

    applyImportHistorySearchAndSort();
}

function applyImportHistorySearchAndSort() {
    const searchInput = document.getElementById('importHistorySearchInput');
    const sortSelect = document.getElementById('importHistorySortSelect');
    const tableBody = document.getElementById('importHistoryTableBody');
    const emptyRow = document.getElementById('importHistoryEmptyRow');

    if (!tableBody) return;

    const keyword = normalizeImportHistoryText(searchInput?.value || '');
    const sortMode = sortSelect?.value || '';
    const rows = Array.from(tableBody.querySelectorAll('tr'))
        .filter(row => row.id !== 'importHistoryEmptyRow' && row.dataset.originalIndex !== undefined);

    rows.forEach(row => {
        const matched = !keyword ||
            row.dataset.drugName.includes(keyword) ||
            row.dataset.batchNumber.includes(keyword);
        row.dataset.visible = matched ? 'true' : 'false';
    });

    rows.sort((a, b) => compareImportHistoryRows(a, b, sortMode));
    rows.forEach(row => {
        row.style.display = row.dataset.visible === 'true' ? '' : 'none';
        tableBody.appendChild(row);
    });

    const visibleCount = rows.filter(row => row.dataset.visible === 'true').length;
    if (emptyRow) {
        emptyRow.style.display = visibleCount === 0 && rows.length > 0 ? '' : 'none';
        tableBody.appendChild(emptyRow);
    }
}

function compareImportHistoryRows(a, b, sortMode) {
    if (sortMode === 'expiry-asc') {
        return Number(a.dataset.expiryTime) - Number(b.dataset.expiryTime);
    }
    if (sortMode === 'expiry-desc') {
        return Number(b.dataset.expiryTime) - Number(a.dataset.expiryTime);
    }
    if (sortMode === 'stock-asc') {
        return Number(a.dataset.stock) - Number(b.dataset.stock);
    }
    if (sortMode === 'stock-desc') {
        return Number(b.dataset.stock) - Number(a.dataset.stock);
    }

    return Number(a.dataset.originalIndex) - Number(b.dataset.originalIndex);
}

function normalizeImportHistoryText(value) {
    return value
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .trim();
}

function parseImportHistoryDate(value) {
    const match = value.trim().match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
    if (!match) return 0;

    const day = Number(match[1]);
    const month = Number(match[2]) - 1;
    const year = Number(match[3]);
    return new Date(year, month, day).getTime();
}

function parseImportHistoryNumber(value) {
    const match = value.replace(/[^\d-]/g, '');
    return match ? Number(match) : 0;
}

function viewBatchDetail(batchId) {
    window.location.href = `/pharmacist/batch-detail/${batchId}`;
}

function deleteBatch(batchId) {
    if (confirm('Bạn chắc chắn muốn xóa lô hàng này?')) {
        fetch(`/pharmacist/batch/${batchId}`, {
            method: 'DELETE',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => {
            if (response.ok) {
                location.reload();
            } else {
                alert('Lỗi khi xóa lô hàng');
            }
        })
        .catch(error => console.error('Error:', error));
    }
}

function goToPage(pageNumber) {
    window.location.href = `/pharmacist/import-history?page=${pageNumber}`;
}
