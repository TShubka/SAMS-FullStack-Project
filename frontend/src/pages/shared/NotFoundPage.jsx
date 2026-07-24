import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="state-block">
      <h1>404</h1>
      <h2>Page not found</h2>
      <Link to="/dashboard" className="btn btn-primary">Back to dashboard</Link>
    </div>
  );
}
