import apiPrivate from './api';

export const securityService = {
    getStats: async () => {
        const response = await apiPrivate.get('/api/admin/security/stats');
        return response.data;
    },
    getTopIps: async (limit = 10) => {
        const response = await apiPrivate.get(`/api/admin/security/top-ips?limit=${limit}`);
        return response.data;
    },
    getTopEndpoints: async (limit = 10) => {
        const response = await apiPrivate.get(`/api/admin/security/top-endpoints?limit=${limit}`);
        return response.data;
    },
    getBlockedIps: async () => {
        const response = await apiPrivate.get('/api/admin/security/blocked-ips');
        return response.data;
    },
    blockIp: async (ipAddress, reason) => {
        const response = await apiPrivate.post('/api/admin/security/block-ip', { ipAddress, reason });
        return response.data;
    },
    unblockIp: async (ipAddress) => {
        const response = await apiPrivate.delete(`/api/admin/security/unblock-ip/${ipAddress}`);
        return response.data;
    }
};