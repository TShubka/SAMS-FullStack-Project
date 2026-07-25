import dashboardService from '../../services/dashboardService';
import useFetch from '../../hooks/useFetch';
import Spinner from '../../components/common/Spinner';
import Alert from '../../components/common/Alert';
import SimpleBarChart from '../../components/common/SimpleBarChart';
import { StatCard } from '../student/MyGradesPage';

/** Admin overview - every figure is a live count. Owner: Member 4. */
export default function AdminDashboard() {
  const { data, loading, error } = useFetch(() => dashboardService.admin(), []);

  if (loading) return <Spinner label="Loading dashboard..." />;
  if (error) return <Alert type="error">{error}</Alert>;

  return (
    <section>
      <h1>Admin Dashboard</h1>

      <div className="stat-row">
        <StatCard label="Students" value={data.totalStudents} />
        <StatCard label="Teachers" value={data.totalTeachers} />
        <StatCard label="Departments" value={data.totalDepartments} />
        <StatCard label="Courses" value={data.totalCourses} />
      </div>
      <div className="stat-row">
        <StatCard label="Enrollments" value={data.totalEnrollments} />
        <StatCard label="Low Attendance" value={data.lowAttendanceCount} />
      </div>

      <div className="card">
        <h3>Students by Department</h3>
        <SimpleBarChart
          data={(data.studentsByDepartment || []).map((d) => ({
            label: `${d.departmentCode}`,
            value: d.studentCount,
          }))}
        />
      </div>
    </section>
  );
}
