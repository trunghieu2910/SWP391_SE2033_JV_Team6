// src/utils/statusUtils.js
export const getStatusColor = (status) => {
    switch (status) {
        case 'ACTIVE':
            return 'text-green-600 bg-green-100';
        case 'INACTIVE':
            return 'text-gray-600 bg-gray-100';
        case 'BANNED':
            return 'text-red-600 bg-red-100';
        case 'PENDING':
            return 'text-yellow-600 bg-yellow-100';
        default:
            return 'text-gray-600 bg-gray-100';
    }
};

export const getRoleColor = (role) => {
    switch (role) {
        case 'ADMIN':
            return 'text-purple-600 bg-purple-100';
        case 'DOCTOR':
            return 'text-blue-600 bg-blue-100';
        case 'PATIENT':
            return 'text-green-600 bg-green-100';
        default:
            return 'text-gray-600 bg-gray-100';
    }
};