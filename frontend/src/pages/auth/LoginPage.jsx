import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import { extractErrorMessage } from '../../services/api';
import Alert from '../../components/common/Alert';

/**
 * Login screen. Owner: Member 1.
 *
 * Client-side validation runs first so obvious mistakes never reach the network,
 * but the server validates again - client checks are for the user's convenience,
 * not for correctness.
 */
export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [form, setForm] = useState({ username: '', password: '' });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const redirectTo = location.state?.from?.pathname || '/dashboard';

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: undefined });
  };

  const validate = () => {
    const next = {};
    if (!form.username.trim()) next.username = 'Username is required';
    if (!form.password) next.password = 'Password is required';
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;

    setSubmitting(true);
    try {
      await login(form);
      navigate(redirectTo, { replace: true });
    } catch (error) {
      // 401 here means bad credentials. We deliberately do not say whether it was
      // the username or the password that was wrong - that would let an attacker
      // enumerate valid usernames.
      setServerError(
        error.response?.status === 401
          ? 'Invalid username or password'
          : extractErrorMessage(error, 'Login failed'),
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit} noValidate>
        <h1>Sign in</h1>
        <p className="muted">Student Academic Records &amp; Attendance Management</p>

        <Alert type="error" onDismiss={() => setServerError('')}>
          {serverError}
        </Alert>

        <label htmlFor="username">Username</label>
        <input
          id="username"
          name="username"
          value={form.username}
          onChange={handleChange}
          autoComplete="username"
          disabled={submitting}
        />
        {errors.username && <span className="field-error">{errors.username}</span>}

        <label htmlFor="password">Password</label>
        <input
          id="password"
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
          autoComplete="current-password"
          disabled={submitting}
        />
        {errors.password && <span className="field-error">{errors.password}</span>}

        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Signing in...' : 'Sign in'}
        </button>

        <p className="muted center">
          No account? <Link to="/register">Register</Link>
        </p>
      </form>
    </div>
  );
}
