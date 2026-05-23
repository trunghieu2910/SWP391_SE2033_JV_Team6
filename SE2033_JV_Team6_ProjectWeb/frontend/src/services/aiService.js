import api from './api';

export const aiService = {
  predictImage: (formData) => api.post('/ai/predict', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
};

export default aiService;

