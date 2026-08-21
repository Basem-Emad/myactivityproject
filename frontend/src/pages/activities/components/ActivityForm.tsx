import { useEffect, useMemo, useState } from "react";

import Button from "../../../components/ui/Button";
import LoadingSkeleton from "../../../components/ui/LoadingSkeleton";

import { getActivityTypes } from "../../../services/activityTypeService";
import { getActivitySubjects } from "../../../services/activitySubjectService";
import type { ActivityType, ActivitySubject } from "../../../types/masterData";
import type { ActivityEntryRequest } from "../../../types/activity";

import "./ActivityForm.css";

interface ActivityFormProps {
  /** Pre-filled data when editing. Omit for "Add" mode. */
  initialData?: ActivityEntryRequest;
  /** Called with the validated form payload on submit. */
  onSubmit: (data: ActivityEntryRequest) => void;
  /** Called when the user clicks Cancel. */
  onCancel: () => void;
  /** Disables the submit button and shows a spinner. */
  isLoading: boolean;
  /** API error message to display above the action buttons. */
  submitError: string | null;
  /** Called when form fields change to clear stale API errors. */
  onClearSubmitError?: () => void;
}

/**
 * Calculates a human-readable duration string from two HH:MM time strings.
 * Returns null if either value is empty or if the range is invalid.
 */
function formatDurationPreview(
  startTime: string,
  endTime: string,
): string | null {
  if (!startTime || !endTime) return null;

  const [sh, sm] = startTime.split(":").map(Number);
  const [eh, em] = endTime.split(":").map(Number);

  const startMinutes = sh * 60 + sm;
  const endMinutes = eh * 60 + em;

  if (endMinutes <= startMinutes) return null;

  const diff = endMinutes - startMinutes;
  const hours = Math.floor(diff / 60);
  const minutes = diff % 60;

  return `${hours}h ${minutes}m`;
}

