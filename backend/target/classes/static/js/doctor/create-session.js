// create-session.js — Doctor Create Diagnosis Session

let selectedPatient = null;

document.addEventListener('DOMContentLoaded', function () {
    // Enter key triggers search
    const searchInput = document.getElementById('patientSearchInput');
    if (searchInput) {
        searchInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchPatients();
            }
        });
        searchInput.focus();
    }

    console.log('🚀 Create Session page loaded');
});

// ===== PATIENT SEARCH =====
function searchPatients() {
    const keyword = document.getElementById('patientSearchInput').value.trim();
    const resultsEl = document.getElementById('searchResults');
    const emptyEl = document.getElementById('searchEmpty');
    const loadingEl = document.getElementById('searchLoading');
    const resultsBody = document.getElementById('resultsBody');

    // Show loading
    resultsEl.style.display = 'none';
    emptyEl.style.display = 'none';
    loadingEl.style.display = 'block';

    const url = '/api/diagnosis-sessions/search-patients' + (keyword ? '?keyword=' + encodeURIComponent(keyword) : '');

    fetch(url, {
        credentials: 'include',
        headers: {
            'Accept': 'application/json'
        }
    })
        .then(function (response) {
            if (!response.ok) throw new Error('Lỗi tìm kiếm');
            return response.json();
        })
        .then(function (apiResp) {
            loadingEl.style.display = 'none';
            const patients = apiResp.data || [];

            if (patients.length === 0) {
                emptyEl.style.display = 'block';
                resultsEl.style.display = 'none';
                return;
            }

            document.getElementById('resultCount').textContent = patients.length;
            resultsBody.innerHTML = '';

            patients.forEach(function (p) {
                var genderText = p.gender === 'Male' ? 'Nam' : p.gender === 'Female' ? 'Nữ' : (p.gender || '—');
                var dobText = p.dob ? formatDate(p.dob) : '—';
                var initial = (p.fullName || 'P').charAt(0).toUpperCase();

                var item = document.createElement('div');
                item.className = 'patient-result-item';
                item.innerHTML =
                    '<div class="result-avatar">' + initial + '</div>' +
                    '<div class="result-info">' +
                    '  <div class="result-name">' + escapeHtml(p.fullName || '—') + '</div>' +
                    '  <div class="result-meta">' +
                    '    <span><i class="fa-solid fa-id-card"></i> ' + escapeHtml(p.nationalId || '—') + '</span>' +
                    '    <span><i class="fa-solid fa-venus-mars"></i> ' + genderText + '</span>' +
                    '    <span><i class="fa-solid fa-calendar"></i> ' + dobText + '</span>' +
                    '    <span><i class="fa-solid fa-phone"></i> ' + escapeHtml(p.phoneNumber || '—') + '</span>' +
                    '  </div>' +
                    '</div>' +
                    '<button type="button" class="btn-select-patient" onclick="selectPatient(this)" ' +
                    '  data-patient=\'' + escapeAttr(JSON.stringify(p)) + '\'>' +
                    '  <i class="fa-solid fa-plus"></i> Chọn' +
                    '</button>';

                resultsBody.appendChild(item);
            });

            resultsEl.style.display = 'block';
        })
        .catch(function (err) {
            loadingEl.style.display = 'none';
            emptyEl.style.display = 'block';
            console.error('Search error:', err);
        });
}

// ===== SELECT PATIENT =====
function selectPatient(button) {
    var patientData = JSON.parse(button.getAttribute('data-patient'));
    selectedPatient = patientData;

    // Populate step 2
    document.getElementById('selectedAvatar').textContent = (patientData.fullName || 'P').charAt(0).toUpperCase();
    document.getElementById('selectedName').textContent = patientData.fullName || '—';
    document.getElementById('selectedIdBadge').textContent = 'ID: ' + patientData.patientId;
    document.getElementById('selectedGender').textContent =
        patientData.gender === 'Male' ? 'Nam' : patientData.gender === 'Female' ? 'Nữ' : (patientData.gender || '—');
    document.getElementById('selectedDob').textContent = patientData.dob ? formatDate(patientData.dob) : '—';
    document.getElementById('selectedNationalId').textContent = patientData.nationalId || '—';
    document.getElementById('selectedPhone').textContent = patientData.phoneNumber || '—';
    document.getElementById('selectedAddress').textContent = patientData.address || '—';

    // Clear form
    document.getElementById('weightInput').value = '';
    document.getElementById('heightInput').value = '';

    goToStep(2);
}

