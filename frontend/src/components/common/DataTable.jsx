import Spinner from './Spinner';
import EmptyState from './EmptyState';
import Alert from './Alert';

/**
 * Table that renders the loading, error, empty and data states from one place, so
 * every list screen handles all four consistently.
 *
 * `columns` is [{ key, label, render? }]; `rows` is the data; `rowKey` picks a
 * stable key per row.
 */
export default function DataTable({
  columns,
  rows,
  rowKey = (r) => r.id,
  loading,
  error,
  emptyTitle = 'No records',
  emptyMessage,
  actions,
}) {
  if (loading) return <Spinner />;
  if (error) return <Alert type="error">{error}</Alert>;
  if (!rows || rows.length === 0) {
    return <EmptyState title={emptyTitle} message={emptyMessage} />;
  }

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {columns.map((c) => (
              <th key={c.key}>{c.label}</th>
            ))}
            {actions && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={rowKey(row)}>
              {columns.map((c) => (
                <td key={c.key}>{c.render ? c.render(row) : row[c.key]}</td>
              ))}
              {actions && <td className="row-actions">{actions(row)}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
