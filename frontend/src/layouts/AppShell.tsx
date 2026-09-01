import { Outlet, useNavigate } from "react-router";
import Sidebar from "../components/layout/Sidebar";
import TopHeader from "../components/layout/TopHeader";
import { useAuth } from "../hooks/useAuth";
import type { CurrentUser } from "./appShell.types";
import "./AppShell.css";

interface AppShellProps {
  currentUser?: CurrentUser;
}

export default function AppShell({
  currentUser: propUser,
}: AppShellProps) {
  const navigate = useNavigate();
  const { currentUser: authUser, logout } = useAuth();

  const user: CurrentUser = propUser || authUser || {
    name: "User",
    role: "EMPLOYEE",
  };

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <div className="app-shell">
      <Sidebar role={user.role} />

      <TopHeader
        user={user}
        onLogout={handleLogout}
      />

      <main className="app-shell__main">
        <div className="app-shell__content">
          <Outlet />
        </div>
      </main>
    </div>
  );
}