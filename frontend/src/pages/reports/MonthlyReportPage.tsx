import { useState } from "react";
import { mockMonthlyReportData } from "../../services/mockMonthlyReportData";
import type { MonthlyReportData } from "../../services/mockMonthlyReportData";
import "./MonthlyReportPage.css";

type PageState = "loaded" | "loading" | "empty" | "error";

// Helper: Format duration from minutes to "Xh Ym"
function formatDuration(totalMinutes: number): string {
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  return `${hours}h ${minutes}m`;
}

// Helper: Format date from YYYY-MM-DD to "Mon DD"
function formatDate(dateStr: string): string {
  const date = new Date(dateStr + "T00:00:00");
  return date.toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

function MonthlyReportPage() {
  const [selectedMonth, setSelectedMonth] = useState("2026-08");
  const [state] = useState<PageState>("loaded"); // Toggle for testing: loaded, loading, empty, error

  const handleMonthChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSelectedMonth(e.target.value);
  };

  const handleExportExcel = () => {
    // Will be: window.location.href = `/api/reports/export/excel?month=${selectedMonth}`;
    console.log("Export Excel for", selectedMonth);
  };

  const handleRetry = () => {
    console.log("Retry loading report");
  };

  return (
    <div className="monthly-report-page">
      {/* Page Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Monthly Report</h1>
          <p className="page-subtitle">
            View and export your monthly activity summary.
          </p>
        </div>

        {/* Month Picker & Export */}
        <div className="page-actions">
          <label htmlFor="report-month" className="month-label">
            Month
          </label>
          <input
            type="month"
            id="report-month"
            value={selectedMonth}
            onChange={handleMonthChange}
            disabled={state === "loading" || state === "error"}
            className="month-input"
          />
          <button
            type="button"
            onClick={handleExportExcel}
            disabled={state !== "loaded"}
            className="export-button"
            aria-describedby="export-desc"
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              download
            </span>
            Export Excel
          </button>
          <span id="export-desc" className="sr-only">
            {state === "loaded"
              ? `Export the ${selectedMonth} activity report as an Excel workbook.`
              : state === "empty"
              ? `No data to export for ${selectedMonth}.`
              : "Export is unavailable."}
          </span>
        </div>
      </div>

      {/* Loading State */}
      {state === "loading" && <LoadingState />}

      {/* Error State */}
      {state === "error" && <ErrorState onRetry={handleRetry} />}

      {/* Empty State */}
      {state === "empty" && <EmptyState />}

      {/* Loaded State */}
      {state === "loaded" && <LoadedState data={mockMonthlyReportData} />}
    </div>
  );
}

