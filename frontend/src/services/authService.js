import api from './api';

export const authService = {
  async login(login, password) {
    console.log('🔐 authService.login called');
    console.log('📤 Sending to:', `${api.defaults.baseURL}/api/auth/login`);

    const response = await api.post('/api/auth/login', { login, password });

    console.log('📥 Response:', response.data);

    const { accessToken, userId, username, email, role } = response.data;

    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('user', JSON.stringify({ userId, username, email, role }));

    return response.data;
  },

  async logout() {
    try {
      await api.post('/api/auth/logout');
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
    }
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  getToken() {
    return localStorage.getItem('accessToken');
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  hasRole(role) {
    const user = this.getCurrentUser();
    return user?.role === role;
  },
};