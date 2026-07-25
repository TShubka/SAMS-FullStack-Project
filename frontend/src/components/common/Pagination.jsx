/**
 * Pagination driven by a PageResponse. Hidden entirely when there is only one
 * page - a control that can do nothing is just noise.
 */
export default function Pagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null;

  return (
    <div className="pagination">
      <button
        type="button"
        className="btn btn-secondary"
        disabled={page <= 0}
        onClick={() => onChange(page - 1)}
      >
        Previous
      </button>
      <span className="page-info">
        Page {page + 1} of {totalPages}
      </span>
      <button
        type="button"
        className="btn btn-secondary"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        Next
      </button>
    </div>
  );
}
