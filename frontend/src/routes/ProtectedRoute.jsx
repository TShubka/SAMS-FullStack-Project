import { Navigate, Outlet, useLocation } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import Spinner from '../components/common/Spinner';

/**
 * Blocks unauthenticated users. Owner: Member 1.
 *
 * The `loading` check matters: without it, a page refresh would briefly see
 * user === null before localStorage is read and would bounce a logged-in user to
 * the login screen.
 *
 * `state={{ from: location }}` lets the login page send the user back where they
 * were trying to go.
 */
export default function ProtectedRoute() {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) return <Spinner label="Checking your session..." />;

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return <Outlet />;
}
