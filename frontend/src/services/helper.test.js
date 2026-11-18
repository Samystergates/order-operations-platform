import { privateAxios } from "./helper";

describe("helper axios clients", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("adds bearer token to private requests", () => {
    localStorage.setItem(
      "data",
      JSON.stringify({
        token: "token-123",
        user: { username: "admin" },
      })
    );

    const requestHandler = privateAxios.interceptors.request.handlers[0].fulfilled;
    const config = requestHandler({ headers: {} });

    expect(config.headers.Authorization).toBe("Bearer token-123");
  });

  it("logs out and dispatches auth expiry on unauthorized responses", async () => {
    localStorage.setItem(
      "data",
      JSON.stringify({
        token: "token-123",
        user: { username: "admin" },
      })
    );
    const authExpiredListener = jest.fn();
    window.addEventListener("auth:expired", authExpiredListener);

    const responseHandler = privateAxios.interceptors.response.handlers[0].rejected;

    await expect(
      responseHandler({
        response: {
          status: 401,
          data: {
            code: "UNAUTHORIZED",
            message: "Authentication required",
          },
        },
      })
    ).rejects.toMatchObject({
      isUnauthorized: true,
      normalizedMessage: "Authentication required",
    });

    expect(localStorage.getItem("data")).toBeNull();
    expect(authExpiredListener).toHaveBeenCalledTimes(1);

    window.removeEventListener("auth:expired", authExpiredListener);
  });
});
