/* Dispense List JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    initializeDispenseList();
});

function initializeDispenseList() {
    // Initialize dispense list page
    initializeDispenseFilters();
}

function initializeDispenseFilters() {
    const controls = [
        'dispenseSearchInput',
        'dispenseStatusFilter',
        'dispenseDateFrom',
        'dispenseDateTo'
    ];

    controls.forEach(id => {
        const control = document.getElementById(id);
        if (!control) return;

        const eventName = control.tagName === 'SELECT' || control.type === 'date' ? 'change' : 'input';
        control.addEventListener(eventName, applyDispenseFilters);
    });

    const clearButton = document.getElementById('dispenseClearFilter');
    if (clearButton) {
        clearButton.addEventListener('click', clearDispenseFilters);
    }

    const tableBody = document.getElementById('dispenseListTableBody');
    if (tableBody) {
        Array.from(tableBody.querySelectorAll('tr'))
            .filter(row => row.id !== 'dispenseListEmptyRow' && row.querySelectorAll('td').length === 6)
            .forEach(row => {
                const cells = row.querySelectorAll('td');
                row.dataset.patientSearch = normalizeDispenseText(cells[1]?.textContent || '');
                row.dataset.prescriptionDate = String(parseDispenseDisplayDate(cells[2]?.textContent || ''));
                row.dataset.status = normalizeDispenseText(cells[5]?.textContent || '').includes('cho cap phat')
                    ? 'pending'
                    : 'completed';
            });
    }

    applyDispenseFilters();
}

function applyDispenseFilters() {
    const keyword = normalizeDispenseText(document.getElementById('dispenseSearchInput')?.value || '');
    const status = document.getElementById('dispenseStatusFilter')?.value || '';
    const fromDate = parseDispenseInputDate(document.getElementById('dispenseDateFrom')?.value || '');
    const toDate = parseDispenseInputDate(document.getElementById('dispenseDateTo')?.value || '');
    const tableBody = document.getElementById('dispenseListTableBody');
    const emptyRow = document.getElementById('dispenseListEmptyRow');

    if (!tableBody) return;

    const rows = Array.from(tableBody.querySelectorAll('tr'))
        .filter(row => row.id !== 'dispenseListEmptyRow' && row.dataset.status);
    let visibleCount = 0;

    rows.forEach(row => {
        const rowDate = Number(row.dataset.prescriptionDate);
        const matchKeyword = !keyword || row.dataset.patientSearch.includes(keyword);
        const matchStatus = !status || row.dataset.status === status;
        const matchFrom = !fromDate || rowDate >= fromDate;
        const matchTo = !toDate || rowDate <= toDate;
        const visible = matchKeyword && matchStatus && matchFrom && matchTo;

        row.style.display = visible ? '' : 'none';
        if (visible) visibleCount++;
    });

    if (emptyRow) {
        emptyRow.style.display = rows.length > 0 && visibleCount === 0 ? '' : 'none';
    }
}

function clearDispenseFilters() {
    ['dispenseSearchInput', 'dispenseStatusFilter', 'dispenseDateFrom', 'dispenseDateTo']
        .forEach(id => {
            const control = document.getElementById(id);
            if (control) control.value = '';
        });
    applyDispenseFilters();
}

function normalizeDispenseText(value) {
    return value
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .trim();
}

function parseDispenseDisplayDate(value) {
    const match = value.trim().match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
    if (!match) return 0;
    return new Date(Number(match[3]), Number(match[2]) - 1, Number(match[1])).setHours(0, 0, 0, 0);
}

function parseDispenseInputDate(value) {
    if (!value) return 0;
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return 0;
    return date.setHours(0, 0, 0, 0);
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
