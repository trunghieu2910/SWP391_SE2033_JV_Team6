// session-detail.js

document.addEventListener('DOMContentLoaded', function() {

    // ============================================
    // 1. AUTO CLOSE ALERT AFTER 5 SECONDS
    // ============================================
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

    // ============================================
    // 2. ACCORDION TOGGLE
    // ============================================
    window.toggleAccordion = function(id) {
        const body = document.getElementById(id);
        const icon = document.getElementById(id.replace('Accordion', 'Icon'));

        if (body.style.display === 'none') {
            body.style.display = 'block';
            if (icon) icon.className = 'fa-solid fa-chevron-up accordion-icon';
        } else {
            body.style.display = 'none';
            if (icon) icon.className = 'fa-solid fa-chevron-down accordion-icon';
        }
    };

    // ============================================
    // 3. SYMPTOM FORM - EDIT / CANCEL
    // ============================================
    window.editSymptoms = function() {
        const view = document.getElementById('symptomView');
        const edit = document.getElementById('symptomEdit');
        if (view) view.style.display = 'none';
        if (edit) edit.style.display = 'block';
    };

    window.cancelEditSymptoms = function() {
        const view = document.getElementById('symptomView');
        const edit = document.getElementById('symptomEdit');
        if (view) view.style.display = 'block';
        if (edit) edit.style.display = 'none';
    };

    // ============================================
    // 4. SYMPTOM FORM - CONFIRM WITH CUSTOM MODAL
    // ============================================
    const symptomForm = document.getElementById('symptomForm');
    const confirmModal = document.getElementById('confirmModal');
    const confirmModalTitle = document.getElementById('confirmModalTitle');
    const confirmModalMessage = document.getElementById('confirmModalMessage');
    const confirmModalOkBtn = document.getElementById('confirmModalOkBtn');

    if (symptomForm) {
        symptomForm.addEventListener('submit', function(e) {
            e.preventDefault();

            confirmModalTitle.textContent = 'Xác nhận lưu triệu chứng';
            confirmModalMessage.textContent = 'Bạn có chắc chắn muốn lưu các thay đổi về triệu chứng lâm sàng?';

            confirmModalOkBtn.onclick = function() {
                symptomForm.submit();
                closeConfirmModal();
            };

            openConfirmModal();
        });
    }

    // ============================================
    // 5. CONFIRM MODAL FUNCTIONS
    // ============================================
    function openConfirmModal() {
        const modal = document.getElementById('confirmModal');
        if (modal) {
            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        }
    }

    window.closeConfirmModal = function() {
        const modal = document.getElementById('confirmModal');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = '';
        }
    };

    document.addEventListener('click', function(e) {
        const modal = document.getElementById('confirmModal');
        if (e.target === modal) {
            closeConfirmModal();
        }
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeConfirmModal();
        }
    });

    // ============================================
    // 6. STATUS MODAL FUNCTIONS
    // ============================================
    let selectedStatusSessionId = null;
    let selectedStatusCurrent = null;

    window.openStatusModal = function() {
        const modal = document.getElementById('statusModal');
        const sessionId = document.getElementById('statusSessionId');
        const statusSelect = document.getElementById('statusSelect');

        if (modal) {
            // Lấy sessionId từ DOM
            const sessionIdText = sessionId ? sessionId.textContent : '';
            selectedStatusSessionId = sessionIdText;

            // Lưu giá trị hiện tại
            selectedStatusCurrent = statusSelect ? statusSelect.value : '';

            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        }
    };

    window.closeStatusModal = function() {
        const modal = document.getElementById('statusModal');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = '';
        }
    };

    window.confirmStatusUpdate = function() {
        const statusSelect = document.getElementById('statusSelect');
        const newStatus = statusSelect ? statusSelect.value : '';

        if (!newStatus) {
            alert('Vui lòng chọn trạng thái mới.');
            return;
        }

        if (newStatus === selectedStatusCurrent) {
            alert('Trạng thái mới trùng với trạng thái hiện tại.');
            return;
        }

        // Tạo form và submit
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/doctor/sessions/' + selectedStatusSessionId + '/status';

        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'status';
        input.value = newStatus;

        form.appendChild(input);
        document.body.appendChild(form);
        form.submit();

        closeStatusModal();
    };

    // Close status modal when clicking outside
    document.addEventListener('click', function(e) {
        const modal = document.getElementById('statusModal');
        if (e.target === modal) {
            closeStatusModal();
        }
    });

    // ============================================
    // 7. SHARE MODAL FUNCTIONS
    // ============================================
    let selectedShareSessionId = null;
    let selectedShareCurrent = null;

    window.openShareModal = function() {
        const modal = document.getElementById('shareModal');
        const sessionId = document.getElementById('shareSessionId');
        const message = document.getElementById('shareMessage');
        const actionText = document.getElementById('shareActionText');
        const warningText = document.getElementById('shareWarningText');
        const confirmBtn = document.getElementById('shareConfirmBtn');
        const btnText = document.getElementById('shareBtnText');

        if (modal) {
            // Lấy sessionId
            const sessionIdText = sessionId ? sessionId.textContent : '';
            selectedShareSessionId = sessionIdText;

            // Xác định trạng thái hiện tại
            const isShared = document.querySelector('.badge-shared') !== null;
            selectedShareCurrent = isShared;

            if (isShared) {
                // Đang công bố -> gỡ công bố
                actionText.textContent = 'gỡ công bố';
                warningText.textContent = 'Sau khi gỡ công bố, bệnh nhân và các bác sĩ khác sẽ không thể xem ca chẩn đoán này.';
                btnText.textContent = 'Gỡ công bố';
                confirmBtn.className = 'btn-action btn-danger';
            } else {
                // Chưa công bố -> công bố
                actionText.textContent = 'công bố';
                warningText.textContent = 'Sau khi công bố, bệnh nhân và các bác sĩ khác có thể xem ca chẩn đoán này.';
                btnText.textContent = 'Công bố';
                confirmBtn.className = 'btn-action btn-primary';
            }

            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        }
    };

    window.closeShareModal = function() {
        const modal = document.getElementById('shareModal');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = '';
        }
    };

    window.confirmShareUpdate = function() {
        const isShared = !selectedShareCurrent;

        // Tạo form và submit
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/doctor/sessions/' + selectedShareSessionId + '/share';

        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'isShared';
        input.value = isShared;

        form.appendChild(input);
        document.body.appendChild(form);
        form.submit();

        closeShareModal();
    };

    // Close share modal when clicking outside
    document.addEventListener('click', function(e) {
        const modal = document.getElementById('shareModal');
        if (e.target === modal) {
            closeShareModal();
        }
    });

    // ============================================
    // 8. ADD LAB MODAL FUNCTIONS
    // ============================================
    window.openAddLabModal = function() {
        const modal = document.getElementById('addLabModal');
        if (modal) {
            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        }
    };

    window.closeAddLabModal = function() {
        const modal = document.getElementById('addLabModal');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = '';
            // Reset form
            const form = document.getElementById('addLabForm');
            if (form) form.reset();
        }
    };

    // Close add lab modal when clicking outside
    document.addEventListener('click', function(e) {
        const modal = document.getElementById('addLabModal');
        if (e.target === modal) {
            closeAddLabModal();
        }
    });

    // ============================================
    // 9. ADD LAB FORM SUBMIT
    // ============================================
    const addLabForm = document.getElementById('addLabForm');
    if (addLabForm) {
        addLabForm.addEventListener('submit', function(e) {
            const testType = document.getElementById('testType');
            if (!testType || !testType.value) {
                e.preventDefault();
                alert('Vui lòng chọn loại xét nghiệm.');
                return;
            }
        });
    }

    // ============================================
    // 10. IMAGE VIEWER
    // ============================================
    const images = document.querySelectorAll('.image-thumbs .thumb img');
    images.forEach(function(img) {
        img.addEventListener('click', function() {
            window.open(this.src, '_blank');
        });
        img.style.cursor = 'pointer';
    });

    // ============================================
    // 11. PRINT SESSION DETAIL
    // ============================================
    const printBtn = document.createElement('button');
    printBtn.className = 'btn-action btn-outline';
    printBtn.innerHTML = '<i class="fa-solid fa-print"></i> In chi tiết';
    printBtn.style.marginLeft = '10px';

    const actionContainer = document.querySelector('.session-actions');
    if (actionContainer) {
        const existingBtn = actionContainer.querySelector('.btn-outline');
        if (existingBtn) {
            existingBtn.parentNode.insertBefore(printBtn, existingBtn.nextSibling);
        } else {
            actionContainer.appendChild(printBtn);
        }

        printBtn.addEventListener('click', function() {
            window.print();
        });
    }

    // ============================================
    // 12. KEYBOARD SHORTCUTS
    // ============================================
    document.addEventListener('keydown', function(e) {
        // Ctrl + E: Mở chế độ chỉnh sửa triệu chứng
        if (e.ctrlKey && e.key === 'e') {
            const editBtn = document.querySelector('.btn-edit');
            if (editBtn) {
                editBtn.click();
            }
        }

        // ESC: Đóng các modal
        if (e.key === 'Escape') {
            closeConfirmModal();
            closeStatusModal();
            closeShareModal();
            closeAddLabModal();

            // Đóng chỉnh sửa triệu chứng
            const cancelBtn = document.querySelector('.btn-outline[onclick*="cancelEditSymptoms"]');
            if (cancelBtn) cancelBtn.click();
        }

        // Ctrl + S: Lưu (submit form)
        if (e.ctrlKey && e.key === 's') {
            e.preventDefault();
            const saveBtn = document.querySelector('#symptomForm button[type="submit"]');
            if (saveBtn) {
                saveBtn.click();
            }
        }
    });

    // ============================================
    // 13. RESPONSIVE TABLE - Lab results scroll
    // ============================================
    const labTables = document.querySelectorAll('.lab-params');
    labTables.forEach(function(table) {
        const parent = table.closest('.lab-item');
        if (parent) {
            parent.style.overflowX = 'auto';
        }
    });

    // ============================================
    // 14. CLOSE ALERT FUNCTION
    // ============================================
    window.closeAlert = function(button) {
        const alert = button.closest('.alert');
        if (alert) {
            alert.style.transition = 'opacity 0.3s ease';
            alert.style.opacity = '0';
            setTimeout(function() {
                alert.style.display = 'none';
            }, 300);
        }
    };

    console.log('🚀 Doctor Session Detail page loaded');
});