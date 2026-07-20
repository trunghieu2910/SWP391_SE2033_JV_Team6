/* Drug Form JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    initializeDrugForms();
    initializeUnitConversionBuilders();
});

function initializeDrugForms() {
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', validateDrugForm);
    });
}

function initializeUnitConversionBuilders() {
    document.querySelectorAll('.unit-conversion-builder').forEach(builder => {
        const addButton = builder.querySelector('.unit-conversion-add');
        const drugNameInputId = builder.dataset.drugNameInput;
        const drugNameInput = drugNameInputId ? document.getElementById(drugNameInputId) : null;

        if (addButton) {
            addButton.addEventListener('click', function() {
                addUnitConversionStep(builder);
            });
        }

        if (drugNameInput) {
            drugNameInput.addEventListener('input', function() {
                updateUnitConversionSummary(builder);
            });
        }

        const baseUnitSelect = getBaseUnitSelect(builder);
        if (baseUnitSelect) {
            baseUnitSelect.addEventListener('change', function() {
                updateUnitConversionSummary(builder);
            });
        }

        if (!builder.querySelector('.unit-conversion-step')) {
            addUnitConversionStep(builder);
        }

        bindUnitConversionStepEvents(builder);
        updateUnitConversionSummary(builder);
    });
}

function bindUnitConversionStepEvents(builder) {
    builder.querySelectorAll('.unit-conversion-step').forEach(step => {
        step.querySelectorAll('select, input').forEach(input => {
            input.removeEventListener('input', handleUnitConversionChange);
            input.removeEventListener('change', handleUnitConversionChange);
            input.addEventListener('input', handleUnitConversionChange);
            input.addEventListener('change', handleUnitConversionChange);
        });

        const removeButton = step.querySelector('.unit-conversion-remove');
        if (removeButton) {
            removeButton.removeEventListener('click', handleRemoveUnitConversionStep);
            removeButton.addEventListener('click', handleRemoveUnitConversionStep);
        }
    });
}

function handleUnitConversionChange(event) {
    const builder = event.currentTarget.closest('.unit-conversion-builder');
    if (builder) {
        updateUnitConversionSummary(builder);
    }
}

function handleRemoveUnitConversionStep(event) {
    const builder = event.currentTarget.closest('.unit-conversion-builder');
    const steps = builder.querySelectorAll('.unit-conversion-step');

    if (steps.length <= 1) {
        clearUnitConversionStep(steps[0]);
    } else {
        event.currentTarget.closest('.unit-conversion-step').remove();
    }

    updateUnitConversionSummary(builder);
}

function addUnitConversionStep(builder) {
    const container = builder.querySelector('.unit-conversion-steps');
    const firstStep = builder.querySelector('.unit-conversion-step');
    if (!container || !firstStep) return;

    const newStep = firstStep.cloneNode(true);
    clearUnitConversionStep(newStep);

    const lastStep = container.querySelector('.unit-conversion-step:last-child');
    const lastSmallUnit = lastStep ? lastStep.querySelector('.unit-conversion-small-unit')?.value : '';
    const newLargeUnit = newStep.querySelector('.unit-conversion-large-unit');
    if (newLargeUnit && lastSmallUnit) {
        newLargeUnit.value = lastSmallUnit;
    }

    container.appendChild(newStep);
    bindUnitConversionStepEvents(builder);
    updateUnitConversionSummary(builder);
}

function clearUnitConversionStep(step) {
    if (!step) return;

    step.querySelectorAll('select').forEach(select => {
        select.value = '';
    });
    step.querySelectorAll('input').forEach(input => {
        input.value = '';
    });
}

function updateUnitConversionSummary(builder) {
    const summary = builder.querySelector('.unit-conversion-summary');
    const drugNameText = builder.querySelector('.unit-conversion-drug-name');
    const baseUnitText = builder.querySelector('.unit-conversion-base-unit');
    const drugNameInputId = builder.dataset.drugNameInput;
    const drugNameInput = drugNameInputId ? document.getElementById(drugNameInputId) : null;
    const drugName = drugNameInput && drugNameInput.value.trim() ? drugNameInput.value.trim() : 'thuốc đang thiết lập';
    const result = buildUnitConversionSummary(builder);
    const baseUnitSelect = getBaseUnitSelect(builder);
    if (baseUnitSelect && !baseUnitSelect.value && result.baseUnitId) {
        baseUnitSelect.value = result.baseUnitId;
    }
    const selectedBaseUnit = baseUnitSelect && baseUnitSelect.value
        ? baseUnitSelect.options[baseUnitSelect.selectedIndex].text
        : '';

    if (drugNameText) {
        drugNameText.textContent = drugName;
    }

    if (baseUnitText) {
        baseUnitText.textContent = selectedBaseUnit || result.baseUnit || 'Đơn vị gốc';
    }

    if (summary) {
        summary.textContent = result.text || 'Nhập đủ các bước quy đổi để hệ thống tự tính tồn kho theo đơn vị nhỏ nhất.';
    }
}

function buildUnitConversionSummary(builder) {
    const steps = Array.from(builder.querySelectorAll('.unit-conversion-step'));
    let total = 1;
    let firstLargeUnit = '';
    let lastSmallUnit = '';
    let lastSmallUnitId = '';
    let isComplete = false;

    for (let i = 0; i < steps.length; i++) {
        const largeSelect = steps[i].querySelector('.unit-conversion-large-unit');
        const smallSelect = steps[i].querySelector('.unit-conversion-small-unit');
        const factorInput = steps[i].querySelector('.unit-conversion-factor');
        const factor = parseInt(factorInput?.value, 10);

        const hasAnyValue = Boolean(largeSelect?.value || smallSelect?.value || factorInput?.value);
        if (!hasAnyValue) {
            continue;
        }

        if (!largeSelect?.value || !smallSelect?.value || !Number.isInteger(factor) || factor <= 0) {
            return { text: '', baseUnit: lastSmallUnit, baseUnitId: lastSmallUnitId };
        }

        if (!firstLargeUnit) {
            firstLargeUnit = largeSelect.options[largeSelect.selectedIndex].text;
        }

        lastSmallUnit = smallSelect.options[smallSelect.selectedIndex].text;
        lastSmallUnitId = smallSelect.value;
        total *= factor;
        isComplete = true;
    }

    if (!isComplete || !firstLargeUnit || !lastSmallUnit) {
        return { text: '', baseUnit: lastSmallUnit, baseUnitId: lastSmallUnitId };
    }

    return {
        text: `Suy ra: 1 ${firstLargeUnit} = ${total} ${lastSmallUnit}`,
        baseUnit: lastSmallUnit,
        baseUnitId: lastSmallUnitId
    };
}

function validateDrugForm(e) {
    const form = e.currentTarget;
    const fields = [
        { name: 'drugName', label: 'Tên thuốc' },
        { name: 'strength', label: 'Hàm lượng' },
        { name: 'strengthUnit', label: 'Đơn vị hàm lượng' },
        { name: 'dosageForm', label: 'Dạng bào chế' },
        { name: 'routeOfAdministration', label: 'Đường dùng' },
        { name: 'subCategoryId', label: 'Phân nhóm thuốc' },
        { name: 'baseUnitId', label: 'Đơn vị gốc kê đơn' },
        { name: 'manufacturer', label: 'Nhà sản xuất' },
        { name: 'countryOfOrigin', label: 'Nước sản xuất' },
        { name: 'storageCondition', label: 'Điều kiện bảo quản' },
        { name: 'shelfLifeMonths', label: 'Thời hạn sử dụng' },
        { name: 'packaging', label: 'Quy cách đóng gói' }
    ];
    const missing = [];

    fields.forEach(field => {
        const el = form.querySelector(`[name="${field.name}"]`);
        if (!el || !el.value || el.value.trim() === '') {
            missing.push(field.label);
        }
    });

    if (missing.length > 0) {
        e.preventDefault();
        showDrugFormError(form, 'Vui lòng điền đầy đủ: ' + missing.join(', '));
        return false;
    }

    const invalidConversion = findInvalidConversionRow(form);
    if (invalidConversion) {
        e.preventDefault();
        showDrugFormError(form, invalidConversion);
        return false;
    }

    disableEmptyConversionRows(form);
    return true;
}

function findInvalidConversionRow(form) {
    const builder = form.querySelector('.unit-conversion-builder');
    if (!builder) return '';

    const steps = builder.querySelectorAll('.unit-conversion-step');
    const baseUnitSelect = form.querySelector('[name="baseUnitId"]');
    let lastSmallUnit = '';
    for (const step of steps) {
        const largeUnit = step.querySelector('.unit-conversion-large-unit')?.value;
        const smallUnit = step.querySelector('.unit-conversion-small-unit')?.value;
        const factorValue = step.querySelector('.unit-conversion-factor')?.value;
        const hasAnyValue = Boolean(largeUnit || smallUnit || factorValue);

        if (!hasAnyValue) {
            continue;
        }

        const factor = parseInt(factorValue, 10);
        if (!largeUnit || !smallUnit || !Number.isInteger(factor) || factor <= 0) {
            return 'Vui lòng nhập đầy đủ đơn vị lớn, số lượng và đơn vị nhỏ cho mỗi dòng quy đổi.';
        }

        if (largeUnit === smallUnit) {
            return 'Đơn vị lớn và đơn vị nhỏ trong quy đổi không được trùng nhau.';
        }

        lastSmallUnit = smallUnit;
    }

    if (lastSmallUnit && baseUnitSelect?.value && lastSmallUnit !== baseUnitSelect.value) {
        return 'Đơn vị nhỏ cuối cùng trong chuỗi quy đổi phải trùng với đơn vị gốc kê đơn.';
    }

    return '';
}

function showDrugFormError(form, message) {
    const alertBox = form.querySelector('#drugFormAlert, #editFormAlert');
    const alertMsg = form.querySelector('#drugFormAlertMsg, #editFormAlertMsg');

    if (alertBox && alertMsg) {
        alertMsg.textContent = message;
        alertBox.style.display = 'block';
        alertBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
    } else {
        alert(message);
    }
}

function disableEmptyConversionRows(form) {
    form.querySelectorAll('.unit-conversion-step').forEach(step => {
        const largeUnit = step.querySelector('.unit-conversion-large-unit');
        const smallUnit = step.querySelector('.unit-conversion-small-unit');
        const factor = step.querySelector('.unit-conversion-factor');
        const hasAnyValue = Boolean(largeUnit?.value || smallUnit?.value || factor?.value);

        if (!hasAnyValue) {
            [largeUnit, smallUnit, factor].forEach(input => {
                if (input) {
                    input.disabled = true;
                }
            });
        }
    });
}

function getBaseUnitSelect(builder) {
    const form = builder.closest('form');
    return form ? form.querySelector('[name="baseUnitId"]') : null;
}
