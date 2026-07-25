import api from './api';

/** Transcripts and the eight reports. Owner: Member 4. */
const reportService = {
  myTranscript: () => api.get('/transcripts/my').then((r) => r.data),
  transcript: (studentId) => api.get(`/transcripts/student/${studentId}`).then((r) => r.data),

  studentsByDepartment: () => api.get('/reports/students-by-department').then((r) => r.data),
  studentPerformance: (studentId) =>
    api.get(`/reports/student-performance/${studentId}`).then((r) => r.data),
  attendanceByCourse: (courseId) =>
    api.get(`/reports/attendance/course/${courseId}`).then((r) => r.data),
  lowAttendance: (threshold) =>
    api.get('/reports/low-attendance', { params: threshold ? { threshold } : {} })
      .then((r) => r.data),
  coursePerformance: (courseId) =>
    api.get(`/reports/course-performance/${courseId}`).then((r) => r.data),
  gradeDistribution: (courseId) =>
    api.get('/reports/grade-distribution', { params: { courseId } }).then((r) => r.data),
  passFail: (courseId) =>
    api.get('/reports/pass-fail', { params: { courseId } }).then((r) => r.data),
  departmentPerformance: (departmentId) =>
    api.get(`/reports/department-performance/${departmentId}`).then((r) => r.data),
};

export default reportService;
