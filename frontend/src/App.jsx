import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';

const memoryStorage = {};

export const safeStorage = {
  getItem(key) {
    try {
      return localStorage.getItem(key);
    } catch (e) {
      console.warn(`localStorage.getItem failed for key "${key}":`, e);
      return memoryStorage[key] || null;
    }
  },
  setItem(key, value) {
    try {
      localStorage.setItem(key, value);
    } catch (e) {
      console.warn(`localStorage.setItem failed for key "${key}":`, e);
      memoryStorage[key] = value;
    }
  },
  removeItem(key) {
    try {
      localStorage.removeItem(key);
    } catch (e) {
      console.warn(`localStorage.removeItem failed for key "${key}":`, e);
      delete memoryStorage[key];
    }
  }
};

// The access token itself lives only in an HttpOnly cookie (inaccessible to JS); this flag is
// just a UI hint for routing. The backend still enforces auth on every request via the cookie.
function ProtectedRoute({ children }) {
  const isAuthenticated = safeStorage.getItem('isAuthenticated');
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

function PublicRoute({ children }) {
  const isAuthenticated = safeStorage.getItem('isAuthenticated');
  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }
  return children;
}

export default function App() {
  const [toast, setToast] = useState(null);
  const [theme, setTheme] = useState(safeStorage.getItem('theme') || 'dark');

  const showToast = (message, isError = false) => {
    setToast({ message, isError });
  };

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => {
        setToast(null);
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    safeStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'));
  };

  return (
    <Router>
      <div style={{ minHeight: '100vh', position: 'relative' }}>
        <Routes>
          <Route
            path="/login"
            element={
              <PublicRoute>
                <Login onShowToast={showToast} />
              </PublicRoute>
            }
          />
          <Route
            path="/register"
            element={
              <PublicRoute>
                <Register onShowToast={showToast} />
              </PublicRoute>
            }
          />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Dashboard onShowToast={showToast} theme={theme} toggleTheme={toggleTheme} />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>

        {/* Global Toast Notification */}
        {toast && (
          <div className={`toast-notification ${toast.isError ? 'toast-error' : ''}`}>
            <span>{toast.message}</span>
          </div>
        )}
      </div>
    </Router>
  );
}
