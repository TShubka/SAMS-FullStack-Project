import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import courseService from '../../services/courseService';
import enrollmentService from '../../services/enrollmentService';
import attendanceService from '../../services/attendanceService';
import useAuth from '../../hooks/useAuth';
import useFetch from '../../hooks/useFetch';
import { extractErrorMessage } from '../../services/api';
import Spinner from '../../components/common/Spinner';
import Alert from '../../components/common/Alert';
import EmptyState from '../../components/common/EmptyState';
import { ATTENDANCE_STATUS } from '../../utils/constants';

/**
 * Teacher (and admin) attendance marking. Owner: Member 3.
 *
 * A teacher works down the whole roster for one date and submits it in a single
 * bulk request - the backend records it in one transaction. If a date is already
 * recorded, the existing register is shown read-only so the teacher does not
 * accidentally double-submit; corrections go through the per-row edit.
 */
export default function AttendancePage() {
  const { isAdmin } = useAuth();
  const [params] = useSearchParams();
  const [courseId, setCourseId] = useState(params.get('courseId') || '');
  const [date, setDate] = useState(today());
  const [roster, setRoster] = useState([]); // [{ enrollmentId, name, roll, status }]
  const [existing, setExisting] = useState(null); // records already saved for this date
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [banner, setBanner] = useState(null);

  const { data: courses } = useFetch(
    () => (isAdmin ? courseService.list({ size: 200 }) : courseService.myCourses()),
    [isAdmin],
  );
  const courseList = isAdmin ? (courses?.content || []) : (courses || []);

  // Load the roster and any attendance already recorded whenever course or date changes.
  useEffect(() => {
    if (!courseId) { setRoster([]); setExisting(null); return; }
    let cancelled = false;
    setLoading(true);
    setBanner(null);
    Promise.all([
      enrollmentService.byCourse(courseId),
      attendanceService.byCourse(courseId, date),
    ])
      .then(([enrollments, recorded]) => {
        if (cancelled) return;
        if (recorded && recorded.length > 0) {
          setExisting(recorded);
          setRoster([]);
        } else {
          setExisting(null);
          setRoster(enrollments.map((e) => ({
            enrollmentId: e.id,
            name: e.studentName,
            roll: e.rollNumber,
            status: ATTENDANCE_STATUS.PRESENT,
          })));
        }
      })
      .catch((err) => !cancelled && setBanner({ type: 'error', text: extractErrorMessage(err) }))
      .finally(() => !cancelled && setLoading(false));
    return () => { cancelled = true; };
  }, [courseId, date]);

  const setStatus = (enrollmentId, status) =>
    setRoster((r) => r.map((row) => (row.enrollmentId === enrollmentId ? { ...row, status } : row)));

  const setAll = (status) => setRoster((r) => r.map((row) => ({ ...row, status })));

  const submit = async () => {
    setSaving(true);
    setBanner(null);
    try {
      await attendanceService.recordBulk({
        courseId: Number(courseId),
        attendanceDate: date,
        entries: roster.map((r) => ({ enrollmentId: r.enrollmentId, status: r.status })),
      });
      setBanner({ type: 'success', text: `Attendance saved for ${roster.length} student(s)` });
      // reload to show it as the now-existing register
      const recorded = await attendanceService.byCourse(courseId, date);
      setExisting(recorded);
      setRoster([]);
    } catch (err) {
      setBanner({ type: 'error', text: extractErrorMessage(err) });
    } finally {
      setSaving(false);
    }
  };

  return (
    <section>
      <h1>Record Attendance</h1>

      {banner && <Alert type={banner.type} onDismiss={() => setBanner(null)}>{banner.text}</Alert>}

      <div className="card">
        <div className="filter-row">
          <select value={courseId} onChange={(e) => setCourseId(e.target.value)}>
            <option value="">Select a course...</option>
            {courseList.map((c) => (
              <option key={c.id} value={c.id}>{c.code} — {c.title}</option>
            ))}
          </select>
          <input type="date" value={date} max={today()} onChange={(e) => setDate(e.target.value)} />
        </div>
      </div>

      {!courseId && <EmptyState title="Select a course and date to begin" />}

      {courseId && loading && <Spinner label="Loading roster..." />}

      {courseId && !loading && existing && (
        <div className="card">
          <Alert type="info">
            Attendance for this date is already recorded. Edit individual rows below.
          </Alert>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Roll</th><th>Student</th><th>Status</th></tr></thead>
              <tbody>
                {existing.map((r) => (
                  <ExistingRow key={r.id} record={r}
                    onSaved={(text) => setBanner({ type: 'success', text })} />
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {courseId && !loading && !existing && roster.length === 0 && (
        <EmptyState title="No students enrolled in this course" />
      )}

      {courseId && !loading && !existing && roster.length > 0 && (
        <div className="card">
          <div className="bulk-actions">
            <span className="muted">Mark all:</span>
            <button className="btn-link" onClick={() => setAll('PRESENT')}>Present</button>
            <button className="btn-link" onClick={() => setAll('ABSENT')}>Absent</button>
            <button className="btn-link" onClick={() => setAll('LATE')}>Late</button>
          </div>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Roll</th><th>Student</th><th>Status</th></tr></thead>
              <tbody>
                {roster.map((r) => (
                  <tr key={r.enrollmentId}>
                    <td>{r.roll}</td>
                    <td>{r.name}</td>
                    <td>
                      <StatusPicker value={r.status} onChange={(s) => setStatus(r.enrollmentId, s)} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <button className="btn btn-primary inline" style={{ marginTop: '1rem' }}
                  onClick={submit} disabled={saving}>
            {saving ? 'Saving...' : `Save attendance (${roster.length})`}
          </button>
        </div>
      )}
    </section>
  );
}

function StatusPicker({ value, onChange }) {
  return (
    <div className="status-picker">
      {['PRESENT', 'LATE', 'ABSENT'].map((s) => (
        <button key={s} type="button"
          className={`chip ${value === s ? `chip-${s.toLowerCase()}` : ''}`}
          onClick={() => onChange(s)}>
          {s[0] + s.slice(1).toLowerCase()}
        </button>
      ))}
    </div>
  );
}

function ExistingRow({ record, onSaved }) {
  const [status, setStatus] = useState(record.status);
  const [saving, setSaving] = useState(false);
  const dirty = status !== record.status;

  const save = async () => {
    setSaving(true);
    try {
      await attendanceService.update(record.id, {
        enrollmentId: record.enrollmentId,
        attendanceDate: record.attendanceDate,
        status,
      });
      onSaved(`Updated ${record.rollNumber}`);
      record.status = status;
    } finally {
      setSaving(false);
    }
  };

  return (
    <tr>
      <td>{record.rollNumber}</td>
      <td>{record.studentName}</td>
      <td>
        <div className="status-picker">
          <StatusPicker value={status} onChange={setStatus} />
          {dirty && (
            <button className="btn-link" onClick={save} disabled={saving}>
              {saving ? '...' : 'Save'}
            </button>
          )}
        </div>
      </td>
    </tr>
  );
}

function today() {
  return new Date().toISOString().slice(0, 10);
}
