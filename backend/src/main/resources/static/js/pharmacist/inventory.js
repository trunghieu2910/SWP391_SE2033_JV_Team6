/* Inventory JavaScript */

let currentBatchId = null;

document.addEventListener('DOMContentLoaded', function() {
    initializeInventory();
});

function initializeInventory() {
    // Initialize inventory page
    initializeInventorySearchAndSort();

    const modal = document.getElementById('adjustModal');
    const closeBtn = document.querySelector('.close');

    if (closeBtn) {
        closeBtn.addEventListener('click', closeAdjustModal);
    }

    if (modal) {
        window.addEventListener('click', function(event) {
            if (event.target === modal) {
                closeAdjustModal();
            }
        });
    }

    const form = document.getElementById('adjustForm');
    if (form) {
        form.addEventListener('submit', submitAdjustment);
    }
}

function initializeInventorySearchAndSort() {
    const searchInput = document.getElementById('inventorySearchInput');
    const sortSelect = document.getElementById('inventorySortSelect');
    const tableBody = document.getElementById('inventoryTableBody');

    if (!tableBody) return;

    const rows = Array.from(tableBody.querySelectorAll('tr'))
        .filter(row => row.id !== 'inventoryEmptyRow');

    rows.forEach((row, index) => {
        const cells = row.querySelectorAll('td');
        row.dataset.originalIndex = String(index);
        row.dataset.drugName = normalizeInventoryText(cells[1]?.textContent || '');
        row.dataset.batchNumber = normalizeInventoryText(cells[2]?.textContent || '');
        row.dataset.expiryTime = String(parseInventoryDate(cells[3]?.textContent || ''));
        row.dataset.stock = String(parseInventoryNumber(cells[5]?.textContent || ''));
        row.dataset.smallUnit = normalizeInventoryText(row.getAttribute('data-small-unit') || '');
    });

    if (sortSelect) {
        sortSelect.addEventListener('change', applyInventorySearchAndSort);
    }

    applyInventorySearchAndSort();
}

function applyInventorySearchAndSort() {
    const searchInput = document.getElementById('inventorySearchInput');
    const sortSelect = document.getElementById('inventorySortSelect');
    const tableBody = document.getElementById('inventoryTableBody');
    const emptyRow = document.getElementById('inventoryEmptyRow');

    if (!tableBody) return;

    const sortMode = sortSelect?.value || '';
    
    const rows = Array.from(tableBody.querySelectorAll('tr'))
        .filter(row => row.id !== 'inventoryEmptyRow');

    rows.forEach(row => {
        row.dataset.visible = 'true';
    });

    rows.sort((a, b) => compareInventoryRows(a, b, sortMode));
    rows.forEach(row => {
        row.style.display = row.dataset.visible === 'true' ? '' : 'none';
        tableBody.appendChild(row);
    });

    const visibleCount = rows.filter(row => row.dataset.visible === 'true').length;
    if (emptyRow) {
        emptyRow.style.display = visibleCount === 0 ? '' : 'none';
        tableBody.appendChild(emptyRow);
    }
}

function compareInventoryRows(a, b, sortMode) {
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

function normalizeInventoryText(value) {
    return value
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .trim();
}

function parseInventoryDate(value) {
    const match = value.trim().match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
    if (!match) return 0;

    const day = Number(match[1]);
    const month = Number(match[2]) - 1;
    const year = Number(match[3]);
    return new Date(year, month, day).getTime();
}

function parseInventoryNumber(value) {
    const match = value.replace(/[^\d-]/g, '');
    return match ? Number(match) : 0;
}

function adjustInventory(batchId) {
    currentBatchId = batchId;
    const modal = document.getElementById('adjustModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

function closeAdjustModal() {
    const modal = document.getElementById('adjustModal');
    if (modal) {
        modal.style.display = 'none';
    }
    currentBatchId = null;
}

function submitAdjustment(e) {
    e.preventDefault();
    
    if (!currentBatchId) {
        alert('Lỗi: Không tìm thấy lô hàng');
        return;
    }

    const quantityChange = document.getElementById('quantityChange').value;
    const reason = document.getElementById('reason').value;

    if (!quantityChange || !reason) {
        alert('Vui lòng điền tất cả các trường');
        return;
    }

    fetch(`/pharmacist/inventory-adjust/${currentBatchId}?quantityChange=${quantityChange}&reason=${reason}`, {
        method: 'POST',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            closeAdjustModal();
            location.reload();
        } else {
            alert('Lỗi: ' + data.error);
        }
    })
    .catch(error => console.error('Error:', error));
}

function goToPage(pageNumber) {
    window.location.href = `/pharmacist/inventory?page=${pageNumber}`;
}
