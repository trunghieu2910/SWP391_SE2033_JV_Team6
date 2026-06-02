import api from './api';

export const diagnosisService = {
  getDiagnosis: (id) => api.get(`/diagnosis/${id}`),
  createDiagnosis: (data) => api.post('/diagnosis', data),
  updateDiagnosis: (id, data) => api.put(`/diagnosis/${id}`, data),
};

export default diagnosisService;

