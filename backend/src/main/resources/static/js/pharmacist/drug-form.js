/* Drug Form JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    initializeDrugForm();
});

function initializeDrugForm() {
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', validateDrugForm);
    }
}

function validateDrugForm(e) {
    const fields = [
        { id: 'drugName', label: 'Tên thuốc' },
        { id: 'strength', label: 'Hàm lượng' },
        { id: 'strengthUnit', label: 'Đơn vị hàm lượng' },
        { id: 'dosageForm', label: 'Dạng bào chế' },
        { id: 'routeOfAdministration', label: 'Đường dùng' },
        { id: 'subCategoryId', label: 'Phân nhóm thuốc' },
        { id: 'manufacturer', label: 'Nhà sản xuất' },
        { id: 'countryOfOrigin', label: 'Nước sản xuất' },
        { id: 'storageCondition', label: 'Điều kiện bảo quản' },
        { id: 'shelfLifeMonths', label: 'Thời hạn sử dụng' },
        { id: 'packaging', label: 'Quy cách đóng gói' }
    ];
    let missing = [];
    fields.forEach(f => {
        const el = document.getElementById(f.id);
        if (!el || !el.value || el.value.trim() === '') {
            missing.push(f.label);
        }
    });

    if (missing.length > 0) {
        e.preventDefault();
        alert('Vui lòng điền đầy đủ các trường thông tin bắt buộc: ' + missing.join(', '));
        return false;
    }

    return true;
}

function updateUnitField() {
    // Update available units based on drug selection
}
