import api from './api';

/** Course API. Owner: Member 2. */
const courseService = {
  list: (params = {}) => api.get('/courses', { params }).then((r) => r.data),
  get: (id) => api.get(`/courses/${id}`).then((r) => r.data),
  myCourses: () => api.get('/courses/my').then((r) => r.data),
  create: (payload) => api.post('/courses', payload).then((r) => r.data),
  update: (id, payload) => api.put(`/courses/${id}`, payload).then((r) => r.data),
  remove: (id) => api.delete(`/courses/${id}`),
};

export default courseService;
