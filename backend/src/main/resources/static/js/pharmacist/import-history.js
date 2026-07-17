/* Import History JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    initializeImportHistory();
});

function initializeImportHistory() {
    // Initialize import history page
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
