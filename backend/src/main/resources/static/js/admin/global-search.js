(function() {
    'use strict';

    const searchInput = document.getElementById('globalSearchInput');
    const searchResults = document.getElementById('searchResults');
    let searchTimeout = null;
    let isSearching = false;

    const roleMap = {
        'ADMIN': 'Quản trị viên',
        'DOCTOR': 'Bác sĩ',
        'PATIENT': 'Bệnh nhân',
        'PHARMACIST': 'Dược sĩ',
        'RECEPTIONIST': 'Lễ tân',
        'ULTRASOUND_DOCTOR': 'Bác sĩ siêu âm'
    };

    const statusMap = {
        'ACTIVE': 'Hoạt động',
        'BANNED': 'Đã khóa'
    };

    // Đồng bộ với AdminServiceImpl#mapActionToVietnamese bên backend.
    // Khi thêm action mới bên Java, nhớ cập nhật lại map này.
    const actionMap = {
        // Nhóm Đăng nhập
        'LOGIN': 'Đăng nhập',
        'GOOGLE_LOGIN_FIRST_TIME': 'Đăng nhập',
        'GOOGLE_LOGIN': 'Đăng nhập',

        // Nhóm Tạo
        'CREATE_DOCTOR': 'Tạo',
        'CREATE_FINAL_DIAGNOSIS': 'Tạo',
        'CREATE': 'Tạo',
        'CREATE_LAB_RESULT': 'Tạo',

        // Nhóm Cập nhật
        'UPDATE_SESSION_STATUS': 'Cập nhật',
        'UPDATE_SESSION_SHARE': 'Cập nhật',
        'UPDATE_CLINICAL_SYMPTOMS': 'Cập nhật',
        'UPDATE_PASSWORD': 'Cập nhật',
        'UPDATE_REMINDER': 'Cập nhật',

        // Nhóm Nhận kết quả
        'LIS_RECEIVE': 'Nhận kết quả',
        'LIS_SIMULATE': 'Nhận kết quả',

        // Nhóm Xuất file pdf
        'PATIENT_EXPORT_PDF_MEDICAL_RECORD': 'Xuất file pdf',
        'DOCTOR_EXPORT_PDF_MEDICAL_RECORD': 'Xuất file pdf',

        // Các action riêng lẻ
        'LOGOUT': 'Đăng xuất',
        'BAN_USER': 'Khóa User',
        'UNBAN_USER': 'Mở khóa',
        'UPDATE_USER_STATUS': 'Đổi trạng thái',
        'BLOCKED_IP': 'Chặn IP',
        'UNBLOCKED_IP': 'Mở khóa IP',
        'PATIENT_NOTIFICATION': 'Thông báo',
        'FORGOT_PASSWORD': 'Quên mật khẩu',
        'VERIFY_OTP': 'Xác minh OTP',
        'DELETE_LAB_RESULT': 'Xóa',
        'REGISTER': 'Đăng kí',
        'PATIENT_FILL_CLINLCAL_SYMPTOMS_FORM': 'Nhập',
        'CREATE_REMINDER': 'Tạo nhắc nhở',
        'VIEW_DIAGNOSIS_SESSIONS': 'Xem chẩn đoán',
        'DELETE_REMINDER': 'Xoá nhắc nhở',
        'REQUEST_MEDICAL_IMAGE': 'Yêu cầu',
        'DELETE_MEDICAL_IMAGE': 'Xoá ảnh',
        'RECEPTIONIST_CHANGE_PASSWORD': 'Đổi mật khẩu',
        'RECEPTIONIST_CREATE_SESSION': 'Tạo ca khám',
        'RECEPTIONIST_CREATE_PATIENT_ACCOUNT': 'Tạo tài khoản',
        'CREATE_STAFF': 'Tạo tài khoản'
    };

    // Map nhãn hiển thị -> class badge, tái sử dụng style .badge-* đã dùng ở admin/dashboard.html
    const actionBadgeClassMap = {
        'Đăng nhập': 'badge-login',
        'Đăng xuất': 'badge-logout',
        'Khóa User': 'badge-ban',
        'Mở khóa': 'badge-unban',
        'Tạo': 'badge-create',
        'Đổi trạng thái': 'badge-update',
        'Chặn IP': 'badge-block',
        'Mở khóa IP': 'badge-unblock',
        'Thông báo': 'badge-notification',
        'Tạo ca khám': 'badge-create-session',
        'Cập nhật': 'badge-update',
        'Nhận kết quả': 'badge-receive',
        'Xuất file pdf': 'badge-export',
        'Quên mật khẩu': 'badge-forgot',
        'Xác minh OTP': 'badge-otp',
        'Xóa': 'badge-delete',
        'Đăng kí': 'badge-register',
        'Nhập': 'badge-input',
        'Tạo nhắc nhở': 'badge-create',
        'Xem chẩn đoán': 'badge-diagnosis',
        'Xoá nhắc nhở': 'badge-delete',
        'Yêu cầu': 'badge-request',
        'Xoá ảnh': 'badge-delete',
        'Đổi mật khẩu': 'badge-forgot',
        'Tạo tài khoản': 'badge-create-session'
    };

    function getActionBadgeClass(action) {
        const display = getActionDisplay(action);
        return actionBadgeClassMap[display] || 'badge-other';
    }

    function getRoleDisplay(role) {
        return roleMap[role] || role;
    }

    function getStatusDisplay(status) {
        return statusMap[status] || status;
    }

    function getActionDisplay(action) {
        return actionMap[action] || action;
    }

    function getCategoryIcon(category) {
        const icons = {
            'users': 'fa-solid fa-users',
            'logs': 'fa-solid fa-clock-rotate-left',
            'blockedIPs': 'fa-solid fa-shield-halved'
        };
        return icons[category] || 'fa-solid fa-circle';
    }

    function getCategoryLabel(category) {
        const labels = {
            'users': 'Người dùng',
            'logs': 'Nhật ký hoạt động',
            'blockedIPs': 'Bảo mật IP'
        };
        return labels[category] || category;
    }

    function getCategoryColor(category) {
        const colors = {
            'users': 'user',
            'logs': 'log',
            'blockedIPs': 'security'
        };
        return colors[category] || '';
    }

    function getStatusBadge(status) {
        if (!status) return '';
        const statusMap = {
            'ACTIVE': 'status-active',
            'BANNED': 'status-banned'
        };
        return `<span class="search-badge ${statusMap[status] || ''}">${getStatusDisplay(status)}</span>`;
    }

    function getRoleBadge(role) {
        if (!role) return '';
        const roleClassMap = {
            'ADMIN': 'role-admin',
            'DOCTOR': 'role-doctor',
            'PATIENT': 'role-patient',
            'PHARMACIST': 'role-pharmacist',
            'RECEPTIONIST': 'role-receptionist',
            'ULTRASOUND_DOCTOR': 'role-ultrasound_doctor'
        };
        return `<span class="search-badge ${roleClassMap[role] || ''}">${getRoleDisplay(role)}</span>`;
    }

    function renderUserItem(user) {
        return `
            <a href="/admin/users/${user.userId}" class="search-item">
                <div class="search-icon user">
                    <i class="fa-solid fa-user"></i>
                </div>
                <div class="search-content">
                    <div class="search-title">
                        ${user.fullName || user.userName}
                        ${getRoleBadge(user.roleName)}
                        ${getStatusBadge(user.status)}
                    </div>
                    <div class="search-subtitle">
                        <span>@${user.userName}</span>
                        ${user.email ? `<span class="separator">•</span><span>${user.email}</span>` : ''}
                        ${user.phoneNumber ? `<span class="separator">•</span><span>${user.phoneNumber}</span>` : ''}
                    </div>
                </div>
                <span class="search-action">Xem →</span>
            </a>
        `;
    }

    function renderLogItem(log) {
        return `
            <a href="/admin/logs?keyword=${encodeURIComponent(log.description || '')}" class="search-item">
                <div class="search-icon log">
                    <i class="fa-solid fa-clock"></i>
                </div>
                <div class="search-content">
                    <div class="search-title">
                        <span class="search-badge ${getActionBadgeClass(log.action)}">${getActionDisplay(log.action)}</span>
                    </div>
                    <div class="search-subtitle">
                        <span>${log.description || ''}</span>
                        ${log.username ? `<span class="separator">•</span><span>👤 ${log.username}</span>` : ''}
                        ${log.performedAt ? `<span class="separator">•</span><span>${log.performedAt}</span>` : ''}
                    </div>
                </div>
                <span class="search-action">Xem →</span>
            </a>
        `;
    }

    function renderSecurityItem(security) {
        return `
            <a href="/admin/security?keyword=${encodeURIComponent(security.ipAddress || '')}" class="search-item">
                <div class="search-icon security">
                    <i class="fa-solid fa-ban"></i>
                </div>
                <div class="search-content">
                    <div class="search-title">
                        ${security.ipAddress}
                        <span class="search-badge status-banned">Bị chặn</span>
                    </div>
                    <div class="search-subtitle">
                        <span>${security.reason || 'Không có lý do'}</span>
                        ${security.createdBy ? `<span class="separator">•</span><span>👤 ${security.createdBy}</span>` : ''}
                        ${security.blockedAt ? `<span class="separator">•</span><span>${security.blockedAt}</span>` : ''}
                    </div>
                </div>
                <span class="search-action">Xem →</span>
            </a>
        `;
    }

    function renderCategory(data, category, renderFn) {
        if (!data || data.length === 0) return '';

        return `
            <div class="search-category">
                <i class="${getCategoryIcon(category)}"></i>
                ${getCategoryLabel(category)}
                <span class="category-count">${data.length}</span>
            </div>
            ${data.map(renderFn).join('')}
        `;
    }

    function renderResults(data) {
        if (!data) {
            return `
                <div class="search-no-result">
                    <i class="fa-regular fa-face-smile"></i>
                    <p>Nhập từ khóa để tìm kiếm</p>
                    <span>Tìm kiếm người dùng, nhật ký, IP bị chặn...</span>
                </div>
            `;
        }

        const hasUsers = data.users && data.users.length > 0;
        const hasLogs = data.logs && data.logs.length > 0;
        const hasSecurity = data.blockedIPs && data.blockedIPs.length > 0;

        if (!hasUsers && !hasLogs && !hasSecurity) {
            return `
                <div class="search-no-result">
                    <i class="fa-regular fa-face-frown"></i>
                    <p>Không tìm thấy kết quả nào</p>
                    <span>Hãy thử từ khóa khác</span>
                </div>
            `;
        }

        let html = '';

        if (hasUsers) {
            html += renderCategory(data.users, 'users', renderUserItem);
        }

        if (hasLogs) {
            html += renderCategory(data.logs, 'logs', renderLogItem);
        }

        if (hasSecurity) {
            html += renderCategory(data.blockedIPs, 'blockedIPs', renderSecurityItem);
        }

        return html;
    }

    function performSearch(keyword) {
        if (!keyword || keyword.trim().length < 2) {
            searchResults.style.display = 'none';
            return;
        }

        isSearching = true;
        searchResults.innerHTML = `
            <div class="search-loading">
                <i class="fa-solid fa-spinner"></i> Đang tìm kiếm...
            </div>
        `;
        searchResults.style.display = 'block';

        fetch(`/api/admin/search?keyword=${encodeURIComponent(keyword.trim())}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Search failed');
                }
                return response.json();
            })
            .then(data => {
                isSearching = false;
                const html = renderResults(data);
                searchResults.innerHTML = html;
                searchResults.style.display = 'block';
            })
            .catch(error => {
                console.error('Search error:', error);
                isSearching = false;
                searchResults.innerHTML = `
                    <div class="search-no-result">
                        <i class="fa-regular fa-circle-xmark" style="color: #ef4444;"></i>
                        <p>Đã xảy ra lỗi khi tìm kiếm</p>
                        <span>Vui lòng thử lại sau</span>
                    </div>
                `;
                searchResults.style.display = 'block';
            });
    }

    // Event Listeners
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const keyword = this.value;

            clearTimeout(searchTimeout);

            if (keyword.length < 2) {
                searchResults.style.display = 'none';
                return;
            }

            searchTimeout = setTimeout(function() {
                performSearch(keyword);
            }, 300);
        });

        searchInput.addEventListener('focus', function() {
            const keyword = this.value.trim();
            if (keyword.length >= 2 && !isSearching) {
                performSearch(keyword);
            }
        });

        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                searchResults.style.display = 'none';
                this.blur();
            }
        });
    }

    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        const wrapper = document.querySelector('.search-wrapper');
        if (wrapper && !wrapper.contains(e.target)) {
            searchResults.style.display = 'none';
        }
    });

    console.log('🔍 Global Search initialized');
})();