import api from './api';

const doctorService = {
    // Lấy danh sách ca chẩn đoán của bác sĩ
    getSessions: (params) => api.get('/api/doctor/sessions', { params }),

    // Lấy chi tiết ca chẩn đoán
    getSessionDetail: (sessionId) => api.get(`/api/doctor/sessions/${sessionId}`),

    // Cập nhật trạng thái ca chẩn đoán
    updateSessionStatus: (data) => api.patch('/api/doctor/sessions/status', data),

    // Cập nhật trạng thái công bố (isShared)
    updateSessionShare: (data) => api.patch('/api/doctor/sessions/share', data),

    // Lấy triệu chứng của ca chẩn đoán
    getSessionSymptoms: (sessionId) => api.get(`/api/doctor/sessions/${sessionId}/symptoms`),

    // Cập nhật triệu chứng
    updateSessionSymptoms: (sessionId, data) => api.put(`/api/diagnosis-sessions/${sessionId}/symptom-result`, data),

    // Lấy danh sách thông số xét nghiệm
    getParameters: () => api.get('/api/parameters'),

    // Tạo xét nghiệm mới
    createLabResult: (data) => api.post('/api/lab-results', data),

    // Lấy danh sách xét nghiệm theo session
    getLabResultsBySession: (sessionId) => api.get(`/api/lab-results/session/${sessionId}`),
};

export default doctorService;