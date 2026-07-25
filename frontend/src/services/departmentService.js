import api from './api';

/** Department API. Owner: Member 2. */
const departmentService = {
  list: (params = {}) => api.get('/departments', { params }).then((r) => r.data),
  get: (id) => api.get(`/departments/${id}`).then((r) => r.data),
  create: (payload) => api.post('/departments', payload).then((r) => r.data),
  update: (id, payload) => api.put(`/departments/${id}`, payload).then((r) => r.data),
  remove: (id) => api.delete(`/departments/${id}`),
};

export default departmentService;
