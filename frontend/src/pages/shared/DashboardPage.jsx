import useAuth from '../../hooks/useAuth';
import AdminDashboard from '../admin/AdminDashboard';
import TeacherDashboard from '../teacher/TeacherDashboard';
import StudentDashboard from '../student/StudentDashboard';

/**
 * Dispatches to the correct role dashboard. Owner: Member 4.
 *
 * Admin is checked first so a user who somehow holds several roles gets the
 * broadest view. Every dashboard's data comes from its own /api/dashboard/*
 * endpoint - no figure on any of them is hard-coded.
 */
export default function DashboardPage() {
  const { isAdmin, isTeacher, isStudent } = useAuth();

  if (isAdmin) return <AdminDashboard />;
  if (isTeacher) return <TeacherDashboard />;
  if (isStudent) return <StudentDashboard />;

  return (
    <section>
      <h1>Dashboard</h1>
      <p className="muted">No dashboard is available for this account.</p>
    </section>
  );
}
