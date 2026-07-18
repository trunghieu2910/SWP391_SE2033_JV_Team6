/* Drug Import JavaScript */

let selectedDrugCode = '';

document.addEventListener('DOMContentLoaded', function() {
    initializeImportForm();
});

function initializeImportForm() {
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', validateImportForm);
    }

    // Set up date validation and auto lot generation
    const mfgDate = document.getElementById('manufactureDate');
    const expDate = document.getElementById('expiryDate');

    if (mfgDate && expDate) {
        mfgDate.addEventListener('change', function() {
            // If expiry date is already set, validate it
            if (expDate.value && new Date(expDate.value) <= new Date(this.value)) {
                alert('Ngày hết hạn phải lớn hơn ngày sản xuất');
                expDate.value = '';
            }
        });

        expDate.addEventListener('change', function() {
            if (mfgDate.value && new Date(this.value) <= new Date(mfgDate.value)) {
                alert('Ngày hết hạn phải lớn hơn ngày sản xuất');
                this.value = '';
            }
        });
    }

    // Close custom dropdown when clicking outside
    document.addEventListener('click', function(event) {
        const wrapper = document.getElementById('drugSelectWrapper');
        const dropdown = document.getElementById('drugDropdownPanel');
        if (wrapper && dropdown && !wrapper.contains(event.target)) {
            dropdown.style.display = 'none';
        }
    });
}

function toggleDrugDropdown() {
    const dropdown = document.getElementById('drugDropdownPanel');
    if (dropdown) {
        const isHidden = dropdown.style.display === 'none';
        dropdown.style.display = isHidden ? 'flex' : 'none';
        if (isHidden) {
            const searchInput = document.getElementById('customDrugSearch');
            if (searchInput) {
                searchInput.value = '';
                searchInput.focus();
                filterDrugOptions();
            }
        }
    }
}

function filterDrugOptions() {
    const searchVal = document.getElementById('customDrugSearch').value.toLowerCase().trim();
    const options = document.querySelectorAll('#customSelectOptionsList .custom-select-option');
    options.forEach(opt => {
        const name = opt.getAttribute('data-name') || '';
        const code = opt.getAttribute('data-code') || '';
        const text = opt.textContent.toLowerCase();
        if (text.includes(searchVal) || name.toLowerCase().includes(searchVal) || code.toLowerCase().includes(searchVal)) {
            opt.style.display = '';
        } else {
            opt.style.display = 'none';
        }
    });
}

function selectDrugOption(element, drugId, drugName, drugCode) {
    const hiddenInput = document.getElementById('drugId');
    const triggerText = document.getElementById('selectedDrugText');
    const dropdown = document.getElementById('drugDropdownPanel');
    
    if (hiddenInput && triggerText) {
        hiddenInput.value = drugId;
        if (drugId) {
            triggerText.textContent = `${drugName} (${drugCode})`;
            selectedDrugCode = drugCode;
        } else {
            triggerText.textContent = '-- Chọn thuốc --';
            selectedDrugCode = '';
        }
        
        // Remove selected class from other options
        const options = document.querySelectorAll('#customSelectOptionsList .custom-select-option');
        options.forEach(opt => opt.classList.remove('selected'));
        if (element) {
            element.classList.add('selected');
        }
        
        // Call update unit or dynamic fields if needed
        updateUnitField();
        
        // Auto generate batch number
        generateBatchNumber();
    }
    
    if (dropdown) {
        dropdown.style.display = 'none';
    }
}

function generateBatchNumber() {
    const batchInput = document.getElementById('batchNumber');
    if (!batchInput) return;

    if (selectedDrugCode) {
        const stt = typeof nextBatchStt !== 'undefined' ? nextBatchStt : '001';
        batchInput.value = `LOT-${stt}-${selectedDrugCode}`;
    } else {
        batchInput.value = '';
    }
}

function updateUnitField() {
    // Update units based on drug selection if needed
}

function validateImportForm(e) {
    const drugId = document.getElementById('drugId').value;
    const batchNumber = document.getElementById('batchNumber').value;
    const mfgDateVal = document.getElementById('manufactureDate').value;
    const expDateVal = document.getElementById('expiryDate').value;
    const unitId = document.getElementById('unitId').value;
    const quantityVal = document.getElementById('quantity').value;
    const importPriceVal = document.getElementById('importPrice').value;
    const supplier = document.getElementById('supplier').value;

    if (!drugId || !batchNumber || !mfgDateVal || !expDateVal || !unitId || !quantityVal || !importPriceVal || !supplier) {
        e.preventDefault();
        alert('Vui lòng điền tất cả các trường bắt buộc (ngoại trừ ghi chú)');
        return false;
    }

    const todayStr = new Date().toISOString().split('T')[0];
    const mfgDate = new Date(mfgDateVal);
    const expDate = new Date(expDateVal);
    const today = new Date(todayStr);

    if (mfgDate >= today) {
        e.preventDefault();
        alert('Ngày sản xuất phải nhỏ hơn ngày hiện tại (ngày trong quá khứ)');
        return false;
    }

    if (expDate <= today) {
        e.preventDefault();
        alert('Hạn sử dụng phải lớn hơn ngày hiện tại (ngày trong tương lai)');
        return false;
    }

    if (expDate <= mfgDate) {
        e.preventDefault();
        alert('Hạn sử dụng phải lớn hơn ngày sản xuất');
        return false;
    }

    const quantity = parseInt(quantityVal, 10);
    const importPrice = parseFloat(importPriceVal);

    if (isNaN(quantity) || quantity <= 0 || !Number.isInteger(quantity)) {
        e.preventDefault();
        alert('Số lượng nhập phải là số nguyên dương lớn hơn 0');
        return false;
    }

    if (isNaN(importPrice) || importPrice <= 0 || !Number.isInteger(importPrice)) {
        e.preventDefault();
        alert('Đơn giá nhập phải là số nguyên dương lớn hơn 0');
        return false;
    }

    return true;
}
