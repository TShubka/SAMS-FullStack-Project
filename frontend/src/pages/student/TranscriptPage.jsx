import reportService from '../../services/reportService';
import useFetch from '../../hooks/useFetch';
import Spinner from '../../components/common/Spinner';
import Alert from '../../components/common/Alert';
import EmptyState from '../../components/common/EmptyState';
import { GradeBadge } from './MyGradesPage';

/** The student's own academic transcript. Owner: Member 4. */
export default function TranscriptPage() {
  const { data: t, loading, error } = useFetch(() => reportService.myTranscript(), []);

  if (loading) return <Spinner label="Loading transcript..." />;
  if (error) return <Alert type="error">{error}</Alert>;
  if (!t) return <EmptyState title="No transcript available" />;

  return (
    <section>
      <h1>Academic Transcript</h1>

      <div className="card transcript-header">
        <div><strong>{t.studentName}</strong><br /><span className="muted">{t.rollNumber}</span></div>
        <div>{t.departmentName} ({t.departmentCode})<br /><span className="muted">Admitted {t.admissionYear}</span></div>
        <div className="transcript-gpa">
          <span className="stat-value">{t.cumulativeGpa ?? '—'}</span>
          <span className="stat-label">Cumulative GPA</span>
        </div>
      </div>

      {(!t.semesters || t.semesters.length === 0) && (
        <EmptyState title="No courses on record yet" />
      )}

      {t.semesters?.map((sem) => (
        <div className="card" key={`${sem.academicYear}-${sem.semester}`}>
          <div className="semester-head">
            <h3>Semester {sem.semester} — {sem.academicYear}</h3>
            <span className="muted">
              {sem.semesterCredits} credits · GPA {sem.semesterGpa ?? '—'}
            </span>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Code</th><th>Course</th><th>Credits</th>
                  <th>Percentage</th><th>Grade</th><th>Points</th><th>Status</th>
                </tr>
              </thead>
              <tbody>
                {sem.courses.map((c) => (
                  <tr key={c.courseCode}>
                    <td>{c.courseCode}</td>
                    <td>{c.courseTitle}</td>
                    <td>{c.credits}</td>
                    <td>{c.percentage != null ? `${c.percentage}%` : '—'}</td>
                    <td>{c.grade ? <GradeBadge grade={c.grade} /> : '—'}</td>
                    <td>{c.gradePoints ?? '—'}</td>
                    <td><StatusBadge status={c.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}

      <div className="card transcript-footer">
        <span>Total credits: <strong>{t.totalCredits}</strong></span>
        <span>Credits earned: <strong>{t.creditsEarned}</strong></span>
        <span>Cumulative GPA: <strong>{t.cumulativeGpa ?? '—'}</strong></span>
      </div>
    </section>
  );
}

function StatusBadge({ status }) {
  const map = { PASS: 'grade-top', FAIL: 'grade-fail', IN_PROGRESS: 'grade-mid' };
  return <span className={`grade-badge ${map[status] || 'grade-mid'}`}>{status.replace('_', ' ')}</span>;
}
