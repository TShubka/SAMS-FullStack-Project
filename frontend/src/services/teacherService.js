import api from './api';

/** Teacher API. Owner: Member 2. */
const teacherService = {
  list: (params = {}) => api.get('/teachers', { params }).then((r) => r.data),
  get: (id) => api.get(`/teachers/${id}`).then((r) => r.data),
  me: () => api.get('/teachers/me').then((r) => r.data),
  create: (payload) => api.post('/teachers', payload).then((r) => r.data),
  update: (id, payload) => api.put(`/teachers/${id}`, payload).then((r) => r.data),
  remove: (id) => api.delete(`/teachers/${id}`),
};

export default teacherService;
