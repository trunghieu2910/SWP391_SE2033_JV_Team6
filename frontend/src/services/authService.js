import api from './api';

export const authService = {
  login:                   (credentials)  => api.post('/api/auth/login', credentials),
  logout:                  ()             => api.post('/api/auth/logout'),
  register:                (data)          => api.post('/api/auth/register', data),
  verifyRegistrationOtp:   (data)          => api.post('/api/auth/register/verify-otp', data),
  resendRegistrationOtp:   (data)          => api.post('/api/auth/register/resend-otp', data),
  forgotPassword:          (data)          => api.post('/api/auth/forgot-password', data),
  verifyOtp:               (data)          => api.post('/api/auth/forgot-password/verify-otp', data),
  resetPassword:           (data)          => api.post('/api/auth/forgot-password/reset-password', data),
};

export default authService;
