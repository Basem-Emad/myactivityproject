import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router";

import Button from "../../components/ui/Button";
import ConfirmModal from "../../components/ui/ConfirmModal";
import EmptyState from "../../components/ui/EmptyState";
import ErrorAlert from "../../components/ui/ErrorAlert";
import LoadingSkeleton from "../../components/ui/LoadingSkeleton";
import SuccessAlert from "../../components/ui/SuccessAlert";

import {
  getActivitiesByDateRange,
  deleteActivity,
} from "../../services/activityEntryService";
import { getActivityTypes } from "../../services/activityTypeService";
import { getActivitySubjects } from "../../services/activitySubjectService";

import type { ActivityEntry } from "../../types/activity";
import type { ActivityType, ActivitySubject } from "../../types/masterData";
import { getApiErrorMessage } from "../../utils/apiError";

import "./ActivitiesPage.css";

/**
 * Returns `YYYY-MM` for the current month.
 */
function getCurrentMonth(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  return `${year}-${month}`;
}

/**
 * Returns the first day and last day of a YYYY-MM month string.
 */
function getMonthRange(month: string): { startDate: string; endDate: string } {
  const [year, m] = month.split("-").map(Number);
  const startDate = `${year}-${String(m).padStart(2, "0")}-01`;

  // Day 0 of the next month is the last day of the current month
  const lastDay = new Date(year, m, 0).getDate();
  const endDate = `${year}-${String(m).padStart(2, "0")}-${String(lastDay).padStart(2, "0")}`;

  return { startDate, endDate };
}

