import authService from '../../services/authService';
import useFetch from '../../hooks/useFetch';
import useAuth from '../../hooks/useAuth';
import Spinner from '../../components/common/Spinner';
import Alert from '../../components/common/Alert';

/** Read-only account profile for any signed-in user. Owner: Member 1. */
export default function ProfilePage() {
  const { user } = useAuth();
  const { data, loading, error } = useFetch(() => authService.getCurrentUser(), []);

  if (loading) return <Spinner />;
  if (error) return <Alert type="error">{error}</Alert>;

  const profile = data || user;

  return (
    <section>
      <h1>My Profile</h1>
      <div className="card">
        <dl className="profile-list">
          <dt>Username</dt><dd>{profile.username}</dd>
          <dt>Email</dt><dd>{profile.email}</dd>
          <dt>Account status</dt><dd>{profile.enabled === false ? 'Disabled' : 'Active'}</dd>
          <dt>Roles</dt>
          <dd>{(profile.roles || []).map((r) => (
            <span key={r} className="role-badge">{r.replace('ROLE_', '')}</span>
          ))}</dd>
        </dl>
      </div>
    </section>
  );
}
