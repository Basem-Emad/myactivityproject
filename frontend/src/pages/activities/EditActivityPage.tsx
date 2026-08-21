import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";

import LoadingSkeleton from "../../components/ui/LoadingSkeleton";
import ErrorAlert from "../../components/ui/ErrorAlert";

import {
  getActivityById,
  updateActivity,
} from "../../services/activityEntryService";
import type {
  ActivityEntry,
  ActivityEntryRequest,
} from "../../types/activity";
import { getApiErrorMessage } from "../../utils/apiError";

import ActivityForm from "./components/ActivityForm";

export default function EditActivityPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [activity, setActivity] = useState<ActivityEntry | null>(null);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState<string | null>(null);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadActivity() {
      setLoading(true);
      setFetchError(null);
      try {
        const data = await getActivityById(Number(id));
        if (!cancelled) {
          setActivity(data);
        }
      } catch (err) {
        if (!cancelled) {
          setFetchError(getApiErrorMessage(err));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadActivity();
    return () => {
      cancelled = true;
    };
  }, [id]);

  async function handleSubmit(data: ActivityEntryRequest) {
    setIsSubmitting(true);
    setSubmitError(null);

    try {
      await updateActivity(Number(id), data);
      navigate("/activities", {
        state: { successMessage: "Activity updated successfully." },
      });
    } catch (err) {
      setSubmitError(getApiErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleCancel() {
    navigate("/activities");
  }

  if (loading) {
    return (
      <div>
        <h1>Edit Activity</h1>
        <div style={{ display: "flex", flexDirection: "column", gap: 12, maxWidth: 600 }}>
          <LoadingSkeleton height="40px" />
          <LoadingSkeleton height="40px" />
          <LoadingSkeleton height="40px" />
          <LoadingSkeleton height="40px" />
        </div>
      </div>
    );
  }

  if (fetchError) {
    return (
      <div>
        <h1>Edit Activity</h1>
        <ErrorAlert
          message={fetchError}
          onRetry={() => window.location.reload()}
        />
      </div>
    );
  }

  /*
   * Convert the fetched ActivityEntry into an ActivityEntryRequest shape
   * that the form component expects as initialData.
   */
  const initialData: ActivityEntryRequest | undefined = activity
    ? {
        date: activity.date,
        startTime: activity.startTime,
        endTime: activity.endTime,
        activityTypeId: activity.activityTypeId,
        activitySubjectId: activity.activitySubjectId,
        taskDescription: activity.taskDescription,
      }
    : undefined;

  return (
    <div>
      <h1>Edit Activity</h1>
      <ActivityForm
        initialData={initialData}
        onSubmit={handleSubmit}
        onCancel={handleCancel}
        isLoading={isSubmitting}
        submitError={submitError}
        onClearSubmitError={() => setSubmitError(null)}
      />
    </div>
  );
}
