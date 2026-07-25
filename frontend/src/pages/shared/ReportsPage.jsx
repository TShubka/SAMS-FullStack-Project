import { useState } from 'react';
import reportService from '../../services/reportService';
import courseService from '../../services/courseService';
import useFetch from '../../hooks/useFetch';
import useAuth from '../../hooks/useAuth';
import { extractErrorMessage } from '../../services/api';
import Spinner from '../../components/common/Spinner';
import Alert from '../../components/common/Alert';
import EmptyState from '../../components/common/EmptyState';
import SimpleBarChart from '../../components/common/SimpleBarChart';

/**
 * Reports console. Owner: Member 4.
 *
 * A teacher sees course reports for their own courses (the backend enforces this);
 * an admin additionally sees the institution-wide reports.
 */
export default function ReportsPage() {
  const { isAdmin } = useAuth();
  const [courseId, setCourseId] = useState('');
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const { data: courses } = useFetch(
    () => (isAdmin ? courseService.list({ size: 200 }) : courseService.myCourses()),
    [isAdmin],
  );
  const courseList = isAdmin ? (courses?.content || []) : (courses || []);

  const run = async (kind) => {
    if (kind !== 'students-by-dept' && !courseId) {
      setError('Select a course first');
      return;
    }
    setLoading(true);
    setError('');
    setReport(null);
    try {
      let data;
      if (kind === 'course-performance') data = { kind, ...(await reportService.coursePerformance(courseId)) };
      else if (kind === 'grade-distribution') data = { kind, ...(await reportService.gradeDistribution(courseId)) };
      else if (kind === 'pass-fail') data = { kind, ...(await reportService.passFail(courseId)) };
      else if (kind === 'attendance') data = { kind, rows: await reportService.attendanceByCourse(courseId) };
      else if (kind === 'students-by-dept') data = { kind, ...(await reportService.studentsByDepartment()) };
      setReport(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <section>
      <h1>Reports</h1>

      {error && <Alert type="error" onDismiss={() => setError('')}>{error}</Alert>}

      <div className="card">
        <div className="filter-row">
          <select value={courseId} onChange={(e) => setCourseId(e.target.value)}>
            <option value="">Select a course...</option>
            {courseList.map((c) => (
              <option key={c.id} value={c.id}>{c.code} — {c.title}</option>
            ))}
          </select>
        </div>
        <div className="report-buttons">
          <button className="btn btn-secondary" onClick={() => run('course-performance')}>Course Performance</button>
          <button className="btn btn-secondary" onClick={() => run('grade-distribution')}>Grade Distribution</button>
          <button className="btn btn-secondary" onClick={() => run('pass-fail')}>Pass / Fail</button>
          <button className="btn btn-secondary" onClick={() => run('attendance')}>Attendance</button>
          {isAdmin && (
            <button className="btn btn-secondary" onClick={() => run('students-by-dept')}>Students by Department</button>
          )}
        </div>
      </div>

      {loading && <Spinner label="Running report..." />}
      {report && <ReportView report={report} />}
    </section>
  );
}

function ReportView({ report }) {
  if (report.kind === 'students-by-dept') {
    return (
      <div className="card">
        <h3>Students by Department ({report.totalStudents} total)</h3>
        <SimpleBarChart data={report.departments.map((d) => ({ label: d.departmentCode, value: d.studentCount }))} />
      </div>
    );
  }

  if (report.kind === 'grade-distribution') {
    return (
      <div className="card">
        <h3>Grade Distribution — {report.courseCode}</h3>
        <p className="muted">{report.gradedStudents} graded, {report.ungradedStudents} in progress</p>
        <SimpleBarChart data={report.distribution.map((b) => ({ label: b.grade, value: b.count }))} />
      </div>
    );
  }

  if (report.kind === 'pass-fail') {
    return (
      <div className="card">
        <h3>Pass / Fail — {report.courseCode}</h3>
        <div className="stat-row">
          <div className="stat-card"><span className="stat-value">{report.passed}</span><span className="stat-label">Passed</span></div>
          <div className="stat-card"><span className="stat-value">{report.failed}</span><span className="stat-label">Failed</span></div>
          <div className="stat-card"><span className="stat-value">{report.passRate ?? '—'}%</span><span className="stat-label">Pass Rate</span></div>
        </div>
      </div>
    );
  }

  if (report.kind === 'course-performance') {
    return (
      <div className="card">
        <h3>Course Performance — {report.courseCode}</h3>
        <p className="muted">
          {report.graded}/{report.enrolled} graded · avg {report.averagePercentage ?? '—'}% ·
          high {report.highestPercentage ?? '—'}% · low {report.lowestPercentage ?? '—'}%
        </p>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Roll</th><th>Student</th><th>%</th><th>Grade</th></tr></thead>
            <tbody>
              {report.students.map((s) => (
                <tr key={s.enrollmentId}>
                  <td>{s.rollNumber}</td><td>{s.studentName}</td>
                  <td>{s.percentage ?? '—'}</td><td>{s.grade ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  if (report.kind === 'attendance') {
    if (!report.rows || report.rows.length === 0) return <EmptyState title="No attendance data" />;
    return (
      <div className="card">
        <h3>Attendance Summary</h3>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Roll</th><th>Student</th><th>Present</th><th>Total</th><th>%</th></tr></thead>
            <tbody>
              {report.rows.map((r) => (
                <tr key={r.enrollmentId}>
                  <td>{r.rollNumber}</td><td>{r.studentName}</td>
                  <td>{r.attendedCount}</td><td>{r.totalSessions}</td>
                  <td>{r.percentage ?? '—'}{r.percentage != null ? '%' : ''}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  return null;
}
