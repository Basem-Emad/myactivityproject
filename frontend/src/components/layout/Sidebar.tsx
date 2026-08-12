import { Link, useLocation } from "react-router";
import type { UserRole } from "../../layouts/appShell.types";
import "./Sidebar.css";

interface SidebarProps {
  role: UserRole;
}

interface NavigationItem {
  label: string;
  path: string;
  icon: string;
  roles: UserRole[];
}

const navigationItems: NavigationItem[] = [
  {
    label: "Dashboard",
    path: "/dashboard",
    icon: "dashboard",
    roles: ["EMPLOYEE", "MANAGER", "ADMIN"],
  },
  {
    label: "My Activities",
    path: "/activities",
    icon: "list_alt",
    roles: ["EMPLOYEE", "ADMIN"],
  },
  {
    label: "Add Activity",
    path: "/activities/new",
    icon: "add_circle",
    roles: ["EMPLOYEE", "ADMIN"],
  },
  {
    label: "Monthly Report",
    path: "/reports/monthly",
    icon: "insert_chart",
    roles: ["EMPLOYEE", "MANAGER", "ADMIN"],
  },
  {
    label: "Users",
    path: "/users",
    icon: "group",
    roles: ["ADMIN"],
  },
  {
    label: "Activity Types",
    path: "/activity-types",
    icon: "category",
    roles: ["ADMIN"],
  },
  {
    label: "Activity Subjects",
    path: "/activity-subjects",
    icon: "subject",
    roles: ["ADMIN"],
  },
];

function isRouteActive(currentPath: string, itemPath: string) {
  if (itemPath === "/activities") {
    const isActivityList = currentPath === "/activities";
    const isEditActivity =
      /^\/activities\/[^/]+\/edit$/.test(currentPath);

    return isActivityList || isEditActivity;
  }

  return currentPath === itemPath;
}

export default function Sidebar({ role }: SidebarProps) {
  const location = useLocation();

  const visibleItems = navigationItems.filter((item) =>
    item.roles.includes(role),
  );

  return (
    <aside className="sidebar">
      <Link
        to="/dashboard"
        className="sidebar__brand"
        aria-label="Activity Tracking System dashboard"
      >
        <span className="sidebar__brand-icon" aria-hidden="true">
          <span className="material-symbols-outlined material-symbols-filled">
            timer
          </span>
        </span>

        <span className="sidebar__brand-name">
          Activity Tracking
          <br />
          System
        </span>
      </Link>

      <nav className="sidebar__nav" aria-label="Primary navigation">
        {visibleItems.map((item) => {
          const active = isRouteActive(location.pathname, item.path);

          return (
            <Link
              key={item.path}
              to={item.path}
              className={`sidebar__nav-item ${
                active ? "sidebar__nav-item--active" : ""
              }`}
              aria-current={active ? "page" : undefined}
            >
              <span
                className={`material-symbols-outlined sidebar__nav-icon ${
                  active ? "material-symbols-filled" : ""
                }`}
                aria-hidden="true"
              >
                {item.icon}
              </span>

              <span>{item.label}</span>
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}