import React from 'react';
import { Users, User, LogOut, Sun, Moon } from 'lucide-react';
import { logout } from '../services/api';

export default function Navbar({ onOpenProfile, userEmail, theme, toggleTheme }) {
  const handleLogout = () => {
    logout();
  };

  return (
    <nav className="navbar glass-panel" style={{ borderRadius: 0, borderTop: 'none', borderLeft: 'none', borderRight: 'none' }}>
      <div className="logo">
        <Users size={24} style={{ color: 'var(--primary)' }} />
        <span>Contact<span>Hub</span></span>
      </div>
      <div className="nav-user">
        <span className="nav-username">{userEmail || 'User'}</span>
        <button className="btn btn-secondary btn-icon" onClick={toggleTheme} title={theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'}>
          {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
        </button>
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