// ===== STEP NAVIGATION =====
function goToStep(step) {
    document.getElementById('step1').style.display = step === 1 ? 'block' : 'none';
    document.getElementById('step2').style.display = step === 2 ? 'block' : 'none';
    document.getElementById('step3').style.display = step === 3 ? 'block' : 'none';

    // Update step indicators
    var s1 = document.getElementById('step1Indicator');
    var s2 = document.getElementById('step2Indicator');
    var s3 = document.getElementById('step3Indicator');
    var connectors = document.querySelectorAll('.step-connector');

    s1.className = 'step' + (step === 1 ? ' active' : ' completed');
    s2.className = 'step' + (step === 2 ? ' active' : (step > 2 ? ' completed' : ''));
    s3.className = 'step' + (step === 3 ? ' active' : '');

    if (connectors.length >= 1) {
        connectors[0].className = 'step-connector' + (step >= 2 ? ' active' : '');
    }
    if (connectors.length >= 2) {
        connectors[1].className = 'step-connector' + (step >= 3 ? ' active' : '');
    }

    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function backToStep1() {
    selectedPatient = null;
    goToStep(1);
}

// ===== CONFIRMATION =====
function showConfirmation() {
    if (!selectedPatient) {
        alert('Vui lòng chọn bệnh nhân');
        return;
    }

    var weight = document.getElementById('weightInput').value;
    var height = document.getElementById('heightInput').value;

    document.getElementById('confirmPatientName').textContent = selectedPatient.fullName || '—';
    document.getElementById('confirmNationalId').textContent = selectedPatient.nationalId || '—';

    if (weight) {
        document.getElementById('confirmWeightRow').style.display = 'flex';
        document.getElementById('confirmWeight').textContent = weight + ' kg';
    } else {
        document.getElementById('confirmWeightRow').style.display = 'none';
    }

    if (height) {
        document.getElementById('confirmHeightRow').style.display = 'flex';
        document.getElementById('confirmHeight').textContent = height + ' cm';
    } else {
        document.getElementById('confirmHeightRow').style.display = 'none';
    }

    document.getElementById('createConfirmModal').style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function closeConfirmModal() {
    document.getElementById('createConfirmModal').style.display = 'none';
    document.body.style.overflow = '';
}

// ===== CREATE SESSION =====
function createSession() {
    if (!selectedPatient) return;

    var weight = document.getElementById('weightInput').value;
    var height = document.getElementById('heightInput').value;

    var btnConfirm = document.getElementById('btnConfirmCreate');
    btnConfirm.disabled = true;
    btnConfirm.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang tạo...';

    var requestBody = {
        patientId: selectedPatient.patientId
    };
    if (weight) requestBody.weight = parseFloat(weight);
    if (height) requestBody.height = parseFloat(height);

    fetch('/api/diagnosis-sessions', {
        method: 'POST',
        credentials: 'include',
        redirect: 'manual',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(requestBody)
    })
        .then(function (response) {
            if (response.status === 302 || response.type === 'opaqueredirect' || response.url.includes('/auth/login')) {
                throw new Error('Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.');
            }
            var contentType = response.headers.get('content-type') || '';
            if (!response.ok || !contentType.includes('application/json')) {
                return response.text().then(function (text) {
                    var message = 'Lỗi tạo phiên';
                    try {
                        var err = JSON.parse(text);
                        message = err.message || message;
                    } catch (jsonError) {
                        if (text && text.toLowerCase().includes('đăng nhập')) {
                            message = 'Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.';
                        }
                    }
                    if (response.status === 401) {
                        message = 'Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.';
                    }
                    console.error('Create session failed', response.status, text);
                    throw new Error(message);
                });
            }
            return response.json();
        })
        .then(function (apiResp) {
            closeConfirmModal();

            var data = apiResp.data;
            document.getElementById('newSessionId').textContent = data.sessionId;
            document.getElementById('successPatientName').textContent = selectedPatient.fullName || '—';
            document.getElementById('viewSessionLink').href = '/doctor/sessions/' + data.sessionId;

            goToStep(3);
        })
        .catch(function (err) {
            alert('❌ Lỗi: ' + err.message);
            btnConfirm.disabled = false;
            btnConfirm.innerHTML = '<i class="fa-solid fa-check"></i> Xác nhận tạo';
        });
}

// ===== RESET =====
function resetForm() {
    selectedPatient = null;
    document.getElementById('patientSearchInput').value = '';
    document.getElementById('searchResults').style.display = 'none';
    document.getElementById('searchEmpty').style.display = 'none';
    document.getElementById('searchLoading').style.display = 'none';
    goToStep(1);
    document.getElementById('patientSearchInput').focus();
}

// ===== UTILITIES =====
function formatDate(dateStr) {
    if (!dateStr) return '—';
    try {
        var parts = dateStr.split('-');
        if (parts.length === 3) {
            return parts[2] + '/' + parts[1] + '/' + parts[0];
        }
        return dateStr;
    } catch (e) {
        return dateStr;
    }
}

function escapeHtml(str) {
    if (!str) return '';
    var div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function escapeAttr(str) {
    return str.replace(/'/g, '&#39;').replace(/"/g, '&quot;');
}

// Close modal on ESC
document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        closeConfirmModal();
    }
});
