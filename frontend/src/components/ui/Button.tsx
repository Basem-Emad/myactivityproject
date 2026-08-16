import type {
  ButtonHTMLAttributes,
  ReactNode,
} from "react";

import "./Button.css";

type ButtonVariant = "primary" | "secondary" | "danger";

interface ButtonProps
  extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  loading?: boolean;
  icon?: string;
  children: ReactNode;
}

export default function Button({
  variant = "primary",
  loading = false,
  icon,
  children,
  className = "",
  disabled,
  ...props
}: ButtonProps) {
  const isDisabled = disabled || loading;

  return (
    <button
      {...props}
      type={props.type ?? "button"}
      className={`button button--${variant} ${className}`.trim()}
      disabled={isDisabled}
      aria-busy={loading || undefined}
    >
      {loading ? (
        <span
          className="button__spinner"
          aria-hidden="true"
        />
      ) : icon ? (
        <span
          className="material-symbols-outlined button__icon"
          aria-hidden="true"
        >
          {icon}
        </span>
      ) : null}

      <span>{children}</span>
    </button>
  );
}