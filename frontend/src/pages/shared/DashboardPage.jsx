import useAuth from '../../hooks/useAuth';

/**
 * PHASE 8 PLACEHOLDER. Owner: Member 4.
 *
 * Phase 10 replaces this with the three real role-based dashboards, each fed by
 * /api/dashboard/{admin,teacher,student}. Nothing here invents statistics - it
 * only echoes the session, so there is no risk of fake numbers reaching a demo.
 */
export default function DashboardPage() {
  const { user, isAdmin, isTeacher, isStudent } = useAuth();

  const role = isAdmin ? 'Administrator' : isTeacher ? 'Teacher' : isStudent ? 'Student' : 'User';

  return (
    <section>
      <h1>Dashboard</h1>
      <p className="muted">
        Signed in as <strong>{user?.username}</strong> ({role})
      </p>

      <div className="card">
        <h3>Phase 8 foundation</h3>
        <p>
          Authentication, routing and role-based navigation are in place. The
          role-specific dashboard widgets arrive in Phase 10 and will read live data
          from the reporting endpoints.
        </p>
        <ul>
          <li>User id: {user?.id}</li>
          <li>Email: {user?.email}</li>
          <li>Roles: {user?.roles?.join(', ')}</li>
        </ul>
      </div>
    </section>
  );
}
