import { useState } from "react";
import { mockMonthlySummary } from "../../services/mockDashboardData";
import SummaryMetricCard from "../../components/ui/SummaryMetricCard";
import ChartCard from "../../components/ui/ChartCard";
import "./DashboardPage.css";

// ─── Helpers ────────────────────────────────────────────────────────────────

function formatMonth(value: string): string {
  const [year, month] = value.split("-");
  const date = new Date(Number(year), Number(month) - 1, 1);
  return date.toLocaleString("en-US", { month: "long", year: "numeric" });
}

/** Compute bar width % relative to the maximum value in the list */
function barWidth(value: number, max: number): string {
  if (max === 0) return "0%";
  return `${Math.round((value / max) * 100)}%`;
}

/** Build full day-by-day dataset with zeros for non-working days */
function buildFullMonthData(trend: typeof mockMonthlySummary.dailyTrend, daysInMonth: number) {
  const dataMap = new Map(trend.map((d) => [d.day, d.totalMinutes]));
  const fullData: { day: number; totalMinutes: number }[] = [];
  
  for (let day = 1; day <= daysInMonth; day++) {
    fullData.push({ day, totalMinutes: dataMap.get(day) ?? 0 });
  }
  
  return fullData;
}

/** Build SVG polyline points for the trend chart */
function buildTrendPoints(
  trend: typeof mockMonthlySummary.dailyTrend,
  viewW: number,
  viewH: number,
  daysInMonth: number
): string {
  const fullData = buildFullMonthData(trend, daysInMonth);
  const maxMinutes = Math.max(...fullData.map((d) => d.totalMinutes), 1);

  return fullData
    .map((d) => {
      const x = ((d.day - 1) / (daysInMonth - 1)) * viewW;
      const y = viewH - (d.totalMinutes / maxMinutes) * viewH;
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    })
    .join(" ");
}

function buildTrendFill(
  trend: typeof mockMonthlySummary.dailyTrend,
  viewW: number,
  viewH: number,
  daysInMonth: number
): string {
  if (trend.length === 0) return "";
  const pts = buildTrendPoints(trend, viewW, viewH, daysInMonth);
  return `M 0,${viewH} L ${pts} L ${viewW},${viewH} Z`;
}

// ─── Sub-components ──────────────────────────────────────────────────────────

interface BarChartProps {
  rows: { name: string; totalMinutes: number; label: string }[];
}

function BarChart({ rows }: BarChartProps) {
  const max = Math.max(...rows.map((r) => r.totalMinutes), 1);
  return (
    <div className="bar-list">
      {rows.map((row) => (
        <div className="bar-row" key={row.name}>
          <div className="bar-row__meta">
            <span className="bar-row__name">{row.name}</span>
            <span className="bar-row__value">{row.label}</span>
          </div>
          <div className="bar-track">
            <div
              className="bar-fill"
              style={{ width: barWidth(row.totalMinutes, max) }}
            />
          </div>
        </div>
      ))}
    </div>
  );
}

interface TrendChartProps {
  trend: typeof mockMonthlySummary.dailyTrend;
  totalLabel: string;
  month: string;
}

