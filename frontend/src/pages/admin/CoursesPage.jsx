import { useState } from 'react';
import courseService from '../../services/courseService';
import departmentService from '../../services/departmentService';
import teacherService from '../../services/teacherService';
import useFetch from '../../hooks/useFetch';
import { extractErrorMessage } from '../../services/api';
import useAuth from '../../hooks/useAuth';
import DataTable from '../../components/common/DataTable';
import Pagination from '../../components/common/Pagination';
import SearchBar from '../../components/common/SearchBar';
import Modal from '../../components/common/Modal';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import Alert from '../../components/common/Alert';

/** Course CRUD with department and semester filters. Owner: Member 2. */
export default function CoursesPage() {
  const { isAdmin } = useAuth();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [semester, setSemester] = useState('');
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [banner, setBanner] = useState(null);

  const { data: depts } = useFetch(() => departmentService.list({ size: 100 }), []);
  const { data: teachers } = useFetch(() => teacherService.list({ size: 100 }), []);
  const { data, loading, error, reload } = useFetch(
    () => courseService.list({
      page, size: 10, search,
      ...(departmentId ? { departmentId } : {}),
      ...(semester ? { semester } : {}),
    }),
    [page, search, departmentId, semester],
  );

  return (
    <section>
      <div className="page-header">
        <h1>Courses</h1>
        {isAdmin && (
          <button type="button" className="btn btn-primary inline" onClick={() => setEditing({})}>
            + New Course
          </button>
        )}
      </div>

      {banner && <Alert type={banner.type} onDismiss={() => setBanner(null)}>{banner.text}</Alert>}

      <div className="filter-row">
        <SearchBar onSearch={(t) => { setPage(0); setSearch(t); }} placeholder="Search title or code..." />
        <select value={departmentId} onChange={(e) => { setPage(0); setDepartmentId(e.target.value); }}>
          <option value="">All departments</option>
          {depts?.content?.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
        <select value={semester} onChange={(e) => { setPage(0); setSemester(e.target.value); }}>
          <option value="">All semesters</option>
          {[...Array(12)].map((_, i) => <option key={i + 1} value={i + 1}>Semester {i + 1}</option>)}
        </select>
      </div>

      <DataTable
        loading={loading}
        error={error}
        rows={data?.content}
        emptyTitle="No courses found"
        columns={[
          { key: 'code', label: 'Code' },
          { key: 'title', label: 'Title' },
          { key: 'departmentName', label: 'Department' },
          { key: 'credits', label: 'Credits' },
          { key: 'semester', label: 'Semester' },
          { key: 'teacherName', label: 'Teacher', render: (r) => r.teacherName || <span className="muted">Unassigned</span> },
        ]}
        actions={isAdmin ? (row) => (
          <>
            <button type="button" className="btn-link" onClick={() => setEditing(row)}>Edit</button>
            <button type="button" className="btn-link danger" onClick={() => setDeleting(row)}>Delete</button>
          </>
        ) : undefined}
      />

      <Pagination page={data?.page ?? 0} totalPages={data?.totalPages ?? 0} onChange={setPage} />

      {editing && (
        <CourseForm initial={editing} departments={depts?.content || []} teachers={teachers?.content || []}
          onClose={() => setEditing(null)}
          onSaved={(text) => { setEditing(null); setBanner({ type: 'success', text }); reload(); }} />
      )}

      {deleting && (
        <ConfirmDialog
          title="Delete course"
          message={`Delete ${deleting.code} - ${deleting.title}?`}
          onCancel={() => setDeleting(null)}
          onConfirm={async () => {
            try {
              await courseService.remove(deleting.id);
              setBanner({ type: 'success', text: `Course ${deleting.code} deleted` });
              reload();
            } catch (err) {
              setBanner({ type: 'error', text: extractErrorMessage(err) });
            } finally { setDeleting(null); }
          }} />
      )}
    </section>
  );
}

function CourseForm({ initial, departments, teachers, onClose, onSaved }) {
  const isEdit = Boolean(initial.id);
  const [form, setForm] = useState({
    code: initial.code || '',
    title: initial.title || '',
    departmentId: initial.departmentId || '',
    teacherId: initial.teacherId || '',
    credits: initial.credits || 3,
    semester: initial.semester || 1,
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
    if (!form.code.trim()) n.code = 'Code is required';
    if (!form.title.trim()) n.title = 'Title is required';
    if (!form.departmentId) n.departmentId = 'Department is required';
    setErrors(n);
    return Object.keys(n).length === 0;
  };

  const submit = async (e) => {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;
    setSaving(true);
    const payload = {
      code: form.code, title: form.title,
      departmentId: Number(form.departmentId),
      teacherId: form.teacherId ? Number(form.teacherId) : null,
      credits: Number(form.credits), semester: Number(form.semester),
    };
    try {
      if (isEdit) { await courseService.update(initial.id, payload); onSaved('Course updated'); }
      else { await courseService.create(payload); onSaved('Course created'); }
    } catch (err) {
      setServerError(extractErrorMessage(err));
    } finally { setSaving(false); }
  };

  return (
    <Modal title={isEdit ? 'Edit course' : 'New course'} onClose={onClose}>
      <form onSubmit={submit} noValidate>
        <Alert type="error" onDismiss={() => setServerError('')}>{serverError}</Alert>

        <div className="form-row">
          <div>
            <label htmlFor="code">Code</label>
            <input id="code" name="code" value={form.code} onChange={change} disabled={saving} />
            {errors.code && <span className="field-error">{errors.code}</span>}
          </div>
          <div>
            <label htmlFor="credits">Credits</label>
            <input id="credits" name="credits" type="number" value={form.credits} onChange={change} disabled={saving} />
          </div>
        </div>

        <label htmlFor="title">Title</label>
        <input id="title" name="title" value={form.title} onChange={change} disabled={saving} />
        {errors.title && <span className="field-error">{errors.title}</span>}

        <label htmlFor="departmentId">Department</label>
        <select id="departmentId" name="departmentId" value={form.departmentId} onChange={change} disabled={saving}>
          <option value="">Select...</option>
          {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
        {errors.departmentId && <span className="field-error">{errors.departmentId}</span>}

        <div className="form-row">
          <div>
            <label htmlFor="teacherId">Teacher (optional)</label>
            <select id="teacherId" name="teacherId" value={form.teacherId} onChange={change} disabled={saving}>
              <option value="">Unassigned</option>
              {teachers.map((t) => <option key={t.id} value={t.id}>{t.fullName} ({t.employeeCode})</option>)}
            </select>
          </div>
          <div>
            <label htmlFor="semester">Semester</label>
            <input id="semester" name="semester" type="number" value={form.semester} onChange={change} disabled={saving} />
          </div>
        </div>

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
