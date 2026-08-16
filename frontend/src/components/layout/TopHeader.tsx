import type { CurrentUser, UserRole } from "../../layouts/appShell.types";
import "./TopHeader.css";

interface TopHeaderProps {
  user: CurrentUser;
  onLogout: () => void;
}

const roleLabels: Record<UserRole, string> = {
  EMPLOYEE: "Employee",
  MANAGER: "Manager",
  ADMIN: "Admin",
};

export default function TopHeader({
  user,
  onLogout,
}: TopHeaderProps) {
  return (
    <header className="top-header">
      <div className="top-header__account">
        <div className="top-header__user">
          <span className="top-header__user-name">
            {user.name}
          </span>

          <span className="top-header__user-role">
            {roleLabels[user.role]}
          </span>
        </div>

        <button
          type="button"
          className="top-header__logout"
          onClick={onLogout}
          aria-label="Logout"
          title="Logout"
        >
          <span
            className="material-symbols-outlined"
            aria-hidden="true"
          >
            logout
          </span>
        </button>
      </div>
    </header>
  );
}