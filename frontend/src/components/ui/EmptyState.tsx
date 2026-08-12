import type { ReactNode } from "react";

import "./EmptyState.css";

interface EmptyStateProps {
  title: string;
  message?: string;
  icon?: string;
  action?: ReactNode;
}

export default function EmptyState({
  title,
  message,
  icon = "inbox",
  action,
}: EmptyStateProps) {
  return (
    <div className="empty-state">
      <span
        className="material-symbols-outlined empty-state__icon"
        aria-hidden="true"
      >
        {icon}
      </span>

      <h2 className="empty-state__title">
        {title}
      </h2>

      {message ? (
        <p className="empty-state__message">
          {message}
        </p>
      ) : null}

      {action ? (
        <div className="empty-state__action">
          {action}
        </div>
      ) : null}
    </div>
  );
}