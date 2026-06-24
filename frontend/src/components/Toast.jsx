import { useCallback, useEffect, useState } from 'react';

function Toast({ toast, onRemove }) {
  useEffect(() => {
    const t = setTimeout(() => onRemove(toast.id), toast.duration ?? 3500);
    return () => clearTimeout(t);
  }, [toast, onRemove]);

  const icons = { success: 'OK', error: '!', warning: '!', info: 'i' };

  return (
    <div className={`toast toast-${toast.type}`}>
      <span>{icons[toast.type] ?? 'i'}</span>
      <span>{toast.message}</span>
    </div>
  );
}

export function ToastContainer({ toasts, onRemove }) {
  if (!toasts.length) return null;
  return (
    <div className="toast-container">
      {toasts.map(t => <Toast key={t.id} toast={t} onRemove={onRemove} />)}
    </div>
  );
}

export function useToast() {
  const [toasts, setToasts] = useState([]);

  const add = useCallback((message, type = 'success', duration = 3500) => {
    const id = Date.now() + Math.random();
    setToasts(p => [...p, { id, message, type, duration }]);
  }, []);

  const remove = useCallback((id) => {
    setToasts(p => p.filter(t => t.id !== id));
  }, []);

  return { toasts, addToast: add, removeToast: remove };
}
