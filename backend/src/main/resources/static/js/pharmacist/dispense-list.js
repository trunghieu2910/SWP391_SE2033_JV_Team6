/* Dispense List JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    initializeDispenseList();
});

function initializeDispenseList() {
    // Initialize dispense list page
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
    window.location.href = `/pharmacist/dispense-list?page=${pageNumber}`;
}
