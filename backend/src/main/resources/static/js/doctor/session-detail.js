// ============================================
// ACCORDION TOGGLE (Global scope so inline onclick always finds it)
// ============================================
window.toggleAccordion = function(id) {
    const body = document.getElementById(id);
    if (!body) return;
    const icon = document.getElementById(id.replace('Accordion', 'Icon'));

    const computedDisplay = window.getComputedStyle(body).display;
    const isHidden = (body.style.display === 'none' || computedDisplay === 'none');

    if (isHidden) {
        body.style.display = 'block';
        if (icon) {
            icon.classList.remove('fa-chevron-down');
            icon.classList.add('fa-chevron-up');
        }
    } else {
        body.style.display = 'none';
        if (icon) {
            icon.classList.remove('fa-chevron-up');
            icon.classList.add('fa-chevron-down');
        }
    }
};

// ============================================
// TOAST: tự động tắt sau 5 giây
// ============================================
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.wrapper .alert').forEach(function (alertEl) {
        setTimeout(function () {
            alertEl.classList.add('toast-hide');
            setTimeout(function () {
                alertEl.style.display = 'none';
            }, 300);
        }, 5000);
    });
});

document.addEventListener('DOMContentLoaded', function() {

    // ============================================
    // Lưu URL khi có bộ lọc được áp dụng
    // ============================================
    const backBtn = document.getElementById('backToSessionsBtn');
    if (backBtn) {
        backBtn.addEventListener('click', function(e) {
            e.preventDefault();
            const listUrl = sessionStorage.getItem('doctorSessionsListUrl') || '/doctor/sessions';
            window.location.href = listUrl;
        });
    }

    // ============================================
    // AUTO CLOSE ALERT AFTER 5 SECONDS
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
    // TỰ ĐỘNG MỞ LẠI KHUNG "XÉT NGHIỆM Y TẾ" SAU KHI
    // TẠO XÉT NGHIỆM (server redirect kèm ?openLab=true)
    // ============================================
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('openLab') === 'true') {
        toggleAccordion('labAccordion');
        const labSection = document.getElementById('labAccordion');
        if (labSection) {
            labSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
        // Xoá query param khỏi URL để lần F5 sau không tự mở lại nữa
        const cleanUrl = window.location.pathname + window.location.hash;
        window.history.replaceState({}, document.title, cleanUrl);
    }

    // ============================================
    // SYMPTOM FORM - EDIT / CANCEL
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
    // SYMPTOM FORM - CONFIRM WITH CUSTOM MODAL
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
                // symptomForm đã có sẵn CSRF token do Thymeleaf render trong HTML
                symptomForm.submit();
                closeConfirmModal();
            };

            openConfirmModal();
        });
    }

    // ============================================
    // REVIEW FORM - CONFIRM WITH SHARED MODAL
    // ============================================
    const reviewForm = document.getElementById('reviewForm');
    const reviewSubmitBtn = document.getElementById('reviewSubmitBtn');

    if (reviewForm && reviewSubmitBtn) {
        reviewSubmitBtn.addEventListener('click', function() {
            if (!reviewForm.reportValidity()) return; // để trình duyệt tự báo thiếu trường required

            confirmModalTitle.textContent = 'Xác nhận lưu kết luận';
            confirmModalMessage.textContent =
                'Sau khi lưu, kết luận này sẽ KHÔNG THỂ chỉnh sửa lại. Bạn có chắc chắn muốn lưu?';

            confirmModalOkBtn.onclick = function() {
                reviewForm.submit();
                closeConfirmModal();
            };

            openConfirmModal();
        });
    }

    // ============================================
    // CONFIRM MODAL FUNCTIONS
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
    // STATUS MODAL FUNCTIONS
    // ============================================
    let selectedStatusSessionId = null;
    let selectedStatusCurrent = null;

    window.openStatusModal = function() {
        const modal = document.getElementById('statusModal');
        const sessionId = document.getElementById('statusSessionId');
        const statusSelect = document.getElementById('statusSelect');

        if (modal) {
            const sessionIdText = sessionId ? sessionId.textContent : '';
            selectedStatusSessionId = sessionIdText;
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

        const form = document.getElementById('statusForm');
        form.action = '/doctor/sessions/' + selectedStatusSessionId + '/status';
        document.getElementById('statusFormInput').value = newStatus;
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
    // SHARE MODAL FUNCTIONS
    // ============================================
    let selectedShareSessionId = null;
    let selectedShareCurrent = null;

    window.openShareModal = function() {
        const modal = document.getElementById('shareModal');
        const sessionId = document.getElementById('shareSessionId');

        if (modal) {
            const sessionIdText = sessionId ? sessionId.textContent : '';
            selectedShareSessionId = sessionIdText;

            const isShared = document.querySelector('.badge-shared') !== null;
            selectedShareCurrent = isShared;

            const actionText = document.getElementById('shareActionText');
            const warningText = document.getElementById('shareWarningText');
            const btnText = document.getElementById('shareBtnText');
            const confirmBtn = document.getElementById('shareConfirmBtn');

            if (isShared) {
                actionText.textContent = 'gỡ công bố';
                warningText.textContent = 'Sau khi gỡ công bố, bệnh nhân và các bác sĩ khác sẽ không thể xem ca chẩn đoán này.';
                btnText.textContent = 'Gỡ công bố';
                confirmBtn.className = 'btn-action btn-danger';
            } else {
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

        const form = document.getElementById('shareForm');
        form.action = '/doctor/sessions/' + selectedShareSessionId + '/share';
        document.getElementById('shareFormInput').value = isShared;
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
    // ADD MEDICAL IMAGE MODAL FUNCTIONS
    // ============================================
    window.openAddMedicalImageModal = function() {
        const modal = document.getElementById('addMedicalImageModal');
        if (modal) {
            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        }
    };

    window.closeAddMedicalImageModal = function() {
        const modal = document.getElementById('addMedicalImageModal');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = '';
        }
    };

    // ============================================
    // RETAKE ULTRASOUND MODAL FUNCTIONS
    // ============================================
    window.openRetakeModal = function() {
        const modal = document.getElementById('retakeModal');
        if (modal) {
            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
        }
    };

    window.closeRetakeModal = function() {
        const modal = document.getElementById('retakeModal');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = '';
            const textarea = document.getElementById('retakeReason');
            if (textarea) textarea.value = '';
        }
    };

    // ============================================
    // ADD LAB MODAL FUNCTIONS
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
    // ADD LAB FORM SUBMIT
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
    // IMAGE VIEWER
    // ============================================
    const images = document.querySelectorAll('.image-thumbs .thumb img');
    images.forEach(function(img) {
        img.addEventListener('click', function() {
            window.open(this.src, '_blank');
        });
        img.style.cursor = 'pointer';
    });

    // ============================================
    // PRINT SESSION DETAIL
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
    // KEYBOARD SHORTCUTS
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
    // RESPONSIVE TABLE - Lab results scroll
    // ============================================
    const labTables = document.querySelectorAll('.lab-params');
    labTables.forEach(function(table) {
        const parent = table.closest('.lab-item');
        if (parent) {
            parent.style.overflowX = 'auto';
        }
    });

    // ============================================
    // CLOSE ALERT FUNCTION
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

function toggleNewDiseaseTypeInput(selectEl) {
    const group = document.getElementById('newDiseaseTypeNameGroup');
    const input = document.getElementById('newDiseaseTypeName');
    if (selectEl.value === 'NEW') {
        group.style.display = 'block';
        input.required = true;
        selectEl.setAttribute('data-selected-new', 'true');
    } else {
        group.style.display = 'none';
        input.required = false;
        input.value = '';
        selectEl.removeAttribute('data-selected-new');
    }
}

// ============================================
// PRESCRIPTION FORM LOGIC (KÊ ĐƠN THUỐC)
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    // Tự động mở khung kê đơn thuốc nếu URL chứa ?openPrescription=true
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('openPrescription') === 'true') {
        const body = document.getElementById('prescriptionAccordion');
        const icon = document.getElementById('prescriptionIcon');
        if (body) {
            body.style.display = 'block';
            if (icon) icon.className = 'fa-solid fa-chevron-up accordion-icon';
            body.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
        const cleanUrl = window.location.pathname + window.location.hash;
        window.history.replaceState({}, document.title, cleanUrl);
    }

    // Đóng tất cả dropdown menu khi click ra ngoài
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.custom-drug-dropdown-wrapper')) {
            document.querySelectorAll('.drug-dropdown-menu').forEach(menu => {
                menu.style.display = 'none';
            });
        }
    });

    // Validate form kê đơn trước khi submit
    const prescriptionForm = document.getElementById('prescriptionForm');
    if (prescriptionForm) {
        prescriptionForm.addEventListener('submit', function(e) {
            const rows = document.querySelectorAll('#prescriptionTableBody .prescription-row');
            if (rows.length === 0) {
                e.preventDefault();
                alert('Vui lòng thêm ít nhất một loại thuốc vào đơn!');
                return;
            }

            let isValid = true;
            rows.forEach(function(row, idx) {
                const idInput = row.querySelector('.drug-id-input');
                const searchInput = row.querySelector('.drug-search-input');
                const quantityInput = row.querySelector('.quantity-input');
                const instructionInput = row.querySelector('.instruction-input');

                if (!idInput || !idInput.value) {
                    alert(`Dòng ${idx + 1}: Vui lòng chọn thuốc từ danh sách!`);
                    if (searchInput) searchInput.focus();
                    isValid = false;
                    return;
                }

                const qty = parseInt(quantityInput.value);
                if (isNaN(qty) || qty <= 0) {
                    alert(`Dòng ${idx + 1}: Số lượng thuốc phải lớn hơn 0!`);
                    if (quantityInput) quantityInput.focus();
                    isValid = false;
                    return;
                }

                if (!instructionInput || !instructionInput.value.trim()) {
                    alert(`Dòng ${idx + 1}: Vui lòng nhập cách sử dụng thuốc!`);
                    if (instructionInput) instructionInput.focus();
                    isValid = false;
                    return;
                }
            });

            if (!isValid) {
                e.preventDefault();
            }
        });
    }
});

