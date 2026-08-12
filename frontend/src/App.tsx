import { Navigate, Route, Routes } from "react-router";

import AppShell from "./layouts/AppShell";
import type { CurrentUser } from "./layouts/appShell.types";

import LoginPage from "./pages/auth/LoginPage";

import DashboardPage from "./pages/dashboard/DashboardPage";

import ActivitiesPage from "./pages/activities/ActivitiesPage";
import AddActivityPage from "./pages/activities/AddActivityPage";
import EditActivityPage from "./pages/activities/EditActivityPage";

import UsersPage from "./pages/users/UsersPage";

import ActivityTypesPage from "./pages/master-data/ActivityTypesPage";
import ActivitySubjectsPage from "./pages/master-data/ActivitySubjectsPage";

import MonthlyReportPage from "./pages/reports/MonthlyReportPage";

/*
 * Temporary preview user.
 *
 * This will be replaced by the authenticated user supplied
 * by the authentication module.
 */
const previewUser: CurrentUser = {
  name: "Jane Admin",
  role: "ADMIN",
};

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        element={<AppShell currentUser={previewUser} />}
      >
        <Route
          path="/dashboard"
          element={<DashboardPage />}
        />

        <Route
          path="/activities"
          element={<ActivitiesPage />}
        />

        <Route
          path="/activities/new"
          element={<AddActivityPage />}
        />

        <Route
          path="/activities/:id/edit"
          element={<EditActivityPage />}
        />

        <Route
          path="/users"
          element={<UsersPage />}
        />

        <Route
          path="/activity-types"
          element={<ActivityTypesPage />}
        />

        <Route
          path="/activity-subjects"
          element={<ActivitySubjectsPage />}
        />

        <Route
          path="/reports/monthly"
          element={<MonthlyReportPage />}
        />
      </Route>

      <Route
        path="/"
        element={<Navigate to="/login" replace />}
      />

      <Route
        path="*"
        element={<Navigate to="/login" replace />}
      />
    </Routes>
  );
}

export default App;