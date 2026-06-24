import api from './api';

export const medicalRecordService = {
  // Danh sách bệnh án (có filter + pagination)
  // params: { keyword, status, isShared, page, size }
  getRecords: (params = {}) => api.get('/api/medical-records', { params }),

  // Chi tiết 1 bệnh án
  getRecordDetail: (sessionId) => api.get(`/api/medical-records/${sessionId}`),
};

export default medicalRecordService;