// Hiển thị danh sách dropdown khi click hoặc focus vào ô tìm kiếm thuốc
function showDrugDropdown(inputEl) {
    const wrapper = inputEl.closest('.custom-drug-dropdown-wrapper');
    if (!wrapper) return;
    const currentMenu = wrapper.querySelector('.drug-dropdown-menu');

    // Đóng tất cả các menu khác
    document.querySelectorAll('.drug-dropdown-menu').forEach(menu => {
        if (menu !== currentMenu) {
            menu.style.display = 'none';
        }
    });

    if (currentMenu) {
        currentMenu.style.display = 'block';
        filterCustomDrugList(inputEl);
    }
}

// Lọc danh sách thuốc trong custom dropdown theo Mã hoặc Tên thuốc
function filterCustomDrugList(inputEl) {
    const filter = inputEl.value.toLowerCase().trim();
    const wrapper = inputEl.closest('.custom-drug-dropdown-wrapper');
    if (!wrapper) return;
    const menu = wrapper.querySelector('.drug-dropdown-menu');
    if (!menu) return;

    const items = menu.querySelectorAll('.drug-option-item');
    const noFound = menu.querySelector('.no-drug-found');
    let hasMatch = false;

    items.forEach(function(item) {
        const code = (item.getAttribute('data-code') || '').toLowerCase();
        const name = (item.getAttribute('data-name') || '').toLowerCase();

        if (code.includes(filter) || name.includes(filter)) {
            item.style.display = 'block';
            hasMatch = true;
        } else {
            item.style.display = 'none';
        }
    });

    if (noFound) {
        noFound.style.display = hasMatch ? 'none' : 'block';
    }
}

