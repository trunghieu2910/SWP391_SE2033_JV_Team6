import api from './api';

export const labResultService = {
  createLabResult: (data)      => api.post('/api/lab-results', data),
  getBySession:    (sessionId) => api.get(`/api/lab-results/session/${sessionId}`),
};

export default labResultService;
