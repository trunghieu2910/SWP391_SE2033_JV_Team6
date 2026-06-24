import api from './api';

/**
 * Service mô phỏng việc "máy xét nghiệm / LIS" gửi kết quả về hệ thống AI.
 * Dùng API Key tĩnh thay cho JWT (endpoint backend không yêu cầu đăng nhập).
 */
const LIS_API_KEY = 'swp391-lis-simulation-key-2026';

export const lisIntegrationService = {
    sendResults: (payload) =>
        api.post('/api/integration/lis/results', payload, {
            headers: { 'X-API-Key': LIS_API_KEY },
        }),
};

export default lisIntegrationService;