function TrendChart({ trend, totalLabel, month }: TrendChartProps) {
  const [expanded, setExpanded] = useState(false);
  const VW = 300;
  const VH = 100;
  
  // Get actual days in the selected month
  const [year, monthNum] = month.split("-");
  const daysInMonth = new Date(Number(year), Number(monthNum), 0).getDate();
  
  const points = buildTrendPoints(trend, VW, VH, daysInMonth);
  const fillPath = buildTrendFill(trend, VW, VH, daysInMonth);
  const maxMinutes = Math.max(...trend.map((d) => d.totalMinutes), 1);
  const maxHours = Math.round(maxMinutes / 60);
  const midHours = Math.round(maxHours / 2);

  return (
    <div className="trend-chart-wrapper">
      {/* Chart area */}
      <div className="trend-chart">
        {/* Y-axis labels */}
        <div className="trend-chart__y-axis">
          <span>{maxHours}h</span>
          <span>{midHours}h</span>
          <span>0h</span>
        </div>

        {/* SVG plot */}
        <div className="trend-chart__plot">
          {/* Grid lines */}
          <div className="trend-chart__grid">
            <div className="trend-chart__grid-line trend-chart__grid-line--dashed" />
            <div className="trend-chart__grid-line trend-chart__grid-line--dashed" />
            <div className="trend-chart__grid-line" />
          </div>

          <svg
            viewBox={`0 0 ${VW} ${VH}`}
            preserveAspectRatio="none"
            className="trend-chart__svg"
            aria-hidden="true"
          >
            <path d={fillPath} fill="#2563EB" fillOpacity="0.1" />
            <polyline
              points={points}
              fill="none"
              stroke="#2563EB"
              strokeWidth="2.5"
              vectorEffect="non-scaling-stroke"
            />
          </svg>
        </div>
      </div>

      {/* X-axis labels */}
      <div className="trend-chart__x-axis">
        {[1, 5, 10, 15, 20, 25, daysInMonth].map((d) => (
          <span key={d}>{d}</span>
        ))}
      </div>

      {/* Footer */}
      <div className="trend-footer">
        <p className="trend-footer__total">Monthly total: {totalLabel}</p>

        <details
          className="trend-details"
          open={expanded}
          onToggle={(e) => setExpanded((e.target as HTMLDetailsElement).open)}
        >
          <summary className="trend-details__summary">
            <span
              className={`material-symbols-outlined trend-details__chevron ${
                expanded ? "trend-details__chevron--open" : ""
              }`}
            >
              expand_more
            </span>
            View daily values
          </summary>

          <div className="trend-table-wrap">
            <table className="trend-table">
              <thead>
                <tr>
                  <th>Day</th>
                  <th>Duration</th>
                </tr>
              </thead>
              <tbody>
                {trend.map((d) => (
                  <tr key={d.day}>
                    <td>
                      {formatMonth(month).split(" ")[0]} {d.day}
                    </td>
                    <td>{d.label}</td>
                  </tr>
                ))}
                <tr className="trend-table__total-row">
                  <td>Total</td>
                  <td>{totalLabel}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </details>
      </div>
    </div>
  );
}

// ─── Page ────────────────────────────────────────────────────────────────────

export default function DashboardPage() {
  const [month, setMonth] = useState("2026-08");
  const summary = mockMonthlySummary;

  return (
    <div className="dashboard-page">
      {/* Page header */}
      <div className="dashboard-page__header">
        <div>
          <h1 className="dashboard-page__title">Monthly Dashboard</h1>
          <p className="dashboard-page__subtitle">
            Overview of your recorded working hours for the selected month
          </p>
        </div>

        {/* Month picker */}
        <div className="dashboard-page__month-picker">
          <label className="dashboard-page__month-label" htmlFor="dashboard-month">
            Month
          </label>
          <input
            id="dashboard-month"
            type="month"
            className="dashboard-page__month-input"
            value={month}
            onChange={(e) => setMonth(e.target.value)}
          />
        </div>
      </div>

      {/* Summary cards */}
      <div className="dashboard-page__summary-cards">
        <SummaryMetricCard icon="schedule" label="Total Hours">
          {summary.totalHours}
          <span className="metric-unit">h</span> {summary.totalMinutes}
          <span className="metric-unit">m</span>
        </SummaryMetricCard>

        <SummaryMetricCard icon="event_available" label="Total Days">
          {summary.totalDays}
        </SummaryMetricCard>

        <SummaryMetricCard icon="insights" label="Average Hours / Day">
          {summary.avgHours}
          <span className="metric-unit">h</span> {summary.avgMinutes}
          <span className="metric-unit">m</span>
        </SummaryMetricCard>
      </div>

      {/* Charts — bar charts side by side */}
      <div className="dashboard-page__charts-grid">
        <ChartCard title="Hours by Activity Type">
          <BarChart
            rows={summary.byActivityType.map((r) => ({
              name: r.activityType,
              totalMinutes: r.totalMinutes,
              label: r.label,
            }))}
          />
        </ChartCard>

        <ChartCard title="Hours by Activity Subject">
          <BarChart
            rows={summary.byActivitySubject.map((r) => ({
              name: r.activitySubject,
              totalMinutes: r.totalMinutes,
              label: r.label,
            }))}
          />
        </ChartCard>
      </div>

      {/* Trend chart */}
      <ChartCard
        title="Daily Hours Trend"
        subtitle={`Recorded duration for each day in ${formatMonth(month)}`}
        badge={`${summary.totalDays} active days`}
      >
        <TrendChart
          trend={summary.dailyTrend}
          totalLabel={summary.totalHoursLabel}
          month={month}
        />
      </ChartCard>
    </div>
  );
}
