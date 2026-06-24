import React from 'react';

// Map trạng thái ca chẩn đoán
const sessionStatusConfig = {
    PENDING: { label: 'Chờ xử lý', color: 'bg-yellow-100 text-yellow-800' },
    PROCESSING: { label: 'Đang xử lý', color: 'bg-blue-100 text-blue-800' },
    COMPLETED: { label: 'Hoàn thành', color: 'bg-green-100 text-green-800' },
    FAILED: { label: 'Thất bại', color: 'bg-red-100 text-red-800' },
    CANCELLED: { label: 'Đã hủy', color: 'bg-gray-100 text-gray-800' },
};

// Map trạng thái người dùng (User) - THEO YÊU CẦU
const userStatusConfig = {
    ACTIVE: { label: 'Đang hoạt động', color: 'bg-green-100 text-green-800' },
    INACTIVE: { label: 'Không hoạt động', color: 'bg-gray-100 text-gray-800' },
    BANNED: { label: 'Đã bị khóa', color: 'bg-red-100 text-red-800' },
    PENDING: { label: 'Chờ xác nhận', color: 'bg-yellow-100 text-yellow-800' },
};

// Map vai trò
const roleConfig = {
    ADMIN: { label: 'Quản trị viên', color: 'bg-purple-100 text-purple-800' },
    DOCTOR: { label: 'Bác sĩ', color: 'bg-blue-100 text-blue-800' },
    PATIENT: { label: 'Bệnh nhân', color: 'bg-green-100 text-green-800' },
};

// Map trạng thái xét nghiệm, hình ảnh
const labStatusConfig = {
    PENDING: { label: 'Chờ xử lý', color: 'bg-yellow-100 text-yellow-800' },
    PROCESSING: { label: 'Đang xử lý', color: 'bg-blue-100 text-blue-800' },
    COMPLETED: { label: 'Hoàn thành', color: 'bg-green-100 text-green-800' },
    FAILED: { label: 'Thất bại', color: 'bg-red-100 text-red-800' },
};

const StatusBadge = ({ type, value }) => {
    let config;

    if (type === 'userStatus') {
        config = userStatusConfig[value];
    } else if (type === 'role') {
        config = roleConfig[value];
    } else if (type === 'labStatus') {
        config = labStatusConfig[value];
    } else {
        config = sessionStatusConfig[value];
    }

    if (!config) {
        return (
            <span className="px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                {value}
            </span>
        );
    }

    return (
        <span className={`px-2 py-1 rounded-full text-xs font-medium ${config.color}`}>
            {config.label}
        </span>
    );
};

export default StatusBadge;