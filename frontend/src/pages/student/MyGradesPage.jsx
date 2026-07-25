import gradeService from '../../services/gradeService';
import useFetch from '../../hooks/useFetch';
import DataTable from '../../components/common/DataTable';
import Spinner from '../../components/common/Spinner';
import Alert from '../../components/common/Alert';

/** A student's own grades and GPA. Owner: Member 3 / Member 4. */
export default function MyGradesPage() {
  const { data: grades, loading, error } = useFetch(() => gradeService.myGrades(), []);
  const { data: gpa } = useFetch(() => gradeService.myGpa(), []);

  return (
    <section>
      <h1>My Grades</h1>

      {gpa && (
        <div className="stat-row">
          <StatCard label="Cumulative GPA" value={gpa.gpa ?? '—'} />
          <StatCard label="Graded Courses" value={gpa.gradedCourses} />
          <StatCard label="In Progress" value={gpa.ungradedCourses} />
          <StatCard label="Total Credits" value={gpa.totalCredits} />
        </div>
      )}

      <DataTable
        loading={loading}
        error={error}
        rows={grades}
        rowKey={(r) => r.enrollmentId}
        emptyTitle="No grades yet"
        emptyMessage="Your grades will appear here once your teachers enter marks."
        columns={[
          { key: 'courseCode', label: 'Code' },
          { key: 'courseTitle', label: 'Course' },
          { key: 'credits', label: 'Credits' },
          { key: 'percentage', label: 'Percentage', render: (r) => r.percentage != null ? `${r.percentage}%` : <span className="muted">In progress</span> },
          { key: 'grade', label: 'Grade', render: (r) => r.grade ? <GradeBadge grade={r.grade} /> : <span className="muted">—</span> },
          { key: 'gradePoints', label: 'Points', render: (r) => r.gradePoints ?? '—' },
        ]}
      />
    </section>
  );
}

export function StatCard({ label, value }) {
  return (
    <div className="stat-card">
      <span className="stat-value">{value}</span>
      <span className="stat-label">{label}</span>
    </div>
  );
}

export function GradeBadge({ grade }) {
  const cls = grade === 'F' ? 'grade-fail' : grade?.startsWith('A') ? 'grade-top' : 'grade-mid';
  return <span className={`grade-badge ${cls}`}>{grade}</span>;
}
