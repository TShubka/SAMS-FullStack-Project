import { useState } from 'react';
import departmentService from '../../services/departmentService';
import useFetch from '../../hooks/useFetch';
import { extractErrorMessage } from '../../services/api';
import DataTable from '../../components/common/DataTable';
import Pagination from '../../components/common/Pagination';
import SearchBar from '../../components/common/SearchBar';
import Modal from '../../components/common/Modal';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import Alert from '../../components/common/Alert';

/** Department CRUD screen. Owner: Member 2. */
export default function DepartmentsPage() {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [editing, setEditing] = useState(null); // null | {} (new) | row (edit)
  const [deleting, setDeleting] = useState(null);
  const [banner, setBanner] = useState(null); // { type, text }

  const { data, loading, error, reload } = useFetch(
    () => departmentService.list({ page, size: 10, search }),
    [page, search],
  );

  const onSearch = (term) => {
    setPage(0);
    setSearch(term);
  };

  return (
    <section>
      <div className="page-header">
        <h1>Departments</h1>
        <button type="button" className="btn btn-primary inline"
                onClick={() => setEditing({})}>
          + New Department
        </button>
      </div>

      {banner && (
        <Alert type={banner.type} onDismiss={() => setBanner(null)}>{banner.text}</Alert>
      )}

      <SearchBar onSearch={onSearch} placeholder="Search by name or code..." />

      <DataTable
        loading={loading}
        error={error}
        rows={data?.content}
        emptyTitle="No departments"
        emptyMessage="Create the first department to get started."
        columns={[
          { key: 'name', label: 'Name' },
          { key: 'code', label: 'Code' },
        ]}
        actions={(row) => (
          <>
            <button type="button" className="btn-link" onClick={() => setEditing(row)}>Edit</button>
            <button type="button" className="btn-link danger" onClick={() => setDeleting(row)}>Delete</button>
          </>
        )}
      />

      <Pagination page={data?.page ?? 0} totalPages={data?.totalPages ?? 0} onChange={setPage} />

      {editing && (
        <DepartmentForm
          initial={editing}
          onClose={() => setEditing(null)}
          onSaved={(text) => {
            setEditing(null);
            setBanner({ type: 'success', text });
            reload();
          }}
        />
      )}

      {deleting && (
        <ConfirmDialog
          title="Delete department"
          message={`Delete "${deleting.name}" (${deleting.code})? This cannot be undone.`}
          onCancel={() => setDeleting(null)}
          onConfirm={async () => {
            try {
              await departmentService.remove(deleting.id);
              setBanner({ type: 'success', text: `Department ${deleting.code} deleted` });
              reload();
            } catch (err) {
              setBanner({ type: 'error', text: extractErrorMessage(err) });
            } finally {
              setDeleting(null);
            }
          }}
        />
      )}
    </section>
  );
}

function DepartmentForm({ initial, onClose, onSaved }) {
  const isEdit = Boolean(initial.id);
  const [form, setForm] = useState({ name: initial.name || '', code: initial.code || '' });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [saving, setSaving] = useState(false);

  const change = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: undefined });
  };

  const validate = () => {
    const next = {};
    if (!form.name.trim()) next.name = 'Name is required';
    if (!form.code.trim()) next.code = 'Code is required';
    else if (!/^[A-Za-z0-9]+$/.test(form.code)) next.code = 'Code must be letters and digits only';
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const submit = async (e) => {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;
    setSaving(true);
    try {
      if (isEdit) {
        await departmentService.update(initial.id, form);
        onSaved(`Department ${form.code} updated`);
      } else {
        await departmentService.create(form);
        onSaved(`Department ${form.code} created`);
      }
    } catch (err) {
      setServerError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal title={isEdit ? 'Edit department' : 'New department'} onClose={onClose}>
      <form onSubmit={submit} noValidate>
        <Alert type="error" onDismiss={() => setServerError('')}>{serverError}</Alert>

        <label htmlFor="name">Name</label>
        <input id="name" name="name" value={form.name} onChange={change} disabled={saving} />
        {errors.name && <span className="field-error">{errors.name}</span>}

        <label htmlFor="code">Code</label>
        <input id="code" name="code" value={form.code} onChange={change} disabled={saving} />
        {errors.code && <span className="field-error">{errors.code}</span>}

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
