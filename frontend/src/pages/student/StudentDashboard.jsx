import { Link } from 'react-router-dom';
import dashboardService from '../../services/dashboardService';
import useFetch from '../../hooks/useFetch';
import Spinner from '../../components/common/Spinner';
import Alert from '../../components/common/Alert';
import { StatCard, GradeBadge } from './MyGradesPage';

/** Student overview - their own academic summary. Owner: Member 4. */
export default function StudentDashboard() {
  const { data, loading, error } = useFetch(() => dashboardService.student(), []);

  if (loading) return <Spinner label="Loading dashboard..." />;
  if (error) return <Alert type="error">{error}</Alert>;

  return (
    <section>
      <h1>Welcome, {data.studentName}</h1>
      <p className="muted">{data.rollNumber} · {data.departmentName} · Semester {data.currentSemester}</p>

      <div className="stat-row">
        <StatCard label="Cumulative GPA" value={data.cumulativeGpa ?? '—'} />
        <StatCard label="Enrolled Courses" value={data.enrolledCourses} />
        <StatCard label="Courses Passed" value={data.coursesPassed} />
        <StatCard label="Low Attendance" value={data.lowAttendanceCourses} />
      </div>

      {data.lowAttendanceCourses > 0 && (
        <Alert type="warning">
          You are below the attendance threshold in {data.lowAttendanceCourses} course(s).
          Check <Link to="/my-attendance">My Attendance</Link>.
        </Alert>
      )}

      <div className="card">
        <h3>My Courses</h3>
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>Code</th><th>Course</th><th>Credits</th><th>Grade</th></tr>
            </thead>
            <tbody>
              {data.courses.map((c) => (
                <tr key={c.enrollmentId}>
                  <td>{c.courseCode}</td>
                  <td>{c.courseTitle}</td>
                  <td>{c.credits}</td>
                  <td>{c.grade ? <GradeBadge grade={c.grade} /> : <span className="muted">In progress</span>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p className="muted" style={{ marginTop: '0.75rem' }}>
          <Link to="/my-grades">Full grades</Link> · <Link to="/transcript">Transcript</Link>
        </p>
      </div>
    </section>
  );
}
