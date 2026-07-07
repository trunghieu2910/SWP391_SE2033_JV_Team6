document.addEventListener('DOMContentLoaded', function() {
    console.log('User detail page loaded');

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

    // ===== CERTIFICATE VIEWER =====
    const certificateLinks = document.querySelectorAll('.btn-view');
    certificateLinks.forEach(function(link) {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const url = this.getAttribute('href');
            if (url) {
                window.open(url, '_blank');
            }
        });
    });

    // ===== TIMELINE ITEMS HOVER EFFECT =====
    const timelineItems = document.querySelectorAll('.timeline-item');
    timelineItems.forEach(function(item) {
        item.addEventListener('mouseenter', function() {
            this.style.transform = 'translateX(4px)';
        });
        item.addEventListener('mouseleave', function() {
            this.style.transform = 'translateX(0)';
        });
    });

    // ===== COPY USER ID TO CLIPBOARD =====
    const userIdElements = document.querySelectorAll('.detail-item .detail-value');
    userIdElements.forEach(function(el) {
        const label = el.closest('.detail-item').querySelector('.detail-label');
        if (label && label.textContent.includes('ID người dùng')) {
            el.style.cursor = 'pointer';
            el.title = 'Click để copy ID';
            el.addEventListener('click', function() {
                const text = this.textContent.trim();
                navigator.clipboard.writeText(text).then(function() {
                    // Tạo tooltip copy thành công
                    const originalText = el.textContent;
                    el.textContent = '✅ Đã copy!';
                    el.style.color = '#10b981';
                    setTimeout(function() {
                        el.textContent = originalText;
                        el.style.color = '';
                    }, 2000);
                }).catch(function() {
                    // Fallback cho trình duyệt không hỗ trợ
                    const range = document.createRange();
                    range.selectNode(el);
                    window.getSelection().removeAllRanges();
                    window.getSelection().addRange(range);
                    document.execCommand('copy');
                    window.getSelection().removeAllRanges();

                    const originalText = el.textContent;
                    el.textContent = 'Đã copy!';
                    el.style.color = '#10b981';
                    setTimeout(function() {
                        el.textContent = originalText;
                        el.style.color = '';
                    }, 2000);
                });
            });
        }
    });

    // ===== PHONE NUMBER FORMAT (Optional) =====
    const phoneElement = document.querySelector('.detail-item .detail-value');
    if (phoneElement) {
        const label = phoneElement.closest('.detail-item').querySelector('.detail-label');
        if (label && label.textContent.includes('Số điện thoại')) {
            const phone = phoneElement.textContent.trim();
            if (phone && phone !== 'Chưa cập nhật' && phone !== 'N/A') {
                // Format phone number if needed
                // Example: 0987654321 -> 0987 654 321
                if (phone.length === 10) {
                    const formatted = phone.replace(/(\d{4})(\d{3})(\d{3})/, '$1 $2 $3');
                    phoneElement.textContent = formatted;
                }
            }
        }
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
    });
});

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