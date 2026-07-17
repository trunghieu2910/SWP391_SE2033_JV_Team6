/* Drug Form JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    initializeDrugForm();
});

function initializeDrugForm() {
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', validateDrugForm);
    }

    // Load subcategories
    loadSubcategories();
}

function loadSubcategories() {
    const subCategorySelect = document.getElementById('subCategoryId');
    if (subCategorySelect) {
        // This would normally fetch from API
        // For now, we'll use hardcoded values
        const options = [
            { value: 1, text: 'Thuốc kháng sinh' },
            { value: 2, text: 'Thuốc an thần' },
            { value: 3, text: 'Thuốc hạ sốt' },
            { value: 4, text: 'Thuốc ho' },
            { value: 5, text: 'Thuốc tiêu hóa' }
        ];

        options.forEach(option => {
            const optElement = document.createElement('option');
            optElement.value = option.value;
            optElement.textContent = option.text;
            subCategorySelect.appendChild(optElement);
        });
    }
}

function validateDrugForm(e) {
    const drugCode = document.getElementById('drugCode').value;
    const drugName = document.getElementById('drugName').value;
    const strength = document.getElementById('strength').value;
    const dosageForm = document.getElementById('dosageForm').value;

    if (!drugCode || !drugName || !strength || !dosageForm) {
        e.preventDefault();
        alert('Vui lòng điền tất cả các trường bắt buộc');
        return false;
    }

    return true;
}

function updateUnitField() {
    // Update available units based on drug selection
}
