import api from './api';

const diagnosisSessionService = {

    getById: (sessionId) => api.get(`/api/diagnosis-sessions/${sessionId}`),
};

export default diagnosisSessionService;
