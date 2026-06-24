import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

// Auto-inject JWT token from localStorage
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('medai_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Global error handling
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
        const isLoginRequest = err.config.url.includes('login');

        if (!isLoginRequest) {
            localStorage.removeItem('medai_token');
            localStorage.removeItem('medai_user');
            window.location.href = '/login';
        }
    }
    return Promise.reject(err);
  }
);

export default api;
