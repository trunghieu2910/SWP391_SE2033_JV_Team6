function setFilterDates(period) {
    const today = new Date();
    let startDate = new Date();
    let endDate = new Date();

    if (period === 'thisMonth') {
        startDate = new Date(today.getFullYear(), today.getMonth(), 1);
        endDate = new Date(today.getFullYear(), today.getMonth() + 1, 0);
    } else if (period === 'lastMonth') {
        startDate = new Date(today.getFullYear(), today.getMonth() - 1, 1);
        endDate = new Date(today.getFullYear(), today.getMonth(), 0);
    }

    // Format YYYY-MM-DD
    const format = (date) => {
        const d = date.getDate().toString().padStart(2, '0');
        const m = (date.getMonth() + 1).toString().padStart(2, '0');
        const y = date.getFullYear();
        return `${y}-${m}-${d}`;
    };

    document.getElementById('startDate').value = format(startDate);
    document.getElementById('endDate').value = format(endDate);
    document.getElementById('filterForm').submit();
}
