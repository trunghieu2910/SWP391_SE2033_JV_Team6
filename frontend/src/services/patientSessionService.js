import api from './api';

const patientSessionService = {
  getActiveSession: () => api.get('/api/patient/sessions/active'),
  getSessions: () => api.get('/api/patient/sessions'),
  createSession: (data) => api.post('/api/patient/sessions', data),
};

export default patientSessionService;
