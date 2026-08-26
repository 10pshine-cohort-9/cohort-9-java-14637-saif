import React, { useState, useEffect } from 'react';
import { X, Lock, User as UserIcon } from 'lucide-react';
import api from '../services/api';

export default function ProfileModal({ isOpen, onClose, onShowToast }) {
  const [profile, setProfile] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: ''
  });

  const [passwordForm, setPasswordForm] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  const [activeTab, setActiveTab] = useState('details'); // 'details' or 'password'
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (isOpen) {
      fetchProfile();
      setPasswordForm({ oldPassword: '', newPassword: '', confirmPassword: '' });
      setErrors({});
    }
  }, [isOpen]);

  const fetchProfile = async () => {
    try {
      const response = await api.get('/users/profile');
      setProfile(response.data);
    } catch (err) {
      onShowToast(err.response?.data?.message || 'Failed to fetch profile', true);
    }
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setErrors({});
    try {
      const response = await api.put('/users/profile', {
        firstName: profile.firstName,
        lastName: profile.lastName,
        phoneNumber: profile.phoneNumber
      });
      setProfile(response.data);
      onShowToast('Profile updated successfully!', false);
      onClose();
    } catch (err) {
      if (err.response?.data?.errors) {
        setErrors(err.response.data.errors);
      } else {
        onShowToast(err.response?.data?.message || 'Failed to update profile', true);
      }
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setErrors({});

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setErrors({ confirmPassword: 'Passwords do not match' });
      return;
    }

    try {
      await api.post('/users/change-password', {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      });
      onShowToast('Password changed successfully! Please log in again.', false);
      // Wait a moment so the toast is visible, then logout
      setTimeout(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('email');
        window.location.href = '/login';
      }, 1500);
    } catch (err) {
      if (err.response?.data?.errors) {
        setErrors(err.response.data.errors);
      } else {
        onShowToast(err.response?.data?.message || err.response?.data?.error || 'Failed to change password', true);
      }
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content glass-panel">
        <div className="modal-header">
          <h2 className="modal-title">My Profile</h2>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        {/* Tab Selection */}
        <div className="modal-tabs">
          <button
            className={`modal-tab-btn ${activeTab === 'details' ? 'active' : ''}`}
            onClick={() => setActiveTab('details')}
            type="button"
          >
            <UserIcon size={16} /> Profile Details
          </button>
          <button
            className={`modal-tab-btn ${activeTab === 'password' ? 'active' : ''}`}
            onClick={() => setActiveTab('password')}
            type="button"
          >
            <Lock size={16} /> Change Password
          </button>
        </div>

        {activeTab === 'details' ? (
          <form onSubmit={handleProfileSubmit}>
            <div className="form-group">
              <label className="form-label">Email Address (Read Only)</label>
              <input
                type="text"
                className="form-input"
                value={profile.email}
                disabled
                style={{ opacity: 0.6, cursor: 'not-allowed' }}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
              <div className="form-group">
                <label className="form-label">First Name</label>
                <input
                  type="text"
                  className="form-input"
                  value={profile.firstName}
                  onChange={(e) => setProfile({ ...profile, firstName: e.target.value })}
                  required
                />
                {errors.firstName && <span className="form-error">{errors.firstName}</span>}
              </div>

              <div className="form-group">
                <label className="form-label">Last Name</label>
                <input
                  type="text"
                  className="form-input"
                  value={profile.lastName}
                  onChange={(e) => setProfile({ ...profile, lastName: e.target.value })}
                  required
                />
                {errors.lastName && <span className="form-error">{errors.lastName}</span>}
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Phone Number</label>
              <input
                type="text"
                className="form-input"
                value={profile.phoneNumber || ''}
                onChange={(e) => setProfile({ ...profile, phoneNumber: e.target.value })}
              />
              {errors.phoneNumber && <span className="form-error">{errors.phoneNumber}</span>}
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '20px' }}>
              <button type="button" className="btn btn-secondary" onClick={onClose}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary">
                Save Profile
              </button>
            </div>
          </form>
        ) : (
          <form onSubmit={handlePasswordSubmit}>
            <div className="form-group">
              <label className="form-label">Old Password</label>
              <input
                type="password"
                className="form-input"
                value={passwordForm.oldPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, oldPassword: e.target.value })}
                required
              />
              {errors.oldPassword && <span className="form-error">{errors.oldPassword}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">New Password</label>
              <input
                type="password"
                className="form-input"
                value={passwordForm.newPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                required
              />
              {errors.newPassword && <span className="form-error">{errors.newPassword}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Confirm New Password</label>
              <input
                type="password"
                className="form-input"
                value={passwordForm.confirmPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                required
              />
              {errors.confirmPassword && <span className="form-error">{errors.confirmPassword}</span>}
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '20px' }}>
              <button type="button" className="btn btn-secondary" onClick={onClose}>
                Cancel
              </button>
              <button type="submit" className="btn btn-danger">
                Update Password
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
