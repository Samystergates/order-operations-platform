import {
  getApiErrorMessage,
  isUnauthorizedError,
  normalizeApiError,
} from "./api-error";

describe("api-error", () => {
  it("prefers backend error envelope messages", () => {
    const error = {
      response: {
        data: {
          message: "Request validation failed",
          errors: {
            username: "must not be blank",
          },
        },
      },
    };

    expect(getApiErrorMessage(error)).toBe(
      "Request validation failed: username: must not be blank"
    );
  });

  it("returns a user-safe message for network failures", () => {
    expect(getApiErrorMessage({ message: "Network Error" })).toBe(
      "Unable to reach the server. Check your connection and try again."
    );
  });

  it("marks unauthorized errors consistently", () => {
    const error = {
      response: {
        status: 401,
        data: {
          code: "UNAUTHORIZED",
          message: "Authentication required",
        },
      },
    };

    expect(isUnauthorizedError(error)).toBe(true);
    expect(normalizeApiError(error)).toMatchObject({
      apiCode: "UNAUTHORIZED",
      isUnauthorized: true,
      normalizedMessage: "Authentication required",
      status: 401,
    });
  });
});
