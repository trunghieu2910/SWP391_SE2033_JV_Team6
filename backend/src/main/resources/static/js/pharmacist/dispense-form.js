/* Dispense Form JavaScript - Dynamic batch loading from API */

document.addEventListener('DOMContentLoaded', function() {
    initializeDispenseForm();
});

function initializeDispenseForm() {
    const batchSelect = document.getElementById('batchId');
    if (batchSelect) {
        batchSelect.addEventListener('change', updateBatchInfo);
        loadBatches();
    }

    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', validateDispenseForm);
    }
}

// Luu thong tin cac lo hang de hien thi khi chon
let batchData = {};

function loadBatches() {
    const drugId = document.getElementById('drugId') ? document.getElementById('drugId').value : null;
    const batchSelect = document.getElementById('batchId');

    if (!drugId || !batchSelect) {
        console.warn('Khong tim thay drugId hoac batchSelect.');
        return;
    }

    // Goi API lay danh sach lo hang con hang
    fetch('/pharmacist/api/batches?drugId=' + drugId)
        .then(function(response) {
            if (!response.ok) throw new Error('Loi server: ' + response.status);
            return response.json();
        })
        .then(function(batches) {
            batchData = {};
            batchSelect.innerHTML = '<option value="">-- Chon lo hang --</option>';

            if (!batches || batches.length === 0) {
                const opt = document.createElement('option');
                opt.disabled = true;
                opt.textContent = 'Khong co lo hang kha dung';
                batchSelect.appendChild(opt);
                return;
            }

            batches.forEach(function(batch) {
                // Luu du lieu batch de tra cuu nhanh
                batchData[batch.batchId] = batch;

                const option = document.createElement('option');
                option.value = batch.batchId;
                option.textContent = batch.batchNumber
                    + ' | HSD: ' + formatDate(batch.expiryDate)
                    + ' | Ton: ' + batch.quantityInStock;
                batchSelect.appendChild(option);
            });
        })
        .catch(function(err) {
            console.error('Loi khi tai lo hang:', err);
            batchSelect.innerHTML = '<option value="">-- Loi tai du lieu --</option>';
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
