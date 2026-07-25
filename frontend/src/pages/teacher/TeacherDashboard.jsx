import { Link } from 'react-router-dom';
import dashboardService from '../../services/dashboardService';
import useFetch from '../../hooks/useFetch';
import Spinner from '../../components/common/Spinner';
import Alert from '../../components/common/Alert';
import EmptyState from '../../components/common/EmptyState';
import { StatCard } from '../student/MyGradesPage';

/** Teacher overview - scoped to their assigned courses. Owner: Member 4. */
export default function TeacherDashboard() {
  const { data, loading, error } = useFetch(() => dashboardService.teacher(), []);

  if (loading) return <Spinner label="Loading dashboard..." />;
  if (error) return <Alert type="error">{error}</Alert>;

  return (
    <section>
      <h1>Teacher Dashboard</h1>
      <p className="muted">{data.teacherName} · {data.employeeCode} · {data.departmentName}</p>

      <div className="stat-row">
        <StatCard label="Assigned Courses" value={data.assignedCourses} />
        <StatCard label="Total Students" value={data.totalStudents} />
        <StatCard label="Low Attendance" value={data.lowAttendanceStudents} />
      </div>

      <div className="card">
        <h3>My Courses</h3>
        {(!data.courses || data.courses.length === 0) ? (
          <EmptyState title="No courses assigned yet" />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Code</th><th>Title</th><th>Credits</th><th>Semester</th><th></th></tr>
              </thead>
              <tbody>
                {data.courses.map((c) => (
                  <tr key={c.id}>
                    <td>{c.code}</td>
                    <td>{c.title}</td>
                    <td>{c.credits}</td>
                    <td>{c.semester}</td>
                    <td>
                      <Link className="btn-link" to={`/attendance?courseId=${c.id}`}>Attendance</Link>
                      <Link className="btn-link" to={`/marks?courseId=${c.id}`}>Marks</Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}
