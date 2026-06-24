import React from 'react';
import { FaSearch, FaFilter } from 'react-icons/fa';

const UserFilters = ({ filters, onFilterChange, onClear }) => {
    const roles = ['', 'ADMIN', 'DOCTOR', 'PATIENT'];
    const statuses = ['', 'ACTIVE', 'INACTIVE', 'BANNED'];

    const handleChange = (key, value) => {
        onFilterChange({ ...filters, [key]: value, page: 0 });
    };

    return (
        <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
            <div className="flex flex-wrap gap-4 items-end">
                <div className="flex-1 min-w-[200px]">
                    <label className="block text-sm text-gray-600 mb-1">Tìm kiếm</label>
                    <div className="relative">
                        <FaSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                        <input
                            type="text"
                            placeholder="Từ khóa..."
                            value={filters.keyword || ''}
                            onChange={(e) => handleChange('keyword', e.target.value)}
                            className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#097300]"
                        />
                    </div>
                </div>
                <div className="w-40">
                    <label className="block text-sm text-gray-600 mb-1">Vai trò</label>
                    <select
                        value={filters.role || ''}
                        onChange={(e) => handleChange('role', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#097300]"
                    >
                        {roles.map(role => (
                            <option key={role} value={role}>{role || 'Tất cả vai trò'}</option>
                        ))}
                    </select>
                </div>
                <div className="w-40">
                    <label className="block text-sm text-gray-600 mb-1">Trạng thái</label>
                    <select
                        value={filters.status || ''}
                        onChange={(e) => handleChange('status', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#097300]"
                    >
                        {statuses.map(status => (
                            <option key={status} value={status}>{status || 'Tất cả trạng thái'}</option>
                        ))}
                    </select>
                </div>
                <button
                    onClick={onClear}
                    className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 flex items-center gap-2"
                >
                    <FaFilter className="w-4 h-4" /> Xóa bộ lọc
                </button>
            </div>
        </div>
    );
};

export default UserFilters;