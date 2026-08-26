import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';

function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

function PublicRoute({ children }) {
  const token = localStorage.getItem('token');
  if (token) {
    return <Navigate to="/" replace />;
  }
  return children;
}

export default function App() {
  const [toast, setToast] = useState(null);
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'dark');

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
    localStorage.setItem('theme', theme);
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
