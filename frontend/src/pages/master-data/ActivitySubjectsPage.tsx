import { useEffect, useState } from "react";

import Button from "../../components/ui/Button";
import EmptyState from "../../components/ui/EmptyState";
import ErrorAlert from "../../components/ui/ErrorAlert";
import LoadingSkeleton from "../../components/ui/LoadingSkeleton";

import {
  getActivitySubjects,
  createActivitySubject,
  updateActivitySubject,
  deactivateActivitySubject,
} from "../../services/activitySubjectService";
import type {
  ActivitySubject,
  ActivitySubjectRequest,
  SubjectType,
} from "../../types/masterData";
import { getApiErrorMessage } from "../../utils/apiError";

import "./ActivitySubjectsPage.css";

const SUBJECT_TYPE_OPTIONS: SubjectType[] = [
  "PROJECT",
  "PRODUCT",
  "POC",
  "COURSE",
  "MEETING",
  "OTHER",
];

export default function ActivitySubjectsPage() {
  const [items, setItems] = useState<ActivitySubject[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<ActivitySubject | null>(null);
  const [formName, setFormName] = useState("");
  const [formSubjectType, setFormSubjectType] = useState<SubjectType>("PROJECT");
  const [formDescription, setFormDescription] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function loadData() {
    setLoading(true);
    setError(null);

    try {
      const data = await getActivitySubjects();
      setItems(data);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  function openCreateForm() {
    setEditingItem(null);
    setFormName("");
    setFormSubjectType("PROJECT");
    setFormDescription("");
    setFormError(null);
    setIsFormOpen(true);
  }

  function openEditForm(item: ActivitySubject) {
    setEditingItem(item);
    setFormName(item.name);
    setFormSubjectType(item.subjectType);
    setFormDescription(item.description ?? "");
    setFormError(null);
    setIsFormOpen(true);
  }

  function closeForm() {
    setIsFormOpen(false);
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setFormError(null);

    const payload: ActivitySubjectRequest = {
      name: formName.trim(),
      subjectType: formSubjectType,
      description: formDescription.trim() || undefined,
    };

    try {
      if (editingItem) {
        await updateActivitySubject(editingItem.id, payload);
      } else {
        await createActivitySubject(payload);
      }
      setIsFormOpen(false);
      await loadData();
    } catch (err) {
      setFormError(getApiErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDeactivate(item: ActivitySubject) {
    const confirmed = window.confirm(
        `Deactivate "${item.name}"? It will no longer be available for new activity entries.`
    );
    if (!confirmed) return;

    try {
      await deactivateActivitySubject(item.id);
      await loadData();
    } catch (err) {
      setError(getApiErrorMessage(err));
    }
  }

  return (
      <div className="activity-subjects-page">
        <div className="activity-subjects-page__header">
          <h1>Activity Subjects</h1>
          <Button icon="add" onClick={openCreateForm}>
            New Activity Subject
          </Button>
        </div>

        {loading ? (
            <div className="activity-subjects-page__loading">
              <LoadingSkeleton height="40px" />
              <LoadingSkeleton height="40px" />
              <LoadingSkeleton height="40px" />
            </div>
        ) : error ? (
            <ErrorAlert message={error} onRetry={loadData} />
        ) : items.length === 0 ? (
            <EmptyState
                title="No Activity Subjects yet"
                message="Create your first Activity Subject to get started."
                icon="topic"
                action={
                  <Button icon="add" onClick={openCreateForm}>
                    New Activity Subject
                  </Button>
                }
            />
        ) : (
            <table className="activity-subjects-page__table">
              <thead>
              <tr>
                <th>Name</th>
                <th>Type</th>
                <th>Description</th>
                <th>Status</th>
                <th aria-label="Actions" />
              </tr>
              </thead>
              <tbody>
              {items.map((item) => (
                  <tr key={item.id}>
                    <td>{item.name}</td>
                    <td>{item.subjectType}</td>
                    <td>{item.description || "—"}</td>
                    <td>
                  <span
                      className={`activity-subjects-page__status activity-subjects-page__status--${
                          item.active ? "active" : "inactive"
                      }`}
                  >
                    {item.active ? "Active" : "Inactive"}
                  </span>
                    </td>
                    <td className="activity-subjects-page__actions">
                      <Button variant="secondary" icon="edit" onClick={() => openEditForm(item)}>
                        Edit
                      </Button>
                      {item.active && (
                          <Button
                              variant="danger"
                              icon="block"
                              onClick={() => handleDeactivate(item)}
                          >
                            Deactivate
                          </Button>
                      )}
                    </td>
                  </tr>
              ))}
              </tbody>
            </table>
        )}

        {isFormOpen && (
            <div className="activity-subjects-page__modal-overlay" onClick={closeForm}>
              <div
                  className="activity-subjects-page__modal"
                  onClick={(e) => e.stopPropagation()}
              >
                <h2>{editingItem ? "Edit Activity Subject" : "New Activity Subject"}</h2>

                <form onSubmit={handleSubmit}>
                  <label className="activity-subjects-page__field">
                    Name
                    <input
                        type="text"
                        value={formName}
                        onChange={(e) => setFormName(e.target.value)}
                        required
                        maxLength={255}
                    />
                  </label>

                  <label className="activity-subjects-page__field">
                    Subject Type
                    <select
                        value={formSubjectType}
                        onChange={(e) => setFormSubjectType(e.target.value as SubjectType)}
                        required
                    >
                      {SUBJECT_TYPE_OPTIONS.map((option) => (
                          <option key={option} value={option}>
                            {option}
                          </option>
                      ))}
                    </select>
                  </label>

                  <label className="activity-subjects-page__field">
                    Description
                    <textarea
                        value={formDescription}
                        onChange={(e) => setFormDescription(e.target.value)}
                        maxLength={500}
                        rows={3}
                    />
                  </label>

                  {formError && (
                      <p className="activity-subjects-page__form-error">{formError}</p>
                  )}

                  <div className="activity-subjects-page__modal-actions">
                    <Button variant="secondary" type="button" onClick={closeForm}>
                      Cancel
                    </Button>
                    <Button type="submit" loading={submitting}>
                      {editingItem ? "Save Changes" : "Create"}
                    </Button>
                  </div>
                </form>
              </div>
            </div>
        )}
      </div>
  );
}
