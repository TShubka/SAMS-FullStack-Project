import api from './api';

/** Attendance API. Owner: Member 3. */
const attendanceService = {
  record: (payload) => api.post('/attendance', payload).then((r) => r.data),
  recordBulk: (payload) => api.post('/attendance/bulk', payload).then((r) => r.data),
  update: (id, payload) => api.put(`/attendance/${id}`, payload).then((r) => r.data),
  remove: (id) => api.delete(`/attendance/${id}`),
  byCourse: (courseId, date) =>
    api.get(`/attendance/course/${courseId}`, { params: date ? { date } : {} })
      .then((r) => r.data),
  byStudent: (studentId) => api.get(`/attendance/student/${studentId}`).then((r) => r.data),
  percentage: (studentId, courseId) =>
    api.get('/attendance/percentage', { params: { studentId, courseId } }).then((r) => r.data),
  courseSummary: (courseId) =>
    api.get(`/attendance/summary/course/${courseId}`).then((r) => r.data),
  low: (threshold) =>
    api.get('/attendance/low', { params: threshold ? { threshold } : {} }).then((r) => r.data),
};

export default attendanceService;
