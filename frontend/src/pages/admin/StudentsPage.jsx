import { useState } from 'react';
import studentService from '../../services/studentService';
import departmentService from '../../services/departmentService';
import useFetch from '../../hooks/useFetch';
import { extractErrorMessage } from '../../services/api';
import useAuth from '../../hooks/useAuth';
import DataTable from '../../components/common/DataTable';
import Pagination from '../../components/common/Pagination';
import SearchBar from '../../components/common/SearchBar';
import Modal from '../../components/common/Modal';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import Alert from '../../components/common/Alert';

/** Student CRUD, search and department filter. Owner: Member 2. */
export default function StudentsPage() {
  const { isAdmin } = useAuth();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [banner, setBanner] = useState(null);

  const { data: depts } = useFetch(() => departmentService.list({ size: 100 }), []);
  const { data, loading, error, reload } = useFetch(
    () => studentService.list({
      page, size: 10, search,
      ...(departmentId ? { departmentId } : {}),
    }),
    [page, search, departmentId],
  );

  return (
    <section>
      <div className="page-header">
        <h1>Students</h1>
        {isAdmin && (
          <button type="button" className="btn btn-primary inline" onClick={() => setEditing({})}>
            + New Student
          </button>
        )}
      </div>

      {banner && <Alert type={banner.type} onDismiss={() => setBanner(null)}>{banner.text}</Alert>}

      <div className="filter-row">
        <SearchBar onSearch={(t) => { setPage(0); setSearch(t); }}
                   placeholder="Search name or roll number..." />
        <select value={departmentId}
                onChange={(e) => { setPage(0); setDepartmentId(e.target.value); }}>
          <option value="">All departments</option>
          {depts?.content?.map((d) => (
            <option key={d.id} value={d.id}>{d.name}</option>
          ))}
        </select>
      </div>

      <DataTable
        loading={loading}
        error={error}
        rows={data?.content}
        emptyTitle="No students found"
        columns={[
          { key: 'rollNumber', label: 'Roll No' },
          { key: 'fullName', label: 'Name' },
          { key: 'departmentName', label: 'Department' },
          { key: 'currentSemester', label: 'Semester' },
          { key: 'admissionYear', label: 'Admitted' },
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
        <StudentForm initial={editing} departments={depts?.content || []}
          onClose={() => setEditing(null)}
          onSaved={(text) => { setEditing(null); setBanner({ type: 'success', text }); reload(); }} />
      )}

      {deleting && (
        <ConfirmDialog
          title="Delete student"
          message={`Delete ${deleting.fullName} (${deleting.rollNumber})? Their enrollments, attendance and marks are removed too.`}
          onCancel={() => setDeleting(null)}
          onConfirm={async () => {
            try {
              await studentService.remove(deleting.id);
              setBanner({ type: 'success', text: `Student ${deleting.rollNumber} deleted` });
              reload();
            } catch (err) {
              setBanner({ type: 'error', text: extractErrorMessage(err) });
            } finally { setDeleting(null); }
          }} />
      )}
    </section>
  );
}

function StudentForm({ initial, departments, onClose, onSaved }) {
  const isEdit = Boolean(initial.id);
  const [form, setForm] = useState({
    userId: initial.userId || '',
    departmentId: initial.departmentId || '',
    rollNumber: initial.rollNumber || '',
    firstName: initial.firstName || '',
    lastName: initial.lastName || '',
    admissionYear: initial.admissionYear || new Date().getFullYear(),
    currentSemester: initial.currentSemester || 1,
    phone: initial.phone || '',
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
    if (!isEdit && !String(form.userId).trim()) n.userId = 'User id is required';
    if (!form.departmentId) n.departmentId = 'Department is required';
    if (!form.rollNumber.trim()) n.rollNumber = 'Roll number is required';
    if (!form.firstName.trim()) n.firstName = 'First name is required';
    if (!form.lastName.trim()) n.lastName = 'Last name is required';
    setErrors(n);
    return Object.keys(n).length === 0;
  };

  const submit = async (e) => {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;
    setSaving(true);
    const payload = {
      ...form,
      userId: Number(form.userId),
      departmentId: Number(form.departmentId),
      admissionYear: Number(form.admissionYear),
      currentSemester: Number(form.currentSemester),
    };
    try {
      if (isEdit) { await studentService.update(initial.id, payload); onSaved('Student updated'); }
      else { await studentService.create(payload); onSaved('Student created'); }
    } catch (err) {
      setServerError(extractErrorMessage(err));
    } finally { setSaving(false); }
  };

  return (
    <Modal title={isEdit ? 'Edit student' : 'New student'} onClose={onClose}>
      <form onSubmit={submit} noValidate>
        <Alert type="error" onDismiss={() => setServerError('')}>{serverError}</Alert>

        {!isEdit && (
          <>
            <label htmlFor="userId">User account id</label>
            <input id="userId" name="userId" value={form.userId} onChange={change} disabled={saving} />
            {errors.userId && <span className="field-error">{errors.userId}</span>}
          </>
        )}

        <label htmlFor="departmentId">Department</label>
        <select id="departmentId" name="departmentId" value={form.departmentId} onChange={change} disabled={saving}>
          <option value="">Select...</option>
          {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
        {errors.departmentId && <span className="field-error">{errors.departmentId}</span>}

        <label htmlFor="rollNumber">Roll number</label>
        <input id="rollNumber" name="rollNumber" value={form.rollNumber} onChange={change} disabled={saving} />
        {errors.rollNumber && <span className="field-error">{errors.rollNumber}</span>}

        <div className="form-row">
          <div>
            <label htmlFor="firstName">First name</label>
            <input id="firstName" name="firstName" value={form.firstName} onChange={change} disabled={saving} />
            {errors.firstName && <span className="field-error">{errors.firstName}</span>}
          </div>
          <div>
            <label htmlFor="lastName">Last name</label>
            <input id="lastName" name="lastName" value={form.lastName} onChange={change} disabled={saving} />
            {errors.lastName && <span className="field-error">{errors.lastName}</span>}
          </div>
        </div>

        <div className="form-row">
          <div>
            <label htmlFor="admissionYear">Admission year</label>
            <input id="admissionYear" name="admissionYear" type="number" value={form.admissionYear} onChange={change} disabled={saving} />
          </div>
          <div>
            <label htmlFor="currentSemester">Semester</label>
            <input id="currentSemester" name="currentSemester" type="number" value={form.currentSemester} onChange={change} disabled={saving} />
          </div>
        </div>

        <label htmlFor="phone">Phone (optional)</label>
        <input id="phone" name="phone" value={form.phone} onChange={change} disabled={saving} />

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
