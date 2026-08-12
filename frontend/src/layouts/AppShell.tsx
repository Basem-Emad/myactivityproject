import { Outlet, useNavigate } from "react-router";

import Sidebar from "../components/layout/Sidebar";
import TopHeader from "../components/layout/TopHeader";

import type { CurrentUser } from "./appShell.types";

import "./AppShell.css";

interface AppShellProps {
  currentUser: CurrentUser;
}

export default function AppShell({
  currentUser,
}: AppShellProps) {
  const navigate = useNavigate();

  function handleLogout() {
    /*
     * Temporary frontend-foundation behaviour.
     *
     * The authentication module will later:
     * - clear the JWT/session
     * - clear authenticated user state
     * - redirect to /login
     */
    navigate("/login");
  }

  return (
    <div className="app-shell">
      <Sidebar role={currentUser.role} />

      <TopHeader
        user={currentUser}
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