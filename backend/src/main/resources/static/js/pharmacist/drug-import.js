/* Drug Import JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    initializeImportForm();
});

function initializeImportForm() {
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', validateImportForm);
    }

    // Load drugs
    loadDrugsList();

    // Set up date validation
    const mfgDate = document.getElementById('manufactureDate');
    const expDate = document.getElementById('expiryDate');

    if (mfgDate && expDate) {
        expDate.addEventListener('change', function() {
            if (new Date(this.value) <= new Date(mfgDate.value)) {
                alert('Ngày hết hạn phải lớn hơn ngày sản xuất');
                this.value = '';
            }
        });
    }
}

function loadDrugsList() {
    const drugSelect = document.getElementById('drugId');
    if (drugSelect) {
        // This would normally fetch from API
        const drugs = [
            { value: 1, text: 'Aspirin 500mg' },
            { value: 2, text: 'Paracetamol 500mg' },
            { value: 3, text: 'Amoxicillin 250mg' },
            { value: 4, text: 'Ibuprofen 200mg' }
        ];

        drugs.forEach(drug => {
            const option = document.createElement('option');
            option.value = drug.value;
            option.textContent = drug.text;
            drugSelect.appendChild(option);
        });
    }
}

function updateUnitField() {
    // Update units based on drug selection
}

function validateImportForm(e) {
    const drugId = document.getElementById('drugId').value;
    const batchNumber = document.getElementById('batchNumber').value;
    const mfgDate = document.getElementById('manufactureDate').value;
    const expDate = document.getElementById('expiryDate').value;
    const quantity = document.getElementById('quantity').value;

    if (!drugId || !batchNumber || !mfgDate || !expDate || !quantity) {
        e.preventDefault();
        alert('Vui lòng điền tất cả các trường bắt buộc');
        return false;
    }

    if (new Date(expDate) <= new Date(mfgDate)) {
        e.preventDefault();
        alert('Ngày hết hạn phải lớn hơn ngày sản xuất');
        return false;
    }

    return true;
}
