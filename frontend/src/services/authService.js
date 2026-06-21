import api from './api';

export const authService = {
  login:         (credentials)  => api.post('/api/auth/login', credentials),
  logout:        ()             => api.post('/api/auth/logout'),
  forgotPassword:(data)         => api.post('/api/auth/forgot-password', data),
  verifyOtp:     (data)         => api.post('/api/auth/forgot-password/verify-otp', data),
  resetPassword: (data)         => api.post('/api/auth/forgot-password/reset-password', data),
};

export default authService;
