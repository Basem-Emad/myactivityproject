import { useEffect, useState } from "react";
import {
  getMonthlySummary,
  getHoursByType,
  getHoursBySubject,
  getDailyTrend,
} from "../../services/reportService";
import type {
  MonthlySummary,
  HoursByType,
  HoursBySubject,
  DailyTrend,
} from "../../types/dashboard";
import { formatMinutesToLabel } from "../../utils/formatDuration";
import SummaryMetricCard from "../../components/ui/SummaryMetricCard";
import ChartCard from "../../components/ui/ChartCard";
import LoadingSkeleton from "../../components/ui/LoadingSkeleton";
import ErrorAlert from "../../components/ui/ErrorAlert";
import "./DashboardPage.css";

// ─── Helpers ────────────────────────────────────────────────────────────────

function formatMonth(value: string): string {
  const [year, month] = value.split("-");
  const date = new Date(Number(year), Number(month) - 1, 1);
  return date.toLocaleString("en-US", { month: "long", year: "numeric" });
}

/** Extract the day-of-month number from an ISO date string like "2026-08-03" */
function dayOfMonth(isoDate: string): number {
  return Number(isoDate.split("-")[2]);
}

/** Compute bar width % relative to the maximum value in the list */
function barWidth(value: number, max: number): string {
  if (max === 0) return "0%";
  return `${Math.round((value / max) * 100)}%`;
}

/** Build full day-by-day dataset with zeros for non-working days */
function buildFullMonthData(trend: DailyTrend[], daysInMonth: number) {
  const dataMap = new Map(trend.map((d) => [dayOfMonth(d.date), d.totalDurationMinutes]));
  const fullData: { day: number; totalMinutes: number }[] = [];

  for (let day = 1; day <= daysInMonth; day++) {
    fullData.push({ day, totalMinutes: dataMap.get(day) ?? 0 });
  }

  return fullData;
}

/** Build SVG polyline points for the trend chart */
function buildTrendPoints(
    trend: DailyTrend[],
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
    trend: DailyTrend[],
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
  trend: DailyTrend[];
  totalLabel: string;
  month: string;
}

function TrendChart({ trend, totalLabel, month }: TrendChartProps) {
  const [expanded, setExpanded] = useState(false);
  const VW = 300;
  const VH = 100;

  const [year, monthNum] = month.split("-");
  const daysInMonth = new Date(Number(year), Number(monthNum), 0).getDate();

  const points = buildTrendPoints(trend, VW, VH, daysInMonth);
  const fillPath = buildTrendFill(trend, VW, VH, daysInMonth);
  const maxMinutes = Math.max(...trend.map((d) => d.totalDurationMinutes), 1);
  const maxHours = Math.round(maxMinutes / 60);
  const midHours = Math.round(maxHours / 2);

  return (
      <div className="trend-chart-wrapper">
        <div className="trend-chart">
          <div className="trend-chart__y-axis">
            <span>{maxHours}h</span>
            <span>{midHours}h</span>
            <span>0h</span>
          </div>

          <div className="trend-chart__plot">
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

        <div className="trend-chart__x-axis">
          {[1, 5, 10, 15, 20, 25, daysInMonth].map((d) => (
              <span key={d}>{d}</span>
          ))}
        </div>

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
                    <tr key={d.date}>
                      <td>
                        {formatMonth(month).split(" ")[0]} {dayOfMonth(d.date)}
                      </td>
                      <td>{formatMinutesToLabel(d.totalDurationMinutes)}</td>
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

  const [summary, setSummary] = useState<MonthlySummary | null>(null);
  const [hoursByType, setHoursByType] = useState<HoursByType[]>([]);
  const [hoursBySubject, setHoursBySubject] = useState<HoursBySubject[]>([]);
  const [dailyTrend, setDailyTrend] = useState<DailyTrend[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadDashboard() {
    setLoading(true);
    setError(null);
    try {
      const [summaryData, typeData, subjectData, trendData] = await Promise.all([
        getMonthlySummary(month),
        getHoursByType(month),
        getHoursBySubject(month),
        getDailyTrend(month),
      ]);
      setSummary(summaryData);
      setHoursByType(typeData);
      setHoursBySubject(subjectData);
      setDailyTrend(trendData);
    } catch {
      setError("Couldn't load the dashboard data. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDashboard();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [month]);

  if (loading) {
    return (
        <div className="dashboard-page">
          <LoadingSkeleton height="40px" className="dashboard-page__title-skeleton" />
          <LoadingSkeleton height="120px" />
          <LoadingSkeleton height="300px" />
        </div>
    );
  }

  if (error || !summary) {
    return (
        <div className="dashboard-page">
          <ErrorAlert message={error ?? "No data available."} onRetry={loadDashboard} />
        </div>
    );
  }

  const totalLabel = formatMinutesToLabel(summary.totalDurationMinutes);
  const avgLabel = formatMinutesToLabel(summary.averageDurationMinutes);

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
            {totalLabel}
          </SummaryMetricCard>

          <SummaryMetricCard icon="event_available" label="Total Days">
            {summary.totalDays}
          </SummaryMetricCard>

          <SummaryMetricCard icon="insights" label="Average Hours / Day">
            {avgLabel}
          </SummaryMetricCard>
        </div>

        {/* Charts — bar charts side by side */}
        <div className="dashboard-page__charts-grid">
          <ChartCard title="Hours by Activity Type">
            <BarChart
                rows={hoursByType.map((r) => ({
                  name: r.activityType,
                  totalMinutes: r.totalDurationMinutes,
                  label: formatMinutesToLabel(r.totalDurationMinutes),
                }))}
            />
          </ChartCard>

          <ChartCard title="Hours by Activity Subject">
            <BarChart
                rows={hoursBySubject.map((r) => ({
                  name: r.activitySubject,
                  totalMinutes: r.totalDurationMinutes,
                  label: formatMinutesToLabel(r.totalDurationMinutes),
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
          <TrendChart trend={dailyTrend} totalLabel={totalLabel} month={month} />
        </ChartCard>
      </div>
  );
}
