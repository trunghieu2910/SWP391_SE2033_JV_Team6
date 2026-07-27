/* Dispense List JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    initializeDispenseList();
});

function initializeDispenseList() {
    const clearButton = document.getElementById('dispenseClearFilter');
    if (clearButton) {
        clearButton.addEventListener('click', function() {
            window.location.href = '/pharmacist/dispense-list';
        });
    }
}

function dispenseDrug(detailId) {
    window.location.href = `/pharmacist/dispense-form/${detailId}`;
}

function printPrescription(prescriptionId) {
    const printWindow = window.open(`/pharmacist/print-prescription/${prescriptionId}`, 'print');
    printWindow.addEventListener('load', function() {
        printWindow.print();
    });
}

function goToPage(pageNumber) {
    const urlParams = new URLSearchParams(window.location.search);
    urlParams.set('page', pageNumber);
    window.location.href = `/pharmacist/dispense-list?${urlParams.toString()}`;
}
