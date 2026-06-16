import api from './api';

const adminService = {
    // Dashboard
    getDashboardStats: () => api.get('/admin/dashboard'),
    getChartStats: () => api.get('/admin/dashboard/charts'),

    // User Management
    getUsers: (params) => api.get('/admin/users', { params }),
    getUserDetail: (userId) => api.get(`/admin/users/${userId}`),
    updateUserStatus: (data) => api.patch('/admin/users/status', data),

    // Create Doctor
    initiateCreateDoctor: (data) => api.post('/admin/doctors/initiate', data),
    confirmCreateDoctor: (data) => api.post('/admin/doctors/confirm', data),

    // System Logs
    getSystemLogs: (params) => api.get('/admin/logs', { params }),
};

export default adminService;