/** Loading State */
function LoadingState() {
  return (
    <div className="report-content" aria-busy="true">
      <div aria-live="polite" className="sr-only">
        Loading the monthly report.
      </div>

      {/* Summary Cards Skeleton */}
      <div className="summary-grid">
        <SummaryCardSkeleton icon="schedule" />
        <SummaryCardSkeleton icon="event_available" />
        <SummaryCardSkeleton icon="insights" />
      </div>

      {/* Tables Skeleton */}
      <div className="tables-grid">
        <TableSkeleton title="Hours by Activity Type" rows={6} />
        <TableSkeleton title="Hours by Activity Subject" rows={4} />
      </div>

      {/* Details Table Skeleton */}
      <div className="details-card">
        <h3 className="details-title">Monthly Activity Details</h3>
        <div className="table-scroll">
          <table className="details-table">
            <thead>
              <tr>
                <th scope="col">DATE</th>
                <th scope="col">FROM</th>
                <th scope="col">TO</th>
                <th scope="col">ACTIVITY TYPE</th>
                <th scope="col">ACTIVITY SUBJECT</th>
                <th scope="col">TASK DESCRIPTION</th>
                <th scope="col">DURATION</th>
              </tr>
            </thead>
            <tbody>
              {Array.from({ length: 6 }).map((_, i) => (
                <tr key={i} aria-hidden="true">
                  <td><div className="skeleton-box w-12" /></td>
                  <td><div className="skeleton-box w-10" /></td>
                  <td><div className="skeleton-box w-10" /></td>
                  <td><div className="skeleton-box w-20" /></td>
                  <td><div className="skeleton-box w-24" /></td>
                  <td><div className="skeleton-box w-40" /></td>
                  <td><div className="skeleton-box w-12 ml-auto" /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

/** Error State */
function ErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="report-content">
      {/* Error Banner */}
      <div className="error-banner" role="alert">
        <span className="material-symbols-outlined" aria-hidden="true">
          error
        </span>
        <div className="error-content">
          <h4 className="error-title">Unable to load monthly report</h4>
          <p className="error-message">
            We couldn't load the monthly report data. Please try again.
          </p>
        </div>
        <button type="button" onClick={onRetry} className="retry-button">
          Retry
        </button>
      </div>

      {/* Summary Cards - Empty */}
      <div className="summary-grid">
        <SummaryCard icon="schedule" label="Total Hours" value="—" />
        <SummaryCard icon="event_available" label="Total Days" value="—" />
        <SummaryCard icon="insights" label="Average Hours / Day" value="—" />
      </div>

      {/* Tables - Empty */}
      <div className="tables-grid">
        <EmptyTable title="Hours by Activity Type" />
        <EmptyTable title="Hours by Activity Subject" />
      </div>

      {/* Details - Empty */}
      <div className="details-card">
        <h3 className="details-title">Monthly Activity Details</h3>
        <div className="table-scroll">
          <table className="details-table">
            <thead>
              <tr>
                <th scope="col">DATE</th>
                <th scope="col">FROM</th>
                <th scope="col">TO</th>
                <th scope="col">ACTIVITY TYPE</th>
                <th scope="col">ACTIVITY SUBJECT</th>
                <th scope="col">TASK DESCRIPTION</th>
                <th scope="col">DURATION</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td colSpan={7} className="empty-table-cell">
                  <div className="empty-table-content">
                    <span className="material-symbols-outlined" aria-hidden="true">
                      info
                    </span>
                    Data unavailable
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

/** Empty State */
function EmptyState() {
  return (
    <div className="report-content">
      {/* Summary Cards - Zero */}
      <div className="summary-grid">
        <SummaryCard icon="schedule" label="Total Hours" value="0h 0m" />
        <SummaryCard icon="event_available" label="Total Days" value="0" />
        <SummaryCard icon="insights" label="Average Hours / Day" value="0h 0m" />
      </div>

      {/* Empty State Message */}
      <section
        className="empty-state-section"
        aria-labelledby="empty-state-heading"
        aria-describedby="empty-state-desc"
      >
        <div className="empty-icon-wrapper">
          <span className="material-symbols-outlined" aria-hidden="true">
            description
          </span>
        </div>
        <h2 id="empty-state-heading" className="empty-heading">
          No report data for August 2026
        </h2>
        <p id="empty-state-desc" className="empty-description">
          Log an activity to generate your monthly report.
        </p>
        <button type="button" className="add-activity-button">
          <span className="material-symbols-outlined" aria-hidden="true">
            add
          </span>
          Add Activity
        </button>
        <span className="sr-only" role="status">
          No report data is available for August 2026.
        </span>
      </section>
    </div>
  );
}

/** Loaded State */
function LoadedState({ data }: { data: MonthlyReportData }) {
  return (
    <div className="report-content">
      {/* Summary Cards */}
      <div className="summary-grid">
        <SummaryCard
          icon="schedule"
          label="Total Hours"
          value={data.totalHoursLabel}
        />
        <SummaryCard
          icon="event_available"
          label="Total Days"
          value={String(data.totalDays)}
        />
        <SummaryCard
          icon="insights"
          label="Average Hours / Day"
          value={data.avgHoursLabel}
        />
      </div>

      {/* Hours Tables */}
      <div className="tables-grid">
        <HoursByTypeTable data={data.hoursByType} total={data.totalHoursLabel} />
        <HoursBySubjectTable
          data={data.hoursBySubject}
          total={data.totalHoursLabel}
        />
      </div>

      {/* Activity Details Table */}
      <ActivityDetailsTable activities={data.activities} />
    </div>
  );
}

/** Summary Card */
function SummaryCard({
  icon,
  label,
  value,
}: {
  icon: string;
  label: string;
  value: string;
}) {
  return (
    <div className="summary-card">
      <div className="summary-header">
        <span className="material-symbols-outlined" aria-hidden="true">
          {icon}
        </span>
        <h3 className="summary-label">{label}</h3>
      </div>
      <div className="summary-value">{value}</div>
    </div>
  );
}

/** Summary Card Skeleton */
function SummaryCardSkeleton({ icon }: { icon: string }) {
  return (
    <div className="summary-card">
      <div className="summary-header">
        <span className="material-symbols-outlined" aria-hidden="true">
          {icon}
        </span>
        <div className="skeleton-box w-24 h-4" aria-hidden="true" />
      </div>
      <div className="skeleton-box w-32 h-8" aria-hidden="true" />
    </div>
  );
}

/** Hours by Type Table */
function HoursByTypeTable({
  data,
  total,
}: {
  data: { activityType: string; label: string }[];
  total: string;
}) {
  return (
    <div className="hours-card">
      <h3 className="hours-title">Hours by Activity Type</h3>
      <table className="hours-table">
        <caption className="sr-only">
          Hours grouped by Activity Type for August 2026.
        </caption>
        <thead>
          <tr>
            <th scope="col">ACTIVITY TYPE</th>
            <th scope="col">TOTAL HOURS</th>
          </tr>
        </thead>
        <tbody>
          {data.map((item, i) => (
            <tr key={i}>
              <th scope="row">{item.activityType}</th>
              <td>{item.label}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <th scope="row">Total</th>
            <td>{total}</td>
          </tr>
        </tfoot>
      </table>
    </div>
  );
}

/** Hours by Subject Table */
function HoursBySubjectTable({
  data,
  total,
}: {
  data: { activitySubject: string; label: string }[];
  total: string;
}) {
  return (
    <div className="hours-card">
      <h3 className="hours-title">Hours by Activity Subject</h3>
      <table className="hours-table">
        <caption className="sr-only">
          Hours grouped by Activity Subject for August 2026.
        </caption>
        <thead>
          <tr>
            <th scope="col">ACTIVITY SUBJECT</th>
            <th scope="col">TOTAL HOURS</th>
          </tr>
        </thead>
        <tbody>
          {data.map((item, i) => (
            <tr key={i}>
              <th scope="row">{item.activitySubject}</th>
              <td>{item.label}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <th scope="row">Total</th>
            <td>{total}</td>
          </tr>
        </tfoot>
      </table>
    </div>
  );
}

/** Activity Details Table */
function ActivityDetailsTable({
  activities,
}: {
  activities: MonthlyReportData["activities"];
}) {
  return (
    <div className="details-card">
      <h3 id="monthly-activity-details-heading" className="details-title">
        Monthly Activity Details
      </h3>
      <div className="table-scroll">
        <table
          className="details-table"
          aria-labelledby="monthly-activity-details-heading"
        >
          <caption className="sr-only">
            Monthly activity details for August 2026.
          </caption>
          <thead>
            <tr>
              <th scope="col">DATE</th>
              <th scope="col">FROM</th>
              <th scope="col">TO</th>
              <th scope="col">ACTIVITY TYPE</th>
              <th scope="col">ACTIVITY SUBJECT</th>
              <th scope="col">TASK DESCRIPTION</th>
              <th scope="col">DURATION</th>
            </tr>
          </thead>
          <tbody>
            {activities.map((activity) => (
              <tr key={activity.id}>
                <td>{formatDate(activity.date)}</td>
                <td>{activity.startTime}</td>
                <td>{activity.endTime}</td>
                <td>{activity.activityType}</td>
                <td>{activity.activitySubject}</td>
                <td>{activity.description}</td>
                <td>{formatDuration(activity.durationMinutes)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

/** Table Skeleton */
function TableSkeleton({ title, rows }: { title: string; rows: number }) {
  return (
    <div className="hours-card">
      <h3 className="hours-title">{title}</h3>
      <table className="hours-table">
        <thead>
          <tr>
            <th scope="col">ACTIVITY TYPE</th>
            <th scope="col">TOTAL HOURS</th>
          </tr>
        </thead>
        <tbody>
          {Array.from({ length: rows }).map((_, i) => (
            <tr key={i} aria-hidden="true">
              <td>
                <div className="skeleton-box w-24" />
              </td>
              <td>
                <div className="skeleton-box w-16 ml-auto" />
              </td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr aria-hidden="true">
            <th scope="row">
              <div className="skeleton-box w-12" />
            </th>
            <td>
              <div className="skeleton-box w-20 ml-auto" />
            </td>
          </tr>
        </tfoot>
      </table>
    </div>
  );
}

/** Empty Table */
function EmptyTable({ title }: { title: string }) {
  return (
    <div className="hours-card">
      <h3 className="hours-title">{title}</h3>
      <table className="hours-table">
        <thead>
          <tr>
            <th scope="col">ACTIVITY TYPE</th>
            <th scope="col">TOTAL HOURS</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td colSpan={2} className="empty-table-cell">
              <div className="empty-table-content">
                <span className="material-symbols-outlined" aria-hidden="true">
                  info
                </span>
                Data unavailable
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  );
}

export default MonthlyReportPage;
