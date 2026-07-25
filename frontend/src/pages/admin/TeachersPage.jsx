import { useState } from 'react';
import teacherService from '../../services/teacherService';
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

/** Teacher CRUD. Owner: Member 2. */
export default function TeachersPage() {
  const { isAdmin } = useAuth();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [banner, setBanner] = useState(null);

  const { data: depts } = useFetch(() => departmentService.list({ size: 100 }), []);
  const { data, loading, error, reload } = useFetch(
    () => teacherService.list({ page, size: 10, search, ...(departmentId ? { departmentId } : {}) }),
    [page, search, departmentId],
  );

  return (
    <section>
      <div className="page-header">
        <h1>Teachers</h1>
        {isAdmin && (
          <button type="button" className="btn btn-primary inline" onClick={() => setEditing({})}>
            + New Teacher
          </button>
        )}
      </div>

      {banner && <Alert type={banner.type} onDismiss={() => setBanner(null)}>{banner.text}</Alert>}

      <div className="filter-row">
        <SearchBar onSearch={(t) => { setPage(0); setSearch(t); }} placeholder="Search name or employee code..." />
        <select value={departmentId} onChange={(e) => { setPage(0); setDepartmentId(e.target.value); }}>
          <option value="">All departments</option>
          {depts?.content?.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
      </div>

      <DataTable
        loading={loading}
        error={error}
        rows={data?.content}
        emptyTitle="No teachers found"
        columns={[
          { key: 'employeeCode', label: 'Employee Code' },
          { key: 'fullName', label: 'Name' },
          { key: 'departmentName', label: 'Department' },
          { key: 'designation', label: 'Designation', render: (r) => r.designation || '—' },
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
        <TeacherForm initial={editing} departments={depts?.content || []}
          onClose={() => setEditing(null)}
          onSaved={(text) => { setEditing(null); setBanner({ type: 'success', text }); reload(); }} />
      )}

      {deleting && (
        <ConfirmDialog
          title="Delete teacher"
          message={`Delete ${deleting.fullName}? Their courses become unassigned; attendance and marks they recorded are kept.`}
          onCancel={() => setDeleting(null)}
          onConfirm={async () => {
            try {
              await teacherService.remove(deleting.id);
              setBanner({ type: 'success', text: `Teacher ${deleting.employeeCode} deleted` });
              reload();
            } catch (err) {
              setBanner({ type: 'error', text: extractErrorMessage(err) });
            } finally { setDeleting(null); }
          }} />
      )}
    </section>
  );
}

function TeacherForm({ initial, departments, onClose, onSaved }) {
  const isEdit = Boolean(initial.id);
  const [form, setForm] = useState({
    userId: initial.userId || '',
    departmentId: initial.departmentId || '',
    employeeCode: initial.employeeCode || '',
    firstName: initial.firstName || '',
    lastName: initial.lastName || '',
    designation: initial.designation || '',
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
    if (!form.employeeCode.trim()) n.employeeCode = 'Employee code is required';
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
    const payload = { ...form, userId: Number(form.userId), departmentId: Number(form.departmentId) };
    try {
      if (isEdit) { await teacherService.update(initial.id, payload); onSaved('Teacher updated'); }
      else { await teacherService.create(payload); onSaved('Teacher created'); }
    } catch (err) {
      setServerError(extractErrorMessage(err));
    } finally { setSaving(false); }
  };

  return (
    <Modal title={isEdit ? 'Edit teacher' : 'New teacher'} onClose={onClose}>
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

        <label htmlFor="employeeCode">Employee code</label>
        <input id="employeeCode" name="employeeCode" value={form.employeeCode} onChange={change} disabled={saving} />
        {errors.employeeCode && <span className="field-error">{errors.employeeCode}</span>}

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

        <label htmlFor="designation">Designation (optional)</label>
        <input id="designation" name="designation" value={form.designation} onChange={change} disabled={saving} />

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
