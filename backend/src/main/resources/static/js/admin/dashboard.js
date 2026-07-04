// dashboard.js
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Dashboard loaded');

    // Lấy dữ liệu từ window (đã được truyền từ backend)
    var userRegistrations = window._userData || [];
    var diagnosisSessions = window._sessionData || [];

    console.log('📊 User Registrations:', userRegistrations);
    console.log('📊 Diagnosis Sessions:', diagnosisSessions);
    console.log('📊 User count:', userRegistrations.length);
    console.log('📊 Session count:', diagnosisSessions.length);

    // Vẽ biểu đồ
    createUserChart(userRegistrations);
    createSessionChart(diagnosisSessions);
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

function createSessionChart(data) {
    var canvas = document.getElementById('sessionChart');
    var errorEl = document.getElementById('sessionChartError');

    if (!canvas) {
        console.error('❌ Không tìm thấy canvas sessionChart');
        return;
    }

    if (!data || data.length === 0) {
        console.warn('⚠️ Không có dữ liệu diagnosis sessions');
        canvas.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            var span = errorEl.querySelector('span');
            if (span) span.textContent = 'Chưa có dữ liệu phiên chẩn đoán';
        }
        return;
    }

    var months = [];
    var counts = [];

    data.forEach(function(item) {
        months.push(item.month || '');
        counts.push(item.count || 0);
    });

    console.log('📈 Session Months:', months);
    console.log('📈 Session Counts:', counts);

    var hasData = counts.some(function(c) { return c > 0; });
    if (!hasData) {
        console.warn('⚠️ Không có dữ liệu hợp lệ cho session chart');
        canvas.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            var span = errorEl.querySelector('span');
            if (span) span.textContent = 'Chưa có dữ liệu phiên chẩn đoán';
        }
        return;
    }

    try {
        var ctx = canvas.getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: months,
                datasets: [{
                    label: 'Phiên chẩn đoán',
                    data: counts,
                    backgroundColor: '#8b5cf6',
                    borderColor: '#7c3aed',
                    borderWidth: 2,
                    borderRadius: 8,
                    hoverBackgroundColor: '#7c3aed'
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
                                return context.parsed.y + ' phiên';
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

        console.log('✅ Session chart created successfully');
        canvas.style.display = 'block';
        if (errorEl) errorEl.style.display = 'none';

    } catch (e) {
        console.error('❌ Lỗi tạo session chart:', e);
        canvas.style.display = 'none';
        if (errorEl) {
            errorEl.style.display = 'block';
            var span = errorEl.querySelector('span');
            if (span) span.textContent = '⚠️ Lỗi tạo biểu đồ: ' + e.message;
        }
    }
}