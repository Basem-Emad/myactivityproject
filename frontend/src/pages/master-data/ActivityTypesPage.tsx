import { useEffect, useState } from "react";

import Button from "../../components/ui/Button";
import ConfirmModal from "../../components/ui/ConfirmModal";
import EmptyState from "../../components/ui/EmptyState";
import ErrorAlert from "../../components/ui/ErrorAlert";
import LoadingSkeleton from "../../components/ui/LoadingSkeleton";
import SuccessAlert from "../../components/ui/SuccessAlert";

import {
  getActivityTypes,
  createActivityType,
  updateActivityType,
  deactivateActivityType,
} from "../../services/activityTypeService";
import type { ActivityType, ActivityTypeRequest } from "../../types/masterData";
import { getApiErrorMessage } from "../../utils/apiError";

import "./ActivityTypesPage.css";

export default function ActivityTypesPage() {
  const [items, setItems] = useState<ActivityType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<ActivityType | null>(null);
  const [formName, setFormName] = useState("");
  const [formDescription, setFormDescription] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  // Deactivate modal state
  const [itemToDeactivate, setItemToDeactivate] = useState<ActivityType | null>(null);
  const [deactivating, setDeactivating] = useState(false);

  async function loadData() {
    try {
      const data = await getActivityTypes();
      setItems(data);
      setError(null);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  function handleRetry() {
    setLoading(true);
    setError(null);
    loadData();
  }

  useEffect(() => {
    let ignore = false;
    async function fetchInitial() {
      try {
        const data = await getActivityTypes();
        if (!ignore) {
          setItems(data);
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

  function openCreateForm() {
    setEditingItem(null);
    setFormName("");
    setFormDescription("");
    setFormError(null);
    setIsFormOpen(true);
  }

  function openEditForm(item: ActivityType) {
    setEditingItem(item);
    setFormName(item.name);
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

    const payload: ActivityTypeRequest = {
      name: formName.trim(),
      description: formDescription.trim() || undefined,
    };

    try {
      if (editingItem) {
        await updateActivityType(editingItem.id, payload);
        setSuccessMessage(`Activity Type "${payload.name}" updated successfully.`);
      } else {
        await createActivityType(payload);
        setSuccessMessage(`Activity Type "${payload.name}" created successfully.`);
      }
      setIsFormOpen(false);
      await loadData();
    } catch (err) {
      setFormError(getApiErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleConfirmDeactivate() {
    if (!itemToDeactivate) return;

    try {
      setDeactivating(true);
      setError(null);
      await deactivateActivityType(itemToDeactivate.id);
      setSuccessMessage(`Activity Type "${itemToDeactivate.name}" deactivated successfully.`);
      setItemToDeactivate(null);
      await loadData();
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setDeactivating(false);
    }
  }

  return (
    <div className="activity-types-page">
      <div className="activity-types-page__header">
        <h1>Activity Types</h1>
        <Button icon="add" onClick={openCreateForm}>
          New Activity Type
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

      {loading ? (
        <div className="activity-types-page__loading">
          <LoadingSkeleton height="40px" />
          <LoadingSkeleton height="40px" />
          <LoadingSkeleton height="40px" />
        </div>
      ) : error ? (
        <ErrorAlert message={error} onRetry={handleRetry} />
      ) : items.length === 0 ? (
        <EmptyState
          title="No Activity Types yet"
          message="Create your first Activity Type to get started."
          icon="category"
          action={
            <Button icon="add" onClick={openCreateForm}>
              New Activity Type
            </Button>
          }
        />
      ) : (
        <table className="activity-types-page__table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Description</th>
              <th>Status</th>
              <th aria-label="Actions" />
            </tr>
          </thead>
          <tbody>
            {(Array.isArray(items) ? items : []).map((item) => (
              <tr key={item.id}>
                <td>{item.name}</td>
                <td>{item.description || "—"}</td>
                <td>
                  <span
                    className={`activity-types-page__status activity-types-page__status--${
                      item.active ? "active" : "inactive"
                    }`}
                  >
                    {item.active ? "Active" : "Inactive"}
                  </span>
                </td>
                <td className="activity-types-page__actions">
                  <Button
                    variant="secondary"
                    icon="edit"
                    onClick={() => openEditForm(item)}
                  >
                    Edit
                  </Button>
                  {item.active && (
                    <Button
                      variant="danger"
                      icon="block"
                      onClick={() => setItemToDeactivate(item)}
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
        <div className="activity-types-page__modal-overlay" onClick={closeForm}>
          <div
            className="activity-types-page__modal"
            onClick={(e) => e.stopPropagation()}
          >
            <h2>{editingItem ? "Edit Activity Type" : "New Activity Type"}</h2>

            <form onSubmit={handleSubmit}>
              <label className="activity-types-page__field">
                Name
                <input
                  type="text"
                  value={formName}
                  onChange={(e) => setFormName(e.target.value)}
                  required
                  maxLength={255}
                />
              </label>

              <label className="activity-types-page__field">
                Description
                <textarea
                  value={formDescription}
                  onChange={(e) => setFormDescription(e.target.value)}
                  maxLength={500}
                  rows={3}
                />
              </label>

              {formError && (
                <p className="activity-types-page__form-error">{formError}</p>
              )}

              <div className="activity-types-page__modal-actions">
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

      <ConfirmModal
        isOpen={itemToDeactivate !== null}
        title="Deactivate Activity Type"
        message={
          itemToDeactivate ? (
            <span>
              Are you sure you want to deactivate{" "}
              <strong>&ldquo;{itemToDeactivate.name}&rdquo;</strong>? It will no longer
              be available for new activity entries.
            </span>
          ) : null
        }
        confirmLabel="Deactivate"
        confirmVariant="danger"
        loading={deactivating}
        onConfirm={handleConfirmDeactivate}
        onCancel={() => {
          if (!deactivating) setItemToDeactivate(null);
        }}
      />
    </div>
  );
}
