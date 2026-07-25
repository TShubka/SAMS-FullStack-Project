import api from './api';

/** Role-based dashboard data. Owner: Member 4. */
const dashboardService = {
  admin: () => api.get('/dashboard/admin').then((r) => r.data),
  teacher: () => api.get('/dashboard/teacher').then((r) => r.data),
  student: () => api.get('/dashboard/student').then((r) => r.data),
};

export default dashboardService;