// Chọn một loại thuốc trong dropdown
function selectDrugOption(itemEl) {
    const wrapper = itemEl.closest('.custom-drug-dropdown-wrapper');
    if (!wrapper) return;
    const row = itemEl.closest('.prescription-row');
    if (!row) return;

    const drugId = itemEl.getAttribute('data-id');
    const drugCode = itemEl.getAttribute('data-code');
    const drugName = itemEl.getAttribute('data-name');
    const unitName = itemEl.getAttribute('data-unit') || 'Đơn vị';
    const strength = itemEl.getAttribute('data-strength') || '';

    // Set hidden input value
    const idInput = wrapper.querySelector('.drug-id-input');
    if (idInput) idInput.value = drugId;

    // Set display search input value
    const searchInput = wrapper.querySelector('.drug-search-input');
    if (searchInput) {
        searchInput.value = `[${drugCode}] ${drugName}` + (strength ? ` - ${strength}` : '');
    }

    // Set unit badge
    const unitBadge = row.querySelector('.unit-badge');
    if (unitBadge) {
        unitBadge.textContent = unitName;
    }

    // Đóng menu
    const menu = wrapper.querySelector('.drug-dropdown-menu');
    if (menu) menu.style.display = 'none';
}

// Thêm một dòng thuốc mới vào bảng kê đơn
function addPrescriptionRow() {
    const tbody = document.getElementById('prescriptionTableBody');
    if (!tbody) return;

    const firstRow = tbody.querySelector('.prescription-row');
    if (!firstRow) return;

    const newRow = firstRow.cloneNode(true);

    // Reset thông tin trong dòng mới
    const idInput = newRow.querySelector('.drug-id-input');
    if (idInput) idInput.value = '';

    const searchInput = newRow.querySelector('.drug-search-input');
    if (searchInput) searchInput.value = '';

    const menu = newRow.querySelector('.drug-dropdown-menu');
    if (menu) {
        menu.style.display = 'none';
        menu.querySelectorAll('.drug-option-item').forEach(item => item.style.display = 'block');
        const noFound = menu.querySelector('.no-drug-found');
        if (noFound) noFound.style.display = 'none';
    }

    const qtyInput = newRow.querySelector('.quantity-input');
    if (qtyInput) qtyInput.value = '';

    const unitBadge = newRow.querySelector('.unit-badge');
    if (unitBadge) unitBadge.textContent = '-';

    const instructionInput = newRow.querySelector('.instruction-input');
    if (instructionInput) instructionInput.value = '';

    tbody.appendChild(newRow);
    reindexPrescriptionRows();
}

