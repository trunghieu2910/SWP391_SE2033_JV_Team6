/* Inventory JavaScript */

let currentBatchId = null;

document.addEventListener('DOMContentLoaded', function() {
    initializeInventory();
});

function initializeInventory() {
    // Initialize inventory page
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