export default function ActivityForm({
  initialData,
  onSubmit,
  onCancel,
  isLoading,
  submitError,
  onClearSubmitError,
}: ActivityFormProps) {
  // ── Dropdown option state ───────────────────────────────────
  const [types, setTypes] = useState<ActivityType[]>([]);
  const [subjects, setSubjects] = useState<ActivitySubject[]>([]);
  const [optionsLoading, setOptionsLoading] = useState(true);
  const [optionsError, setOptionsError] = useState<string | null>(null);

  // ── Form field state ────────────────────────────────────────
  const [date, setDate] = useState(initialData?.date ?? "");
  const [startTime, setStartTime] = useState(
    initialData?.startTime ? initialData.startTime.slice(0, 5) : "",
  );
  const [endTime, setEndTime] = useState(
    initialData?.endTime ? initialData.endTime.slice(0, 5) : "",
  );
  const [activityTypeId, setActivityTypeId] = useState<string>(
    initialData?.activityTypeId?.toString() ?? "",
  );
  const [activitySubjectId, setActivitySubjectId] = useState<string>(
    initialData?.activitySubjectId?.toString() ?? "",
  );
  const [taskDescription, setTaskDescription] = useState(
    initialData?.taskDescription ?? "",
  );
  const [validationError, setValidationError] = useState<string | null>(null);

  // ── Load dropdown options ───────────────────────────────────
  useEffect(() => {
    let cancelled = false;

    async function loadOptions() {
      setOptionsLoading(true);
      setOptionsError(null);
      try {
        const [typesData, subjectsData] = await Promise.all([
          getActivityTypes(),
          getActivitySubjects(),
        ]);
        if (!cancelled) {
          setTypes(
            typesData.filter(
              (t) => t.active || (initialData && t.id === initialData.activityTypeId)
            )
          );
          setSubjects(
            subjectsData.filter(
              (s) => s.active || (initialData && s.id === initialData.activitySubjectId)
            )
          );
        }
      } catch {
        if (!cancelled) {
          setOptionsError(
            "Failed to load activity types or subjects. Please refresh the page.",
          );
        }
      } finally {
        if (!cancelled) {
          setOptionsLoading(false);
        }
      }
    }

    loadOptions();
    return () => {
      cancelled = true;
    };
  }, [initialData]);

  // ── Sync form fields when initialData changes (edit mode) ──
  const [prevInitialData, setPrevInitialData] = useState(initialData);
  if (initialData !== prevInitialData) {
    setPrevInitialData(initialData);
    if (initialData) {
      setDate(initialData.date ?? "");
      setStartTime(
        initialData.startTime ? initialData.startTime.slice(0, 5) : "",
      );
      setEndTime(
        initialData.endTime ? initialData.endTime.slice(0, 5) : "",
      );
      setActivityTypeId(initialData.activityTypeId?.toString() ?? "");
      setActivitySubjectId(initialData.activitySubjectId?.toString() ?? "");
      setTaskDescription(initialData.taskDescription ?? "");
    }
  }

  // ── Duration preview ────────────────────────────────────────
  const durationPreview = useMemo(
    () => formatDurationPreview(startTime, endTime),
    [startTime, endTime],
  );

  // ── Error clearing helper ────────────────────────────────────
  function clearErrors() {
    if (validationError) {
      setValidationError(null);
    }
    if (submitError && onClearSubmitError) {
      onClearSubmitError();
    }
  }

  // ── Submit handler ──────────────────────────────────────────
  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    clearErrors();

    // Client-side time validation
    if (startTime && endTime) {
      const [sh, sm] = startTime.split(":").map(Number);
      const [eh, em] = endTime.split(":").map(Number);
      if (eh * 60 + em <= sh * 60 + sm) {
        setValidationError("End time must be after start time.");
        return;
      }
    }

    const formatTime = (t: string) => {
      const parts = t.split(":");
      const h = (parts[0] || "0").padStart(2, "0");
      const m = (parts[1] || "0").padStart(2, "0");
      const s = parts[2] ? parts[2].padStart(2, "0") : "00";
      return `${h}:${m}:${s}`;
    };

    const payload: ActivityEntryRequest = {
      date,
      startTime: formatTime(startTime),
      endTime: formatTime(endTime),
      activityTypeId: Number(activityTypeId),
      activitySubjectId: Number(activitySubjectId),
      taskDescription: taskDescription.trim(),
    };

    onSubmit(payload);
  }

  // ── Render ──────────────────────────────────────────────────
  if (optionsLoading) {
    return (
      <div className="activity-form">
        <LoadingSkeleton height="40px" />
        <LoadingSkeleton height="40px" />
        <LoadingSkeleton height="40px" />
      </div>
    );
  }

  if (optionsError) {
    return (
      <div className="activity-form">
        <p className="activity-form__error">{optionsError}</p>
        <div className="activity-form__actions">
          <Button variant="secondary" onClick={onCancel}>
            Go Back
          </Button>
        </div>
      </div>
    );
  }

  return (
    <form className="activity-form" onSubmit={handleSubmit}>
      {/* Date */}
      <label className="activity-form__field">
        Date
        <input
          type="date"
          value={date}
          onChange={(e) => {
            setDate(e.target.value);
            clearErrors();
          }}
          required
        />
      </label>

      {/* Time Range */}
      <div className="activity-form__row">
        <label className="activity-form__field">
          From
          <input
            type="time"
            value={startTime}
            onChange={(e) => {
              setStartTime(e.target.value);
              clearErrors();
            }}
            required
          />
        </label>

        <label className="activity-form__field">
          To
          <input
            type="time"
            value={endTime}
            onChange={(e) => {
              setEndTime(e.target.value);
              clearErrors();
            }}
            required
          />
        </label>
      </div>

      {/* Duration Preview */}
      {durationPreview && (
        <div className="activity-form__duration-preview">
          <span className="material-symbols-outlined" aria-hidden="true">
            timer
          </span>
          Calculated duration:{" "}
          <span className="activity-form__duration-value">
            {durationPreview}
          </span>
        </div>
      )}

      {/* Activity Type */}
      <label className="activity-form__field">
        Activity Type
        <select
          value={activityTypeId}
          onChange={(e) => {
            setActivityTypeId(e.target.value);
            clearErrors();
          }}
          required
        >
          <option value="" disabled>
            Select a type…
          </option>
          { (Array.isArray(types) ? types : []).map((t) => (
            <option key={t.id} value={t.id}>
              {t.name}{!t.active ? " (Inactive)" : ""}
            </option>
          ))}
        </select>
      </label>

      {/* Activity Subject */}
      <label className="activity-form__field">
        Activity Subject
        <select
          value={activitySubjectId}
          onChange={(e) => {
            setActivitySubjectId(e.target.value);
            clearErrors();
          }}
          required
        >
          <option value="" disabled>
            Select a subject…
          </option>
          { (Array.isArray(subjects) ? subjects : []).map((s) => (
            <option key={s.id} value={s.id}>
              {s.name}{!s.active ? " (Inactive)" : ""}
            </option>
          ))}
        </select>
      </label>

      {/* Task Description */}
      <label className="activity-form__field">
        Task Description
        <textarea
          value={taskDescription}
          onChange={(e) => {
            setTaskDescription(e.target.value);
            clearErrors();
          }}
          required
          maxLength={2000}
          rows={4}
          placeholder="Describe what you worked on…"
        />
      </label>

      {/* Errors */}
      {validationError && (
        <p className="activity-form__error">{validationError}</p>
      )}
      {submitError && <p className="activity-form__error">{submitError}</p>}

      {/* Actions */}
      <div className="activity-form__actions">
        <Button variant="secondary" type="button" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={isLoading}>
          {initialData ? "Save Changes" : "Create Activity"}
        </Button>
      </div>
    </form>
  );
}
