import Button from "./Button";
import "./ErrorAlert.css";

interface ErrorAlertProps {
  title?: string;
  message: string;
  onRetry?: () => void;
  retryLabel?: string;
}

export default function ErrorAlert({
  title = "Something went wrong",
  message,
  onRetry,
  retryLabel = "Retry",
}: ErrorAlertProps) {
  return (
    <div className="error-alert" role="alert">
      <span
        className="material-symbols-outlined error-alert__icon"
        aria-hidden="true"
      >
        error
      </span>

      <div className="error-alert__content">
        <strong className="error-alert__title">
          {title}
        </strong>

        <p className="error-alert__message">
          {message}
        </p>
      </div>

      {onRetry ? (
        <Button
          variant="secondary"
          onClick={onRetry}
          className="error-alert__retry"
        >
          {retryLabel}
        </Button>
      ) : null}
    </div>
  );
}