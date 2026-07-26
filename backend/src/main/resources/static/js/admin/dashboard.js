document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Dashboard loaded');

    // Lấy dữ liệu từ window (đã được truyền từ backend)
    var userRegistrations = window._userData || [];
    var requestTrends = window._requestData || [];

    console.log('📊 User Registrations:', userRegistrations);
    console.log('📊 Request Trends:', requestTrends);
    console.log('📊 User count:', userRegistrations.length);
    console.log('📊 Request count:', requestTrends.length);

    // Vẽ biểu đồ
    createUserChart(userRegistrations);
    createRequestChart(requestTrends);
});

function createUserChart(data) {
    var canvas = document.getElementById('userChart');
    var errorEl = document.getElementById('userChartError');

    if (!canvas) {
        console.error('❌ Không tìm thấy canvas userChart');
        return;
    }

    // Kiểm tra dữ liệu
    if (!data || data.length === 0) {
        console.warn('⚠️ Không có dữ liệu user registrations');
        canvas.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            var span = errorEl.querySelector('span');
            if (span) span.textContent = 'Chưa có dữ liệu đăng ký người dùng';
        }
        return;
    }

    // Lấy tháng và số lượng
    var months = [];
    var counts = [];

    data.forEach(function(item) {
        months.push(item.month || '');
        counts.push(item.count || 0);
    });

    console.log('📈 User Months:', months);
    console.log('📈 User Counts:', counts);

    // Kiểm tra có dữ liệu hợp lệ
    var hasData = counts.some(function(c) { return c > 0; });
    if (!hasData) {
        console.warn('⚠️ Không có dữ liệu hợp lệ cho user chart');
        canvas.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            var span = errorEl.querySelector('span');
            if (span) span.textContent = 'Chưa có dữ liệu đăng ký người dùng';
        }
        return;
    }

    try {
        var ctx = canvas.getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: months,
                datasets: [{
                    label: 'Người dùng mới',
                    data: counts,
                    borderColor: '#100357',
                    backgroundColor: 'rgba(16, 3, 87, 0.1)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.3,
                    pointBackgroundColor: '#100357',
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 2,
                    pointRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.parsed.y + ' người dùng';
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                            font: { size: 11 }
                        },
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { font: { size: 11 } }
                    }
                }
            }
        });

        console.log('✅ User chart created successfully');
        canvas.style.display = 'block';
        if (errorEl) errorEl.style.display = 'none';

    } catch (e) {
        console.error('❌ Lỗi tạo user chart:', e);
        canvas.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            var span = errorEl.querySelector('span');
            if (span) span.textContent = '⚠️ Lỗi tạo biểu đồ: ' + e.message;
        }
    }
}

function createRequestChart(data) {
    var canvas = document.getElementById('requestChart');
    var errorEl = document.getElementById('requestChartError');

    if (!canvas) {
        console.error('❌ Không tìm thấy canvas requestChart');
        return;
    }

    if (!data || data.length === 0) {
        console.warn('⚠️ Không có dữ liệu access trends');
        canvas.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            var span = errorEl.querySelector('span');
            if (span) span.textContent = 'Chưa có dữ liệu truy cập';
        }
        return;
    }

    var months = [];
    var counts = [];

    data.forEach(function(item) {
        months.push(item.month || '');
        counts.push(item.count || 0);
    });

    console.log('📈 Request Months:', months);
    console.log('📈 Request Counts:', counts);

    var hasData = counts.some(function(c) { return c > 0; });
    if (!hasData) {
        console.warn('⚠️ Không có dữ liệu hợp lệ cho request chart');
        canvas.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            var span = errorEl.querySelector('span');
            if (span) span.textContent = 'Chưa có dữ liệu truy cập';
        }
        return;
    }

    try {
        var ctx = canvas.getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: months,
                datasets: [{
                    label: 'Lượt truy cập',
                    data: counts,
                    borderColor: '#8b5cf6',
                    backgroundColor: 'rgba(139, 92, 246, 0.1)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.3,
                    pointBackgroundColor: '#8b5cf6',
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 2,
                    pointRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.parsed.y + ' lượt truy cập';
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                            font: { size: 11 }
                        },
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { font: { size: 11 } }
                    }
                }
            }
        });

        console.log('✅ Request chart created successfully');
        canvas.style.display = 'block';
        if (errorEl) errorEl.style.display = 'none';

    } catch (e) {
        console.error('❌ Lỗi tạo request chart:', e);
        canvas.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            var span = errorEl.querySelector('span');
            if (span) span.textContent = '⚠️ Lỗi tạo biểu đồ: ' + e.message;
        }
    }
}