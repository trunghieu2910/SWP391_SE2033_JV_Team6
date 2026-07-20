/* Drug Detail JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    initializeDrugDetail();
});

function initializeDrugDetail() {
    // Initialize drug detail page
}

function updateDrug(drugId) {
    window.location.href = `/pharmacist/drug-update/${drugId}`;
}

function toggleDrugStatus(drugId, currentStatus) {
    const newStatus = currentStatus === 1 ? 0 : 1;
    const statusText = newStatus === 1 ? 'đang dùng' : 'ngừng';
    
    if (confirm(`Bạn chắc chắn muốn chuyển trạng thái thuốc này thành ${statusText}?`)) {
        fetch(`/pharmacist/drug-status/${drugId}?status=${newStatus}`, {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                [document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN']:
                    document.querySelector('meta[name="_csrf"]')?.content || ''
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                location.reload();
            } else {
                alert('Lỗi: ' + data.error);
            }
        })
        .catch(error => console.error('Error:', error));
    }
}

function viewBatchDetail(batchId) {
    window.location.href = `/pharmacist/batch-detail/${batchId}`;
}
