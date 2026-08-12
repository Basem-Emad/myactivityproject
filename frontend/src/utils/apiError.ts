import axios from "axios";

const DEFAULT_ERROR_MESSAGE =
  "Something went wrong. Please try again.";

function getStatusMessage(status?: number): string | undefined {
  if (!status) {
    return undefined;
  }

  if (status === 400) {
    return "The request could not be processed. Please check the entered information.";
  }

  if (status === 401) {
    return "Your session has expired. Please sign in again.";
  }

  if (status === 403) {
    return "You do not have permission to perform this action.";
  }

  if (status === 404) {
    return "The requested resource could not be found.";
  }

  if (status === 409) {
    return "The request conflicts with existing data.";
  }

  if (status >= 500) {
    return "The server encountered an error. Please try again.";
  }

  return undefined;
}

function getValidationMessage(data: unknown): string | undefined {
  if (
    typeof data !== "object" ||
    data === null ||
    !("message" in data)
  ) {
    return undefined;
  }

  const message = data.message;

  return typeof message === "string" && message.trim()
    ? message
    : undefined;
}

export function getApiErrorMessage(
  error: unknown,
  fallback = DEFAULT_ERROR_MESSAGE,
): string {
  if (!axios.isAxiosError(error)) {
    return error instanceof Error && error.message.trim()
      ? error.message
      : fallback;
  }

  if (!error.response) {
    return "Unable to connect to the server. Please check your connection and try again.";
  }

  const { status, data } = error.response;

  /*
   * Validation/conflict messages may contain useful feedback such as
   * overlapping activities or duplicate master-data names.
   *
   * Server-side failures intentionally use a generic message so internal
   * implementation details are never exposed in the UI.
   */
  if (status === 400 || status === 409 || status === 422) {
    const validationMessage = getValidationMessage(data);

    if (validationMessage) {
      return validationMessage;
    }
  }

  return getStatusMessage(status) ?? fallback;
}