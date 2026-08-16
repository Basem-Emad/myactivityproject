import { useState } from "react";

import Button from "../../components/ui/Button";

import { mockMonthlySummary } from "../../services/mockDashboardData";

import "./DashboardPage.css";

const MONTH_OPTIONS = [
  "2026-01", "2026-02", "2026-03", "2026-04", "2026-05", "2026-06",
];

export default function DashboardPage() {
  const [selectedMonth, setSelectedMonth] = useState(MONTH_OPTIONS[5]);

  // using mock data for now, real API will replace this
  const summary = mockMonthlySummary;

  return (
      <div className="dashboard-page">
        <div className="dashboard-page__header">
          <h1>Dashboard</h1>

          <div className="dashboard-page__header-actions">
            <select
                className="dashboard-page__month-select"
                value={selectedMonth}
                onChange={(e) => setSelectedMonth(e.target.value)}
            >
              {MONTH_OPTIONS.map((month) => (
                  <option key={month} value={month}>
                    {month}
                  </option>
              ))}
            </select>

            <Button icon="download" variant="secondary">
              Export to Excel
            </Button>
          </div>
        </div>

        <div className="dashboard-page__summary-cards">
          <div className="dashboard-page__card">
            <span className="dashboard-page__card-label">Total Days</span>
            <span className="dashboard-page__card-value">{summary.totalDays}</span>
          </div>

          <div className="dashboard-page__card">
            <span className="dashboard-page__card-label">Total Hours</span>
            <span className="dashboard-page__card-value">{summary.totalHoursMinutes}</span>
          </div>
        </div>

        <div className="dashboard-page__charts">
          <div className="dashboard-page__chart-card">
            <h2>Hours by Activity Type</h2>
            <div className="dashboard-page__chart-placeholder">
              <span className="material-symbols-outlined">donut_large</span>
              <p>Chart will go here</p>
            </div>
            <ul className="dashboard-page__legend">
              {summary.byActivityType.map((item) => (
                  <li key={item.activityType}>
                    <span>{item.activityType}</span>
                    <span>{item.hoursMinutes}</span>
                  </li>
              ))}
            </ul>
          </div>

          <div className="dashboard-page__chart-card">
            <h2>Hours by Activity Subject</h2>
            <div className="dashboard-page__chart-placeholder">
              <span className="material-symbols-outlined">bar_chart</span>
              <p>Chart will go here</p>
            </div>
            <ul className="dashboard-page__legend">
              {summary.byActivitySubject.map((item) => (
                  <li key={item.activitySubject}>
                    <span>{item.activitySubject}</span>
                    <span>{item.hoursMinutes}</span>
                  </li>
              ))}
            </ul>
          </div>

          <div className="dashboard-page__chart-card dashboard-page__chart-card--wide">
            <h2>Daily Trend (This Month)</h2>
            <div className="dashboard-page__chart-placeholder">
              <span className="material-symbols-outlined">show_chart</span>
              <p>Chart will go here</p>
            </div>
          </div>
        </div>
      </div>
  );
}