// Xóa dòng thuốc
function removePrescriptionRow(buttonEl) {
    const tbody = document.getElementById('prescriptionTableBody');
    if (!tbody) return;
    const rows = tbody.querySelectorAll('.prescription-row');

    if (rows.length <= 1) {
        const row = rows[0];
        const idInput = row.querySelector('.drug-id-input');
        if (idInput) idInput.value = '';
        const searchInput = row.querySelector('.drug-search-input');
        if (searchInput) searchInput.value = '';
        const qtyInput = row.querySelector('.quantity-input');
        if (qtyInput) qtyInput.value = '';
        const unitBadge = row.querySelector('.unit-badge');
        if (unitBadge) unitBadge.textContent = '-';
        const instructionInput = row.querySelector('.instruction-input');
        if (instructionInput) instructionInput.value = '';
        return;
    }

    const row = buttonEl.closest('.prescription-row');
    if (row) {
        row.remove();
        reindexPrescriptionRows();
    }
}

// Đánh lại số thứ tự và attribute name của từng trường theo items[index]
function reindexPrescriptionRows() {
    const rows = document.querySelectorAll('#prescriptionTableBody .prescription-row');
    rows.forEach(function(row, index) {
        const indexTd = row.querySelector('.row-index');
        if (indexTd) indexTd.textContent = index + 1;

        const idInput = row.querySelector('.drug-id-input');
        if (idInput) idInput.setAttribute('name', `items[${index}].drugId`);

        const qtyInput = row.querySelector('.quantity-input');
        if (qtyInput) qtyInput.setAttribute('name', `items[${index}].quantityPrescribed`);

        const instructionInput = row.querySelector('.instruction-input');
        if (instructionInput) instructionInput.setAttribute('name', `items[${index}].instruction`);
    });
}
