import { Navigate, Route, Routes } from 'react-router-dom';

import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import DashboardPage from '../pages/shared/DashboardPage';
import UnauthorizedPage from '../pages/shared/UnauthorizedPage';
import NotFoundPage from '../pages/shared/NotFoundPage';
import ProfilePage from '../pages/shared/ProfilePage';

import DepartmentsPage from '../pages/admin/DepartmentsPage';
import StudentsPage from '../pages/admin/StudentsPage';
import TeachersPage from '../pages/admin/TeachersPage';
import CoursesPage from '../pages/admin/CoursesPage';
import EnrollmentsPage from '../pages/admin/EnrollmentsPage';

import MyGradesPage from '../pages/student/MyGradesPage';
import MyAttendancePage from '../pages/student/MyAttendancePage';
import TranscriptPage from '../pages/student/TranscriptPage';

import MainLayout from '../layouts/MainLayout';
import ProtectedRoute from './ProtectedRoute';
import RoleRoute from './RoleRoute';
import { ROLES } from '../utils/constants';

const { ADMIN, TEACHER, STUDENT } = ROLES;

/**
 * Route table. Owner: Member 1, extended per module.
 *
 * ProtectedRoute -> requires a session. MainLayout -> shell. RoleRoute -> role gate.
 * The route guards mirror the backend @PreAuthorize rules, but the backend is the
 * real control: these guards only decide what to render.
 */
export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          {/* Member 2 — admin manages, teacher reads where allowed */}
          <Route element={<RoleRoute allowed={[ADMIN]} />}>
            <Route path="/departments" element={<DepartmentsPage />} />
            <Route path="/teachers" element={<TeachersPage />} />
            <Route path="/enrollments" element={<EnrollmentsPage />} />
          </Route>
          <Route element={<RoleRoute allowed={[ADMIN, TEACHER]} />}>
            <Route path="/students" element={<StudentsPage />} />
            <Route path="/courses" element={<CoursesPage />} />
          </Route>

          {/* Member 3 / 4 — student self-service */}
          <Route element={<RoleRoute allowed={[STUDENT]} />}>
            <Route path="/my-attendance" element={<MyAttendancePage />} />
            <Route path="/my-grades" element={<MyGradesPage />} />
            <Route path="/transcript" element={<TranscriptPage />} />
          </Route>

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  );
}
