import type {
  ButtonHTMLAttributes,
  ReactNode,
  Ref,
} from "react";

import "./Button.css";

type ButtonVariant = "primary" | "secondary" | "danger";

interface ButtonProps
  extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  loading?: boolean;
  icon?: string;
  children: ReactNode;
  ref?: Ref<HTMLButtonElement>;
}

export default function Button({
  variant = "primary",
  loading = false,
  icon,
  children,
  className = "",
  disabled,
  ref,
  ...props
}: ButtonProps) {
  const isDisabled = disabled || loading;

  return (
    <button
      {...props}
      ref={ref}
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