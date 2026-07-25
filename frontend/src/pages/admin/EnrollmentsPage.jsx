import { useState } from 'react';
import enrollmentService from '../../services/enrollmentService';
import studentService from '../../services/studentService';
import courseService from '../../services/courseService';
import useFetch from '../../hooks/useFetch';
import { extractErrorMessage } from '../../services/api';
import DataTable from '../../components/common/DataTable';
import Pagination from '../../components/common/Pagination';
import Modal from '../../components/common/Modal';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import Alert from '../../components/common/Alert';

/** Enrollment CRUD. Owner: Member 2. */
export default function EnrollmentsPage() {
  const [page, setPage] = useState(0);
  const [creating, setCreating] = useState(false);
  const [deleting, setDeleting] = useState(null);
  const [banner, setBanner] = useState(null);

  const { data, loading, error, reload } = useFetch(
    () => enrollmentService.list({ page, size: 10 }),
    [page],
  );

  return (
    <section>
      <div className="page-header">
        <h1>Enrollments</h1>
        <button type="button" className="btn btn-primary inline" onClick={() => setCreating(true)}>
          + New Enrollment
        </button>
      </div>

      {banner && <Alert type={banner.type} onDismiss={() => setBanner(null)}>{banner.text}</Alert>}

      <DataTable
        loading={loading}
        error={error}
        rows={data?.content}
        emptyTitle="No enrollments yet"
        columns={[
          { key: 'rollNumber', label: 'Roll No' },
          { key: 'studentName', label: 'Student' },
          { key: 'courseCode', label: 'Course' },
          { key: 'courseTitle', label: 'Title' },
          { key: 'semester', label: 'Sem' },
          { key: 'academicYear', label: 'Year' },
          { key: 'status', label: 'Status' },
        ]}
        actions={(row) => (
          <button type="button" className="btn-link danger" onClick={() => setDeleting(row)}>Remove</button>
        )}
      />

      <Pagination page={data?.page ?? 0} totalPages={data?.totalPages ?? 0} onChange={setPage} />

      {creating && (
        <EnrollmentForm
          onClose={() => setCreating(false)}
          onSaved={(text) => { setCreating(false); setBanner({ type: 'success', text }); reload(); }} />
      )}

      {deleting && (
        <ConfirmDialog
          title="Remove enrollment"
          message={`Remove ${deleting.studentName} from ${deleting.courseCode}? Their attendance and marks for this course are removed too.`}
          onCancel={() => setDeleting(null)}
          onConfirm={async () => {
            try {
              await enrollmentService.remove(deleting.id);
              setBanner({ type: 'success', text: 'Enrollment removed' });
              reload();
            } catch (err) {
              setBanner({ type: 'error', text: extractErrorMessage(err) });
            } finally { setDeleting(null); }
          }} />
      )}
    </section>
  );
}

function EnrollmentForm({ onClose, onSaved }) {
  const { data: students } = useFetch(() => studentService.list({ size: 200 }), []);
  const { data: courses } = useFetch(() => courseService.list({ size: 200 }), []);
  const [form, setForm] = useState({
    studentId: '', courseId: '', semester: 1,
    academicYear: '2025-2026', status: 'ACTIVE',
  });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [saving, setSaving] = useState(false);

  const change = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: undefined });
  };

  const validate = () => {
    const n = {};
    if (!form.studentId) n.studentId = 'Student is required';
    if (!form.courseId) n.courseId = 'Course is required';
    if (!/^\d{4}-\d{4}$/.test(form.academicYear)) n.academicYear = 'Format: 2025-2026';
    setErrors(n);
    return Object.keys(n).length === 0;
  };

  const submit = async (e) => {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;
    setSaving(true);
    try {
      await enrollmentService.create({
        studentId: Number(form.studentId), courseId: Number(form.courseId),
        semester: Number(form.semester), academicYear: form.academicYear, status: form.status,
      });
      onSaved('Enrollment created');
    } catch (err) {
      setServerError(extractErrorMessage(err));
    } finally { setSaving(false); }
  };

  return (
    <Modal title="New enrollment" onClose={onClose}>
      <form onSubmit={submit} noValidate>
        <Alert type="error" onDismiss={() => setServerError('')}>{serverError}</Alert>

        <label htmlFor="studentId">Student</label>
        <select id="studentId" name="studentId" value={form.studentId} onChange={change} disabled={saving}>
          <option value="">Select...</option>
          {students?.content?.map((s) => (
            <option key={s.id} value={s.id}>{s.rollNumber} — {s.fullName}</option>
          ))}
        </select>
        {errors.studentId && <span className="field-error">{errors.studentId}</span>}

        <label htmlFor="courseId">Course</label>
        <select id="courseId" name="courseId" value={form.courseId} onChange={change} disabled={saving}>
          <option value="">Select...</option>
          {courses?.content?.map((c) => (
            <option key={c.id} value={c.id}>{c.code} — {c.title}</option>
          ))}
        </select>
        {errors.courseId && <span className="field-error">{errors.courseId}</span>}

        <div className="form-row">
          <div>
            <label htmlFor="semester">Semester</label>
            <input id="semester" name="semester" type="number" value={form.semester} onChange={change} disabled={saving} />
          </div>
          <div>
            <label htmlFor="academicYear">Academic year</label>
            <input id="academicYear" name="academicYear" value={form.academicYear} onChange={change} disabled={saving} />
            {errors.academicYear && <span className="field-error">{errors.academicYear}</span>}
          </div>
        </div>

        <label htmlFor="status">Status</label>
        <select id="status" name="status" value={form.status} onChange={change} disabled={saving}>
          <option value="ACTIVE">Active</option>
          <option value="COMPLETED">Completed</option>
          <option value="DROPPED">Dropped</option>
        </select>

        <div className="modal-actions">
          <button type="button" className="btn btn-secondary" onClick={onClose} disabled={saving}>Cancel</button>
          <button type="submit" className="btn btn-primary inline" disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
