// global-search.js

(function() {
    'use strict';

    const searchInput = document.getElementById('globalSearchInput');
    const searchResults = document.getElementById('searchResults');
    let searchTimeout = null;
    let isSearching = false;

    // ===== MAP DỮ LIỆU SANG TIẾNG VIỆT =====
    const roleMap = {
        'ADMIN': 'Quản trị viên',
        'DOCTOR': 'Bác sĩ',
        'PATIENT': 'Bệnh nhân'
    };

    const statusMap = {
        'ACTIVE': 'Hoạt động',
        'INACTIVE': 'Không hoạt động',
        'BANNED': 'Đã khóa'
    };

    const actionMap = {
        'LOGIN': 'Đăng nhập',
        'LOGOUT': 'Đăng xuất',
        'BAN_USER': 'Khóa User',
        'UNBAN_USER': 'Mở khóa',
        'CREATE_DOCTOR': 'Tạo bác sĩ',
        'UPDATE_USER_STATUS': 'Đổi trạng thái',
        'BLOCK_IP': 'Chặn IP',
        'UNBLOCK_IP': 'Mở khóa IP'
    };

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
            'INACTIVE': 'status-inactive',
            'BANNED': 'status-banned'
        };
        return `<span class="search-badge ${statusMap[status] || ''}">${getStatusDisplay(status)}</span>`;
    }

    function getRoleBadge(role) {
        if (!role) return '';
        const roleMap = {
            'ADMIN': 'role-admin',
            'DOCTOR': 'role-doctor',
            'PATIENT': 'role-patient'
        };
        return `<span class="search-badge ${roleMap[role] || ''}">${getRoleDisplay(role)}</span>`;
    }

    function getItemLink(item, category) {
        switch(category) {
            case 'users':
                return `/admin/users/${item.userId}`;
            case 'logs':
                return `/admin/logs?keyword=${encodeURIComponent(item.description || '')}`;
            case 'blockedIPs':
                return `/admin/security?keyword=${encodeURIComponent(item.ipAddress || '')}`;
            default:
                return '#';
        }
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
                        ${getActionDisplay(log.action)}
                        <span class="search-badge log-action">${log.action}</span>
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