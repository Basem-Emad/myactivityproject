import { Navigate, Route, Routes } from "react-router";

import AppShell from "./layouts/AppShell";
import LoginPage from "./pages/auth/LoginPage";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import { AuthProvider } from "./contexts/AuthContext";

import DashboardPage from "./pages/dashboard/DashboardPage";
import ActivitiesPage from "./pages/activities/ActivitiesPage";
import AddActivityPage from "./pages/activities/AddActivityPage";
import EditActivityPage from "./pages/activities/EditActivityPage";
import UsersPage from "./pages/users/UsersPage";
import ActivityTypesPage from "./pages/master-data/ActivityTypesPage";
import ActivitySubjectsPage from "./pages/master-data/ActivitySubjectsPage";
import MonthlyReportPage from "./pages/reports/MonthlyReportPage";

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route
          element={
            <ProtectedRoute>
              <AppShell />
            </ProtectedRoute>
          }
        >
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/activities" element={<ActivitiesPage />} />
          <Route path="/activities/new" element={<AddActivityPage />} />
          <Route path="/activities/:id/edit" element={<EditActivityPage />} />
          <Route path="/users" element={<UsersPage />} />
          <Route path="/activity-types" element={<ActivityTypesPage />} />
          <Route path="/activity-subjects" element={<ActivitySubjectsPage />} />
          <Route path="/reports/monthly" element={<MonthlyReportPage />} />
        </Route>

        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </AuthProvider>
  );
}

export default App;