import axios from "axios";
import { doLogout, getToken } from "../auth";
import { normalizeApiError } from "./api-error";

const DEFAULT_API_BASE_URL = "http://localhost:9090/api";
export const BASE_URL = process.env.REACT_APP_API_BASE_URL || DEFAULT_API_BASE_URL;
export const WS_BASE_URL = process.env.REACT_APP_WS_BASE_URL || BASE_URL.replace(/\/api$/, "");
const API_TIMEOUT_MS = Number(process.env.REACT_APP_API_TIMEOUT_MS || 15000);

export const myAxios = axios.create({
  baseURL: BASE_URL,
  timeout: API_TIMEOUT_MS,
  headers: {
    Accept: "application/json",
  },
});

export const privateAxios = axios.create({
  baseURL: BASE_URL,
  timeout: API_TIMEOUT_MS,
  headers: {
    Accept: "application/json",
  },
});

const handleApiError = (error) => Promise.reject(normalizeApiError(error));

const handlePrivateApiError = (error) => {
  const normalizedError = normalizeApiError(error);

  if (normalizedError.isUnauthorized) {
    doLogout();
    window.dispatchEvent(new Event("auth:expired"));
  }

  return Promise.reject(normalizedError);
};

myAxios.interceptors.response.use((response) => response, handleApiError);
privateAxios.interceptors.response.use((response) => response, handlePrivateApiError);

privateAxios.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);
