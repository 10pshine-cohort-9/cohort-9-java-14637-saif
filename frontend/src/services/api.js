import axios from 'axios';

export const EMAIL_KEY = 'email';
export const AUTH_FLAG_KEY = 'isAuthenticated';

export const logout = async () => {
  try {
    await api.post('/auth/logout');
  } catch (e) {
    // Best-effort: proceed with local cleanup even if the request fails.
  }
  localStorage.removeItem(EMAIL_KEY);
  localStorage.removeItem(AUTH_FLAG_KEY);
  window.location.href = '/login';
};

const api = axios.create({
  baseURL: 'http://localhost:3030/api',
  // Send the HttpOnly access-token cookie and read/echo the XSRF-TOKEN cookie on every request.
  withCredentials: true,
  withXSRFToken: true,
});

// Response interceptor to handle token expiration (401 Unauthorized)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const url = error.config && error.config.url ? String(error.config.url).toLowerCase() : '';
      const isAuthRequest = url && (url.includes('/auth/login') || url.includes('/auth/register'));
      if (!isAuthRequest) {
        localStorage.removeItem(EMAIL_KEY);
        localStorage.removeItem(AUTH_FLAG_KEY);
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
