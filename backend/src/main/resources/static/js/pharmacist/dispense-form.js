/* Dispense Form JavaScript - Dynamic batch loading from API */

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM Content Loaded - Initializing dispense form');
    initializeDispenseForm();
});

function initializeDispenseForm() {
    console.log('Initializing dispense form...');
    const batchSelect = document.getElementById('batchId');
    const drugIdInput = document.getElementById('drugId');
    
    console.log('batchSelect found:', !!batchSelect);
    console.log('drugIdInput found:', !!drugIdInput);
    
    if (drugIdInput) {
        console.log('drugIdInput value:', drugIdInput.value);
    }
    
    if (batchSelect) {
        batchSelect.addEventListener('change', updateBatchInfo);
        loadBatches();
    } else {
        console.error('Could not find batchSelect element');
    }

    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', validateDispenseForm);
    }
}

// Luu thong tin cac lo hang de hien thi khi chon
let batchData = {};

function loadBatches() {
    const drugIdInput = document.getElementById('drugId');
    const batchSelect = document.getElementById('batchId');
    const debugDrugIdEl = document.getElementById('debugDrugId');

    console.log('loadBatches called');
    console.log('drugIdInput element:', drugIdInput);
    console.log('batchSelect element:', batchSelect);

    if (!batchSelect) {
        console.error('batchSelect element not found');
        return;
    }

    let drugId = null;
    
    // Try multiple ways to get drugId
    if (drugIdInput) {
        // Try value attribute
        drugId = drugIdInput.value || drugIdInput.getAttribute('value');
        console.log('drugId from value:', drugId);
        
        // Try data attribute
        if (!drugId || drugId === '') {
            drugId = drugIdInput.getAttribute('data-drug-id');
            console.log('drugId from data attribute:', drugId);
        }
    }
    
    drugId = drugId ? drugId.trim() : null;
    
    console.log('Final drugId extracted:', drugId);
    console.log('drugId type:', typeof drugId);
    console.log('drugId is empty:', !drugId || drugId === '' || drugId === 'null');

    // Update debug display
    if (debugDrugIdEl) {
        debugDrugIdEl.textContent = drugId || 'NOT FOUND';
    }

    if (!drugId || drugId === '' || drugId === 'null' || drugId === '0') {
        console.error('drugId is empty, null, or invalid');
        batchSelect.innerHTML = '<option value="">-- Lỗi: Không có dữ liệu thuốc (drugId=' + drugId + ') --</option>';
        return;
    }

    console.log('Loading batches for drugId: ' + drugId);

    // Goi API lay danh sach lo hang con hang
    fetch('/pharmacist/api/batches?drugId=' + encodeURIComponent(drugId))
        .then(function(response) {
            console.log('API Response status:', response.status);
            console.log('API Response headers:', response.headers.get('content-type'));
            
            if (!response.ok) {
                throw new Error('API Error ' + response.status + ': ' + response.statusText);
            }
            return response.json();
        })
        .then(function(batches) {
            console.log('Batches received from API:', batches);
            console.log('Batches is array:', Array.isArray(batches));
            console.log('Batches is object:', typeof batches === 'object');
            console.log('Batches length:', batches ? (Array.isArray(batches) ? batches.length : 'not-array') : 'null');
            
            // Check if response is error object
            if (batches && batches.error) {
                throw new Error(batches.error);
            }
            
            batchData = {};
            batchSelect.innerHTML = '<option value="">-- Chọn lô hàng --</option>';

            if (!batches || !Array.isArray(batches) || batches.length === 0) {
                console.info('No batches available for this drug');
                const opt = document.createElement('option');
                opt.disabled = true;
                opt.textContent = 'Không có lô hàng khả dụng';
                batchSelect.appendChild(opt);
                return;
            }

            console.log('Adding ' + batches.length + ' batch options');
            batches.forEach(function(batch, index) {
                console.log('Processing batch ' + index + ':', batch);
                
                if (!batch || !batch.batchId) {
                    console.warn('Batch missing required fields:', batch);
                    return;
                }
                
                // Luu du lieu batch de tra cuu nhanh
                batchData[batch.batchId] = batch;

                const option = document.createElement('option');
                option.value = batch.batchId;
                option.textContent = batch.batchNumber
                    + ' | HSD: ' + formatDate(batch.expiryDate)
                    + ' | Tồn: ' + batch.quantityInStock;
                batchSelect.appendChild(option);
            });
            console.log('Batch options added successfully. Total options:', batchSelect.options.length);
        })
        .catch(function(err) {
            console.error('Lỗi khi tải lô hàng:', err);
            console.error('Error message:', err.message);
            console.error('Error stack:', err.stack);
            batchSelect.innerHTML = '<option value="">-- LỖI: ' + err.message + ' --</option>';
        });
}

function updateBatchInfo() {
    const batchId = document.getElementById('batchId').value;
    const expiryEl = document.getElementById('expiryDate');
    const stockEl = document.getElementById('stockQuantity');

    if (!batchId || !batchData[batchId]) {
        if (expiryEl) expiryEl.textContent = '-';
        if (stockEl) stockEl.textContent = '-';
        return;
    }

    const batch = batchData[batchId];
    if (expiryEl) expiryEl.textContent = formatDate(batch.expiryDate);
    if (stockEl) stockEl.textContent = batch.quantityInStock + ' don vi';
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    // dateStr co the la mang [yyyy, mm, dd] hoac chuoi 'yyyy-mm-dd'
    if (Array.isArray(dateStr)) {
        return dateStr[2] + '/' + String(dateStr[1]).padStart(2, '0') + '/' + dateStr[0];
    }
    const parts = dateStr.split('-');
    if (parts.length === 3) {
        return parts[2] + '/' + parts[1] + '/' + parts[0];
    }
    return dateStr;
}

function validateDispenseForm(e) {
    const batchId = document.getElementById('batchId').value;
    const quantity = parseInt(document.getElementById('quantityDispensed').value, 10);

    if (!batchId) {
        e.preventDefault();
        alert('Vui long chon lo hang truoc khi cap phat.');
        return false;
    }

    if (!quantity || quantity <= 0) {
        e.preventDefault();
        alert('So luong cap phat phai lon hon 0.');
        return false;
    }

    // Kiem tra khong vuot qua ton kho hien tai
    const batch = batchData[batchId];
    if (batch && quantity > batch.quantityInStock) {
        e.preventDefault();
        alert('So luong cap phat (' + quantity + ') vuot qua ton kho (' + batch.quantityInStock + ').');
        return false;
    }

    return true;
}
