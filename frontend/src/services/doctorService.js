import api from './api';

const doctorService = {
    // Lấy danh sách ca chẩn đoán của bác sĩ
    getSessions: (params) => api.get('/doctor/sessions', { params }),

    // Lấy chi tiết ca chẩn đoán
    getSessionDetail: (sessionId) => api.get(`/doctor/sessions/${sessionId}`),

    // Cập nhật trạng thái ca chẩn đoán
    updateSessionStatus: (data) => api.patch('/doctor/sessions/status', data),

    // Cập nhật trạng thái công bố (isShared)
    updateSessionShare: (data) => api.patch('/doctor/sessions/share', data),

    // Lấy triệu chứng của ca chẩn đoán
    getSessionSymptoms: (sessionId) => api.get(`/doctor/sessions/${sessionId}/symptoms`),

    // Cập nhật triệu chứng
    updateSessionSymptoms: (sessionId, data) => api.put(`/doctor/sessions/${sessionId}/symptoms`, data),

    // Lấy danh sách thông số xét nghiệm
    getParameters: () => api.get('/parameters'),

    // Tạo xét nghiệm mới
    createLabResult: (data) => api.post('/lab-results', data),

    // Lấy danh sách xét nghiệm theo session
    getLabResultsBySession: (sessionId) => api.get(`/lab-results/session/${sessionId}`),

    // Lấy thông tin hồ sơ bác sĩ
    getProfile: () => api.get('/profile'),

    // Cập nhật thông tin hồ sơ bác sĩ
    updateProfile: (data) => api.put('/profile', data),
};

export default doctorService;