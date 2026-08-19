import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { ToastContext } from "./ToastContext";

const DEFAULT_TOAST_DURATION_MS = 4200;
const MAX_VISIBLE_TOASTS = 4;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const timersRef = useRef(new Map());

  const dismissToast = useCallback((toastId) => {
    const timerId = timersRef.current.get(toastId);

    if (timerId) {
      window.clearTimeout(timerId);
      timersRef.current.delete(toastId);
    }

    setToasts((currentToasts) =>
      currentToasts.filter((toast) => toast.id !== toastId),
    );
  }, []);

  const showToast = useCallback(
    ({
      type = "info",
      title,
      message,
      durationMs = DEFAULT_TOAST_DURATION_MS,
    }) => {
      const toastId = `${Date.now()}-${Math.random().toString(36).slice(2)}`;

      setToasts((currentToasts) =>
        [
          {
            id: toastId,
            type,
            title,
            message,
          },
          ...currentToasts,
        ].slice(0, MAX_VISIBLE_TOASTS),
      );

      if (durationMs > 0) {
        const timerId = window.setTimeout(
          () => dismissToast(toastId),
          durationMs,
        );
        timersRef.current.set(toastId, timerId);
      }

      return toastId;
    },
    [dismissToast],
  );

  useEffect(() => {
    const timers = timersRef.current;

    return () => {
      timers.forEach((timerId) => window.clearTimeout(timerId));
      timers.clear();
    };
  }, []);

  const contextValue = useMemo(
    () => ({
      showToast,
      dismissToast,
    }),
    [dismissToast, showToast],
  );

  return (
    <ToastContext.Provider value={contextValue}>
      {children}

      <div className="toast-viewport" aria-live="polite" aria-atomic="true">
        {toasts.map((toast) => (
          <section
            key={toast.id}
            className={`toast toast-${toast.type}`}
            role="status"
          >
            <div className="toast-marker" aria-hidden="true" />
            <div className="toast-content">
              {toast.title && <strong>{toast.title}</strong>}
              {toast.message && <p>{toast.message}</p>}
            </div>
            <button
              type="button"
              className="toast-close"
              aria-label="Dismiss notification"
              onClick={() => dismissToast(toast.id)}
            >
              x
            </button>
          </section>
        ))}
      </div>
    </ToastContext.Provider>
  );
}
