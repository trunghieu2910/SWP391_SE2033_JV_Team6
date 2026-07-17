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
