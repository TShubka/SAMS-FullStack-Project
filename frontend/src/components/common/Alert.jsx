/** Error and success messaging. `type` is one of error | success | info | warning. */
export default function Alert({ type = 'info', children, onDismiss }) {
  if (!children) return null;

  return (
    <div className={`alert alert-${type}`} role={type === 'error' ? 'alert' : 'status'}>
      <span>{children}</span>
      {onDismiss && (
        <button type="button" className="alert-close" onClick={onDismiss} aria-label="Dismiss">
          &times;
        </button>
      )}
    </div>
  );
}
