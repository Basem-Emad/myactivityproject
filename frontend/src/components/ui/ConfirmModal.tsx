import { useEffect, useRef, type ReactNode } from "react";
import Button from "./Button";
import "./ConfirmModal.css";

export interface ConfirmModalProps {
  isOpen: boolean;
  title?: string;
  message: ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  confirmVariant?: "primary" | "secondary" | "danger";
  loading?: boolean;
  icon?: string;
  onConfirm: () => void | Promise<void>;
  onCancel: () => void;
}

export default function ConfirmModal({
  isOpen,
  title = "Confirm Action",
  message,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
  confirmVariant = "danger",
  loading = false,
  icon = "warning",
  onConfirm,
  onCancel,
}: ConfirmModalProps) {
  const cancelButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!isOpen) return;

    // Focus cancel button on open as safe default for destructive actions
    const timer = setTimeout(() => {
      cancelButtonRef.current?.focus();
    }, 50);

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && !loading) {
        onCancel();
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    return () => {
      clearTimeout(timer);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen, loading, onCancel]);

  if (!isOpen) return null;

  return (
    <div
      className="confirm-modal-overlay"
      onClick={() => {
        if (!loading) onCancel();
      }}
      role="presentation"
    >
      <div
        className="confirm-modal"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="confirm-modal-title"
        aria-describedby="confirm-modal-description"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="confirm-modal__header">
          {icon && (
            <div
              className={`confirm-modal__icon-wrapper confirm-modal__icon-wrapper--${confirmVariant}`}
            >
              <span className="material-symbols-outlined" aria-hidden="true">
                {icon}
              </span>
            </div>
          )}
          <h2 id="confirm-modal-title" className="confirm-modal__title">
            {title}
          </h2>
        </div>

        <div id="confirm-modal-description" className="confirm-modal__message">
          {message}
        </div>

        <div className="confirm-modal__actions">
          <Button
            ref={cancelButtonRef}
            variant="secondary"
            onClick={onCancel}
            disabled={loading}
          >
            {cancelLabel}
          </Button>
          <Button
            variant={confirmVariant}
            onClick={onConfirm}
            loading={loading}
          >
            {confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  );
}
