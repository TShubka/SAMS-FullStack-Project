import api from './api';

/** Assessments, marks and computed grades. Owner: Member 3. */
const gradeService = {
  // assessments
  assessmentsByCourse: (courseId) =>
    api.get('/assessments', { params: { courseId } }).then((r) => r.data),
  createAssessment: (payload) => api.post('/assessments', payload).then((r) => r.data),
  updateAssessment: (id, payload) => api.put(`/assessments/${id}`, payload).then((r) => r.data),
  removeAssessment: (id) => api.delete(`/assessments/${id}`),

  // marks
  marksByEnrollment: (enrollmentId) =>
    api.get(`/marks/enrollment/${enrollmentId}`).then((r) => r.data),
  marksByCourse: (courseId) => api.get(`/marks/course/${courseId}`).then((r) => r.data),
  createMark: (payload) => api.post('/marks', payload).then((r) => r.data),
  updateMark: (id, payload) => api.put(`/marks/${id}`, payload).then((r) => r.data),
  removeMark: (id) => api.delete(`/marks/${id}`),

  // computed grades and GPA
  gradesByStudent: (studentId) => api.get(`/grades/student/${studentId}`).then((r) => r.data),
  myGrades: () => api.get('/grades/my').then((r) => r.data),
  gpa: (studentId, params = {}) =>
    api.get(`/grades/gpa/${studentId}`, { params }).then((r) => r.data),
  myGpa: (params = {}) => api.get('/grades/gpa/my', { params }).then((r) => r.data),
};

export default gradeService;
