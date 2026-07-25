/**
 * A dependency-free horizontal bar chart. Owner: Member 4.
 *
 * The spec does not require a charting library, so we keep the dependency list to
 * Axios + React Router and render bars with plain CSS widths. `data` is
 * [{ label, value }]; bars scale to the largest value.
 */
export default function SimpleBarChart({ data, emptyText = 'No data' }) {
  if (!data || data.length === 0) {
    return <p className="muted">{emptyText}</p>;
  }

  const max = Math.max(...data.map((d) => d.value), 1);

  return (
    <div className="bar-chart">
      {data.map((d) => (
        <div className="bar-row" key={d.label}>
          <span className="bar-label">{d.label}</span>
          <div className="bar-track">
            <div className="bar-fill" style={{ width: `${(d.value / max) * 100}%` }}>
              <span className="bar-value">{d.value}</span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
