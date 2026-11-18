const AUTH_STORAGE_KEY = "data";

const getStoredAuthData = () => {
  const rawData = localStorage.getItem(AUTH_STORAGE_KEY);
  if (!rawData) {
    return null;
  }

  try {
    return JSON.parse(rawData);
  } catch (error) {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    return null;
  }
};

export const isLoggedIn = () => getStoredAuthData() != null;

export const doLogin = (data, next) => {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(data));
  if (typeof next === "function") {
    next();
  }
};

export const doLogout = (next) => {
  localStorage.removeItem(AUTH_STORAGE_KEY);
  if (typeof next === "function") {
    next();
  }
};

export const getCurrentUserDetail = () => getStoredAuthData()?.user;

export const getToken = () => getStoredAuthData()?.token ?? null;
