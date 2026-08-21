import { useEffect } from "react";
import "./SuccessAlert.css";

interface SuccessAlertProps {
  title?: string;
  message: string;
  onClose?: () => void;
  autoDismissMs?: number;
}

export default function SuccessAlert({
  title,
  message,
  onClose,
  autoDismissMs = 4000,
}: SuccessAlertProps) {
  useEffect(() => {
    if (!autoDismissMs || !onClose) return;

    const timer = setTimeout(() => {
      onClose();
    }, autoDismissMs);

    return () => clearTimeout(timer);
  }, [autoDismissMs, onClose]);

  return (
    <div className="success-alert" role="status" aria-live="polite">
      <span
        className="material-symbols-outlined success-alert__icon"
        aria-hidden="true"
      >
        check_circle
      </span>

      <div className="success-alert__content">
        {title && <strong className="success-alert__title">{title}</strong>}
        <p className="success-alert__message">{message}</p>
      </div>

      {onClose && (
        <button
          type="button"
          className="success-alert__close"
          onClick={onClose}
          aria-label="Dismiss notification"
        >
          <span
            className="material-symbols-outlined success-alert__close-icon"
            aria-hidden="true"
          >
            close
          </span>
        </button>
      )}
    </div>
  );
}
