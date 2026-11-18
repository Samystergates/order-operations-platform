const NETWORK_ERROR_MESSAGE = "Unable to reach the server. Check your connection and try again.";
const TIMEOUT_ERROR_MESSAGE = "The request timed out. Please try again.";

const formatFieldErrors = (errors) => {
  if (!errors || typeof errors !== "object") {
    return null;
  }

  const messages = Object.entries(errors)
    .filter(([, message]) => message != null && message !== "")
    .map(([field, message]) => `${field}: ${message}`);

  return messages.length > 0 ? messages.join("; ") : null;
};

export const getApiErrorMessage = (error, fallback = "Something went wrong") => {
  if (!error) {
    return fallback;
  }

  const responseData = error.response?.data;
  const fieldErrors = formatFieldErrors(responseData?.errors);

  if (responseData?.message && fieldErrors) {
    return `${responseData.message}: ${fieldErrors}`;
  }

  if (responseData?.message) {
    return responseData.message;
  }

  if (fieldErrors) {
    return fieldErrors;
  }

  if (responseData?.error) {
    return responseData.error;
  }

  if (error.code === "ECONNABORTED") {
    return TIMEOUT_ERROR_MESSAGE;
  }

  if (error.message === "Network Error") {
    return NETWORK_ERROR_MESSAGE;
  }

  if (error.message) {
    return error.message;
  }

  return fallback;
};

export const isUnauthorizedError = (error) => error?.response?.status === 401;

export const normalizeApiError = (error) => {
  const normalizedError = error || new Error("Unknown API error");
  normalizedError.normalizedMessage = getApiErrorMessage(normalizedError);
  normalizedError.isUnauthorized = isUnauthorizedError(normalizedError);
  normalizedError.status = normalizedError.response?.status;
  normalizedError.apiCode = normalizedError.response?.data?.code;
  normalizedError.fieldErrors = normalizedError.response?.data?.errors || {};
  return normalizedError;
};
