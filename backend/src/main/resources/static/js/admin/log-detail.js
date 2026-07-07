document.addEventListener('DOMContentLoaded', function() {
    console.log('📋 Log detail page loaded');

    // ===== AUTO CLOSE ALERTS =====
    const alerts = document.querySelectorAll('.alert-success, .alert-danger');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(function() {
                alert.style.display = 'none';
            }, 500);
        }, 4500);
    });

    // ===== COPY LOG ID =====
    const logIdElement = document.querySelector('.log-detail-id .value');
    if (logIdElement) {
        logIdElement.style.cursor = 'pointer';
        logIdElement.title = 'Click để copy ID';

        logIdElement.addEventListener('click', function() {
            const text = this.textContent.trim();
            navigator.clipboard.writeText(text).then(function() {
                const originalText = logIdElement.textContent;
                logIdElement.textContent = '✅ Đã copy!';
                logIdElement.style.color = '#10b981';
                setTimeout(function() {
                    logIdElement.textContent = originalText;
                    logIdElement.style.color = '';
                }, 2000);
            }).catch(function() {
                // Fallback
                const range = document.createRange();
                range.selectNode(logIdElement);
                window.getSelection().removeAllRanges();
                window.getSelection().addRange(range);
                document.execCommand('copy');
                window.getSelection().removeAllRanges();

                const originalText = logIdElement.textContent;
                logIdElement.textContent = '✅ Đã copy!';
                logIdElement.style.color = '#10b981';
                setTimeout(function() {
                    logIdElement.textContent = originalText;
                    logIdElement.style.color = '';
                }, 2000);
            });
        });
    }

    // ===== KEYBOARD SHORTCUTS =====
    document.addEventListener('keydown', function(e) {
        // Alt + ← : Quay lại
        if (e.altKey && e.key === 'ArrowLeft') {
            e.preventDefault();
            const backBtn = document.querySelector('.breadcrumb a');
            if (backBtn) {
                window.location.href = backBtn.getAttribute('href');
            }
        }

        // Escape: Quay lại
        if (e.key === 'Escape') {
            const backBtn = document.querySelector('.breadcrumb a');
            if (backBtn) {
                window.location.href = backBtn.getAttribute('href');
            }
        }
    });

    // ===== TIMELINE HOVER EFFECT =====
    const detailItems = document.querySelectorAll('.log-detail-item');
    detailItems.forEach(function(item) {
        item.addEventListener('mouseenter', function() {
            this.style.transition = 'all 0.2s ease';
            this.style.transform = 'translateX(4px)';
        });
        item.addEventListener('mouseleave', function() {
            this.style.transform = 'translateX(0)';
        });
    });

    console.log('✅ Log detail page initialized');
});

// ============================================
// CLOSE ALERT FUNCTION
// ============================================
function closeAlert(button) {
    const alert = button.closest('.alert');
    if (alert) {
        alert.style.transition = 'opacity 0.3s ease';
        alert.style.opacity = '0';
        setTimeout(function() {
            alert.style.display = 'none';
        }, 300);
    }
}