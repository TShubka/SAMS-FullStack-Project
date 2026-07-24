import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import { extractErrorMessage } from '../../services/api';
import Alert from '../../components/common/Alert';

/**
 * Registration screen. Owner: Member 1.
 *
 * The role selector offers STUDENT and TEACHER only. ADMIN is absent because the
 * backend refuses to self-register administrators - leaving it in the dropdown
 * would just produce a guaranteed error.
 */
export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'STUDENT',
  });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [success, setSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: undefined });
  };

  const validate = () => {
    const next = {};

    if (!form.username.trim()) {
      next.username = 'Username is required';
    } else if (form.username.trim().length < 3) {
      next.username = 'Username must be at least 3 characters';
    } else if (!/^[a-zA-Z0-9._-]+$/.test(form.username)) {
      next.username = 'Only letters, digits, dot, underscore and hyphen are allowed';
    }

    if (!form.email.trim()) {
      next.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      next.email = 'Enter a valid email address';
    }

    if (!form.password) {
      next.password = 'Password is required';
    } else if (form.password.length < 8) {
      next.password = 'Password must be at least 8 characters';
    }

    if (form.password !== form.confirmPassword) {
      next.confirmPassword = 'Passwords do not match';
    }

    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;

    setSubmitting(true);
    try {
      // confirmPassword is a client-side concern only and is not sent.
      await register({
        username: form.username,
        email: form.email,
        password: form.password,
        role: form.role,
      });
      setSuccess('Account created. Redirecting to sign in...');
      setTimeout(() => navigate('/login'), 1200);
    } catch (error) {
      setServerError(extractErrorMessage(error, 'Registration failed'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit} noValidate>
        <h1>Create account</h1>

        <Alert type="error" onDismiss={() => setServerError('')}>{serverError}</Alert>
        <Alert type="success">{success}</Alert>

        <label htmlFor="username">Username</label>
        <input id="username" name="username" value={form.username}
               onChange={handleChange} disabled={submitting} />
        {errors.username && <span className="field-error">{errors.username}</span>}

        <label htmlFor="email">Email</label>
        <input id="email" name="email" type="email" value={form.email}
               onChange={handleChange} disabled={submitting} />
        {errors.email && <span className="field-error">{errors.email}</span>}

        <label htmlFor="password">Password</label>
        <input id="password" name="password" type="password" value={form.password}
               onChange={handleChange} autoComplete="new-password" disabled={submitting} />
        {errors.password && <span className="field-error">{errors.password}</span>}

        <label htmlFor="confirmPassword">Confirm password</label>
        <input id="confirmPassword" name="confirmPassword" type="password"
               value={form.confirmPassword} onChange={handleChange}
               autoComplete="new-password" disabled={submitting} />
        {errors.confirmPassword && (
          <span className="field-error">{errors.confirmPassword}</span>
        )}

        <label htmlFor="role">Role</label>
        <select id="role" name="role" value={form.role}
                onChange={handleChange} disabled={submitting}>
          <option value="STUDENT">Student</option>
          <option value="TEACHER">Teacher</option>
        </select>

        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Creating account...' : 'Register'}
        </button>

        <p className="muted center">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </form>
    </div>
  );
}
