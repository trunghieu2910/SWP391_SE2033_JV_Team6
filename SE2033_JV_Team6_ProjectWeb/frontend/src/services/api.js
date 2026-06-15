import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8082';

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
});

// Tự động gắn JWT token (nếu có) vào mỗi request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
