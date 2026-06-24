import api from './api';

const adminService = {
    // Dashboard
    getDashboardStats: () => api.get('/api/admin/dashboard'),
    getChartStats: () => api.get('/api/admin/dashboard/charts'),

    // User Management
    getUsers: (params) => api.get('/api/admin/users', { params }),
    getUserDetail: (userId) => api.get(`/api/admin/users/${userId}`),
    updateUserStatus: (data) => api.patch('/api/admin/users/status', data),

    // Create Doctor
    initiateCreateDoctor: (data) => api.post('/api/admin/doctors/initiate', data),
    confirmCreateDoctor: (data) => api.post('/api/admin/doctors/confirm', data),

    // System Logs
    getSystemLogs: (params) => api.get('/api/admin/logs', { params }),
};

export default adminService;