/**
 * Empty state. Distinct from the loading state on purpose - "we found nothing"
 * and "we are still looking" must never look the same to the user.
 */
export default function EmptyState({ title = 'Nothing here yet', message, action }) {
  return (
    <div className="state-block">
      <h3>{title}</h3>
      {message && <p className="muted">{message}</p>}
      {action}
    </div>
  );
}
