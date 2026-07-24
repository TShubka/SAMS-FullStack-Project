import api from './api';

/** Authentication API calls. Owner: Member 1. */
const authService = {
  register: (payload) => api.post('/auth/register', payload).then((r) => r.data),

  login: (credentials) => api.post('/auth/login', credentials).then((r) => r.data),

  getCurrentUser: () => api.get('/auth/me').then((r) => r.data),
};

export default authService;
