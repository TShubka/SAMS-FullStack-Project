import api from './api';

/** Enrollment API. Owner: Member 2. */
const enrollmentService = {
  list: (params = {}) => api.get('/enrollments', { params }).then((r) => r.data),
  get: (id) => api.get(`/enrollments/${id}`).then((r) => r.data),
  byStudent: (studentId) => api.get(`/enrollments/student/${studentId}`).then((r) => r.data),
  byCourse: (courseId) => api.get(`/enrollments/course/${courseId}`).then((r) => r.data),
  mine: () => api.get('/enrollments/my').then((r) => r.data),
  create: (payload) => api.post('/enrollments', payload).then((r) => r.data),
  update: (id, payload) => api.put(`/enrollments/${id}`, payload).then((r) => r.data),
  remove: (id) => api.delete(`/enrollments/${id}`),
};

export default enrollmentService;
