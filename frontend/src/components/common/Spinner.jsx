/** Loading state. Required on every screen that fetches data. */
export default function Spinner({ label = 'Loading...' }) {
  return (
    <div className="state-block" role="status" aria-live="polite">
      <div className="spinner" />
      <p>{label}</p>
    </div>
  );
}
