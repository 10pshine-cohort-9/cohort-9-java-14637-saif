import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:3030/api',
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle token expiration (401 Unauthorized)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const url = error.config && error.config.url ? String(error.config.url).toLowerCase() : '';
      const isAuthRequest = url && (url.includes('/auth/login') || url.includes('/auth/register'));
      if (!isAuthRequest) {
        localStorage.removeItem('token');
        localStorage.removeItem('email');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
