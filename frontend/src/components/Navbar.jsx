import React from 'react';
import { Users, User, LogOut } from 'lucide-react';

export default function Navbar({ onOpenProfile, userEmail }) {
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    window.location.href = '/login';
  };

  return (
    <nav className="navbar glass-panel" style={{ borderRadius: 0, borderTop: 'none', borderLeft: 'none', borderRight: 'none' }}>
      <div className="logo">
        <Users size={28} style={{ color: 'var(--primary-hover)' }} />
        <span>ContactHub</span>
      </div>
      <div className="nav-user">
        <span className="nav-username">{userEmail || 'User'}</span>
        <button className="btn btn-secondary btn-icon" onClick={onOpenProfile} title="My Profile">
          <User size={18} />
        </button>
        <button className="btn btn-danger btn-icon" onClick={handleLogout} title="Log Out">
          <LogOut size={18} />
        </button>
      </div>
    </nav>
  );
}
