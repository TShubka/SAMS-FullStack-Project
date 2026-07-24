import { Navigate, Route, Routes } from 'react-router-dom';

import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import DashboardPage from '../pages/shared/DashboardPage';
import UnauthorizedPage from '../pages/shared/UnauthorizedPage';
import NotFoundPage from '../pages/shared/NotFoundPage';

import MainLayout from '../layouts/MainLayout';
import ProtectedRoute from './ProtectedRoute';

/**
 * Route table. Owner: Member 1, extended by every member as their pages land.
 *
 * Structure:
 *   public            -> /login, /register
 *   ProtectedRoute    -> requires a session
 *     MainLayout      -> sidebar + topbar shell
 *       RoleRoute     -> requires specific roles (added per module in Phase 9)
 *
 * This file is shared, so members add their routes here rather than restructuring
 * it - it was flagged as a merge-conflict hotspot in the Phase 0 risk list.
 */
export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          {/*
            PHASE 9 adds the module routes here, each wrapped in a RoleRoute:
              /departments /students /teachers /courses /enrollments   (M2)
              /attendance  /marks                                      (M3)
              /transcript  /reports                                    (M4)
              /users       /profile                                    (M1)
          */}

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  );
}
