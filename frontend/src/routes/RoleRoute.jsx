import { Navigate, Outlet } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import Spinner from '../components/common/Spinner';

/**
 * Blocks users who are logged in but hold the wrong role. Owner: Member 1.
 *
 * Sits inside ProtectedRoute, so by the time it runs the user is known to be
 * authenticated and only the role still needs checking.
 *
 * Again: this is UX, not security. The matching endpoint on the backend rejects
 * the request with 403 regardless of what the browser renders.
 */
export default function RoleRoute({ allowed = [] }) {
  const { hasRole, loading } = useAuth();

  if (loading) return <Spinner label="Checking permissions..." />;

  if (!hasRole(...allowed)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}
