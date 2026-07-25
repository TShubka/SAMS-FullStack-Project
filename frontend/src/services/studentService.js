import api from './api';

/** Student API. Owner: Member 2. */
const studentService = {
  list: (params = {}) => api.get('/students', { params }).then((r) => r.data),
  get: (id) => api.get(`/students/${id}`).then((r) => r.data),
  me: () => api.get('/students/me').then((r) => r.data),
  create: (payload) => api.post('/students', payload).then((r) => r.data),
  update: (id, payload) => api.put(`/students/${id}`, payload).then((r) => r.data),
  remove: (id) => api.delete(`/students/${id}`),
};

export default studentService;
