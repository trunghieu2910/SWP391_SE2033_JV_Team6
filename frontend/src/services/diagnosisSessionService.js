import api from './api';

const diagnosisSessionService = {
    getById: (sessionId) => api.get(`/api/diagnosis-sessions/${sessionId}`),
    getSymptomResult: (sessionId) => api.get(`/api/diagnosis-sessions/${sessionId}/symptom-result`),
    submitSymptomForm: (sessionId, data) => api.post(`/api/diagnosis-sessions/${sessionId}/symptom-result`, data),
};

export default diagnosisSessionService;
