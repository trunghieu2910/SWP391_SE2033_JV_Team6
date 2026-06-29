import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

let toastEmitter = null;
let lastErrorMessage = '';
let lastErrorTime = 0;
export const registerToastEmitter = (emitterFn) => {
    toastEmitter = emitterFn;
};

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
        // 1. Xử lý lỗi 401 (Chưa đăng nhập / Token hết hạn)
        if (err.response?.status === 401) {
            const isLoginRequest = err.config.url.includes('login');

            if (!isLoginRequest) {
                localStorage.removeItem('medai_token');
                localStorage.removeItem('medai_user');
                window.location.href = '/login';
            }
        }

        if (toastEmitter) {
            let msg = '';
            if (err.response?.status === 429) {
                msg = err.response.data?.message || "Bạn đã gửi quá nhiều yêu cầu. Vui lòng đợi 1 phút!";
            } else if (err.response?.status === 403) {
                msg = err.response.data?.message || "Yêu cầu bị từ chối. IP của bạn đã bị chặn truy cập!";
            }
            if (msg) {
                const now = Date.now();
                if (msg !== lastErrorMessage || (now - lastErrorTime > 2500)) {
                    lastErrorMessage = msg;
                    lastErrorTime = now;
                    toastEmitter(msg, 'error');
                }
            }
        }
        return Promise.reject(err);
    }
);

export default api;
