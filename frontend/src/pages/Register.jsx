import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, User, Phone, Users } from 'lucide-react';
import api from '../services/api';

export default function Register({ onShowToast }) {
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phoneNumber: ''
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});
    setLoading(true);

    try {
      await api.post('/auth/register', form);
      onShowToast('Registration successful! Please sign in.', false);
      navigate('/login');
    } catch (err) {
      if (err.response?.data?.errors) {
        setErrors(err.response.data.errors);
      } else {
        onShowToast(err.response?.data?.message || 'Registration failed', true);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card glass-panel">
        <div className="auth-header">
          <div style={{ display: 'inline-flex', padding: '12px', background: 'rgba(250, 204, 21, 0.08)', borderRadius: '12px', marginBottom: '15px' }}>
            <Users size={32} style={{ color: 'var(--primary)' }} />
          </div>
          <h1>Create <span>Account</span></h1>
          <p>Get started with your free contact manager</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
            <div className="form-group">
              <label className="form-label">First Name</label>
              <div style={{ position: 'relative' }}>
                <User size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: 'var(--text-secondary)' }} />
                <input
                  type="text"
                  className="form-input"
                  style={{ paddingLeft: '38px', width: '100%' }}
                  value={form.firstName}
                  onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                  required
                />
              </div>
              {errors.firstName && <span className="form-error">{errors.firstName}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Last Name</label>
              <div style={{ position: 'relative' }}>
                <User size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: 'var(--text-secondary)' }} />
                <input
                  type="text"
                  className="form-input"
                  style={{ paddingLeft: '38px', width: '100%' }}
                  value={form.lastName}
                  onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                  required
                />
              </div>
              {errors.lastName && <span className="form-error">{errors.lastName}</span>}
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Email Address</label>
            <div style={{ position: 'relative' }}>
              <Mail size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: 'var(--text-secondary)' }} />
              <input
                type="email"
                className="form-input"
                style={{ paddingLeft: '38px', width: '100%' }}
                placeholder="name@example.com"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                required
              />
            </div>
            {errors.email && <span className="form-error">{errors.email}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">Phone Number</label>
            <div style={{ position: 'relative' }}>
              <Phone size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: 'var(--text-secondary)' }} />
              <input
                type="text"
                className="form-input"
                style={{ paddingLeft: '38px', width: '100%' }}
                placeholder="+1234567890"
                value={form.phoneNumber}
                onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
              />
            </div>
            {errors.phoneNumber && <span className="form-error">{errors.phoneNumber}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: 'var(--text-secondary)' }} />
              <input
                type="password"
                className="form-input"
                style={{ paddingLeft: '38px', width: '100%' }}
                placeholder="Min 8 characters"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                required
              />
            </div>
            {errors.password && <span className="form-error">{errors.password}</span>}
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '10px' }} disabled={loading}>
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <div className="auth-footer">
          Already have an account? <Link to="/login" className="auth-link">Sign In</Link>
        </div>
      </div>
    </div>
  );
}
