import { useState } from "react";
import { useNavigate } from "react-router";

import { createActivity } from "../../services/activityEntryService";
import type { ActivityEntryRequest } from "../../types/activity";
import { getApiErrorMessage } from "../../utils/apiError";

import ActivityForm from "./components/ActivityForm";

export default function AddActivityPage() {
  const navigate = useNavigate();

  const [isLoading, setIsLoading] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  async function handleSubmit(data: ActivityEntryRequest) {
    setIsLoading(true);
    setSubmitError(null);

    try {
      await createActivity(data);
      navigate("/activities", {
        state: { successMessage: "Activity recorded successfully." },
      });
    } catch (err) {
      setSubmitError(getApiErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }

  function handleCancel() {
    navigate("/activities");
  }

  return (
    <div>
      <h1>Add Activity</h1>
      <ActivityForm
        onSubmit={handleSubmit}
        onCancel={handleCancel}
        isLoading={isLoading}
        submitError={submitError}
        onClearSubmitError={() => setSubmitError(null)}
      />
    </div>
  );
}
