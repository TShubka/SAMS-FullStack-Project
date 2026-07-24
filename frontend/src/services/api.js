import axios from 'axios';
import { STORAGE_KEYS } from '../utils/constants';

/**
 * The single Axios instance every service uses. Owner: Member 1.
 *
 * Two interceptors carry the authentication concern so that no component ever
 * has to think about the token:
 *
 *  - request:  attaches the JWT as an Authorization: Bearer header
 *  - response: on 401 clears the session and sends the user back to /login
 *
 * This is why components can simply call studentService.getAll() and stay
 * unaware that authentication exists at all.
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;

    if (status === 401) {
      // The token is missing, expired or invalid. Anything cached is now stale.
      localStorage.removeItem(STORAGE_KEYS.TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER);
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }

    return Promise.reject(error);
  },
);

/**
 * Turns any Axios failure into a readable string.
 *
 * The backend's GlobalExceptionHandler always returns the same ErrorResponse
 * shape, so validation errors (which carry a fieldErrors map) can be flattened
 * here instead of in every form.
 */
export function extractErrorMessage(error, fallback = 'Something went wrong') {
  const data = error.response?.data;
  if (!data) {
    return error.request ? 'Cannot reach the server. Is the backend running?' : fallback;
  }
  if (data.fieldErrors && Object.keys(data.fieldErrors).length > 0) {
    return Object.entries(data.fieldErrors)
      .map(([field, message]) => `${field}: ${message}`)
      .join(', ');
  }
  return data.message || fallback;
}

export default api;
