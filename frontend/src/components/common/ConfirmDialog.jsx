import Modal from './Modal';

/** Confirmation gate for destructive actions like delete. */
export default function ConfirmDialog({ title = 'Are you sure?', message, onConfirm, onCancel, busy }) {
  return (
    <Modal title={title} onClose={onCancel}>
      <p>{message}</p>
      <div className="modal-actions">
        <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={busy}>
          Cancel
        </button>
        <button type="button" className="btn btn-danger" onClick={onConfirm} disabled={busy}>
          {busy ? 'Deleting...' : 'Delete'}
        </button>
      </div>
    </Modal>
  );
}