export default function ActivitiesPage() {
  const navigate = useNavigate();
  const location = useLocation();

  // ── Data state ──────────────────────────────────────────────
  const [activities, setActivities] = useState<ActivityEntry[]>([]);
  const [types, setTypes] = useState<ActivityType[]>([]);
  const [subjects, setSubjects] = useState<ActivitySubject[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(
    () => (location.state as { successMessage?: string } | null)?.successMessage ?? null,
  );

  // Clear navigation state once consumed so message does not reappear on refresh
  useEffect(() => {
    if (location.state?.successMessage) {
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  // ── Filter state ────────────────────────────────────────────
  const [filterMonth, setFilterMonth] = useState(getCurrentMonth);
  const [filterTypeId, setFilterTypeId] = useState("");
  const [filterSubjectId, setFilterSubjectId] = useState("");

  // ── Delete confirmation state ──────────────────────────────
  const [entryToDelete, setEntryToDelete] = useState<ActivityEntry | null>(null);
  const [deleting, setDeleting] = useState(false);

  // ── Data fetching ───────────────────────────────────────────
  async function loadActivities(month: string) {
    try {
      setLoading(true);
      const { startDate, endDate } = getMonthRange(month);

      const [activitiesData, typesData, subjectsData] = await Promise.all([
        getActivitiesByDateRange(startDate, endDate),
        getActivityTypes(),
        getActivitySubjects(),
      ]);

      setActivities(activitiesData);
      setTypes(typesData);
      setSubjects(subjectsData);
      setError(null);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  function handleManualRefresh() {
    loadActivities(filterMonth);
  }

  function handleMonthChange(e: React.ChangeEvent<HTMLInputElement>) {
    const newMonth = e.target.value;
    setFilterMonth(newMonth);
    loadActivities(newMonth);
  }

  useEffect(() => {
    let ignore = false;
    async function fetchInitial() {
      try {
        const { startDate, endDate } = getMonthRange(getCurrentMonth());

        const [activitiesData, typesData, subjectsData] = await Promise.all([
          getActivitiesByDateRange(startDate, endDate),
          getActivityTypes(),
          getActivitySubjects(),
        ]);

        if (!ignore) {
          setActivities(activitiesData);
          setTypes(typesData);
          setSubjects(subjectsData);
          setError(null);
        }
      } catch (err) {
        if (!ignore) {
          setError(getApiErrorMessage(err));
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }
    fetchInitial();
    return () => {
      ignore = true;
    };
  }, []);

  // ── Client-side filtering ──────────────────────────────────
  const filteredActivities = useMemo(() => {
    let result = activities;

    if (filterTypeId) {
      result = result.filter(
        (a) => a.activityTypeId === Number(filterTypeId),
      );
    }

    if (filterSubjectId) {
      result = result.filter(
        (a) => a.activitySubjectId === Number(filterSubjectId),
      );
    }

    return result;
  }, [activities, filterTypeId, filterSubjectId]);

  // ── Delete handlers ────────────────────────────────────────
  async function handleConfirmDelete() {
    if (!entryToDelete) return;

    try {
      setDeleting(true);
      setError(null);
      await deleteActivity(entryToDelete.id);
      setEntryToDelete(null);
      setSuccessMessage("Activity successfully deleted.");
      await loadActivities(filterMonth);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setDeleting(false);
    }
  }

  // ── Format helpers ─────────────────────────────────────────
  function formatTime(timeStr: string): string {
    // "09:30:00" → "09:30"
    return timeStr.slice(0, 5);
  }

  function formatDate(dateStr: string): string {
    // "2026-08-13" → "13 Aug 2026"
    const d = new Date(dateStr + "T00:00:00");
    return d.toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  }

  // ── Render ─────────────────────────────────────────────────
  return (
    <div className="activities-page">
      {/* Header */}
      <div className="activities-page__header">
        <h1>My Activities</h1>
        <Button icon="add" onClick={() => navigate("/activities/new")}>
          Add Activity
        </Button>
      </div>

      {successMessage && (
        <div style={{ marginBottom: 20 }}>
          <SuccessAlert
            message={successMessage}
            onClose={() => setSuccessMessage(null)}
          />
        </div>
      )}

      {/* Filter bar */}
      <div className="activities-page__filters">
        <label className="activities-page__filter-field">
          Month
          <input
            type="month"
            value={filterMonth}
            onChange={handleMonthChange}
          />
        </label>

        <label className="activities-page__filter-field">
          Activity Type
          <select
            value={filterTypeId}
            onChange={(e) => setFilterTypeId(e.target.value)}
          >
            <option value="">All Types</option>
            { (Array.isArray(types) ? types : []).map((t) => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
        </label>

        <label className="activities-page__filter-field">
          Activity Subject
          <select
            value={filterSubjectId}
            onChange={(e) => setFilterSubjectId(e.target.value)}
          >
            <option value="">All Subjects</option>
            { (Array.isArray(subjects) ? subjects : []).map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </label>
      </div>

      {/* Content */}
      {loading ? (
        <div className="activities-page__loading">
          <LoadingSkeleton height="40px" />
          <LoadingSkeleton height="40px" />
          <LoadingSkeleton height="40px" />
          <LoadingSkeleton height="40px" />
        </div>
      ) : error ? (
        <ErrorAlert message={error} onRetry={handleManualRefresh} />
      ) : filteredActivities.length === 0 ? (
        <EmptyState
          title={
            activities.length === 0
              ? "No activities this month"
              : "No matching activities"
          }
          message={
            activities.length === 0
              ? 'Click "Add Activity" to record your first entry for this period.'
              : "Try adjusting your filters to find what you're looking for."
          }
          icon="event_note"
          action={
            activities.length === 0 ? (
              <Button icon="add" onClick={() => navigate("/activities/new")}>
                Add Activity
              </Button>
            ) : undefined
          }
        />
      ) : (
        <table className="activities-page__table">
          <thead>
            <tr>
              <th>Date</th>
              <th>From</th>
              <th>To</th>
              <th>Duration</th>
              <th>Type</th>
              <th>Subject</th>
              <th>Description</th>
              <th aria-label="Actions" />
            </tr>
          </thead>
          <tbody>
            { (Array.isArray(filteredActivities) ? filteredActivities : []).map((entry) => (
              <tr key={entry.id}>
                <td>{formatDate(entry.date)}</td>
                <td>{formatTime(entry.startTime)}</td>
                <td>{formatTime(entry.endTime)}</td>
                <td>
                  <span className="activities-page__duration">
                    <span
                      className="material-symbols-outlined"
                      aria-hidden="true"
                    >
                      timer
                    </span>
                    {entry.durationFormatted}
                  </span>
                </td>
                <td>{entry.activityTypeName}</td>
                <td>{entry.activitySubjectName}</td>
                <td className="activities-page__description">
                  {entry.taskDescription}
                </td>
                <td className="activities-page__actions">
                  <Button
                    variant="secondary"
                    icon="edit"
                    onClick={() => navigate(`/activities/${entry.id}/edit`)}
                  >
                    Edit
                  </Button>
                  <Button
                    variant="danger"
                    icon="delete"
                    onClick={() => setEntryToDelete(entry)}
                  >
                    Delete
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <ConfirmModal
        isOpen={entryToDelete !== null}
        title="Delete Activity"
        message={
          entryToDelete ? (
            <span>
              Are you sure you want to delete this activity from{" "}
              <strong>{formatDate(entryToDelete.date)}</strong> (
              <em>
                {entryToDelete.activityTypeName} – {entryToDelete.activitySubjectName}
              </em>
              )? This action cannot be undone.
            </span>
          ) : null
        }
        confirmLabel="Delete"
        cancelLabel="Cancel"
        confirmVariant="danger"
        loading={deleting}
        onConfirm={handleConfirmDelete}
        onCancel={() => {
          if (!deleting) setEntryToDelete(null);
        }}
      />
    </div>
  );
}
