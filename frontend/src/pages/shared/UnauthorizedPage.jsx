import { Link } from 'react-router-dom';

/** Shown when a logged-in user reaches a route their role does not allow. */
export default function UnauthorizedPage() {
  return (
    <div className="state-block">
      <h1>403</h1>
      <h2>Not authorized</h2>
      <p className="muted">
        Your account does not have permission to view this page.
      </p>
      <Link to="/dashboard" className="btn btn-primary">Back to dashboard</Link>
    </div>
  );
}
