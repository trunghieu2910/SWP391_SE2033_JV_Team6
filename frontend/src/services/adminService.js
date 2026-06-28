import api from './api';

const adminService = {
    // Dashboard
    getDashboardStats: () => api.get('/api/admin/dashboard'),
    getChartStats: () => api.get('/api/admin/dashboard/charts'),

    // Search
    searchGlobal: (keyword) => api.get('/api/admin/search', { params: { keyword } }),

    // User Management
    getUsers: (params) => api.get('/api/admin/users', { params }),
    getUserDetail: (userId) => api.get(`/api/admin/users/${userId}`),
    updateUserStatus: (data) => api.patch('/api/admin/users/status', data),

    // Create Doctor
    initiateCreateDoctor: (data) => {
        if (data instanceof FormData) {
            return api.post('/api/admin/doctors/initiate', data, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
        }
        return api.post('/api/admin/doctors/initiate', data);
    },
    confirmCreateDoctor: (data) => api.post('/api/admin/doctors/confirm', data),

    // System Logs
    getSystemLogs: (params) => api.get('/api/admin/logs', { params }),
};

export default adminService;