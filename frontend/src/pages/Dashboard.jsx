import React, { useState, useEffect, useRef } from 'react';
import { Search, Plus, Upload, Download, Trash2, Edit, ChevronLeft, ChevronRight, UserPlus, Star, Users, User, LogOut, Sun, Moon, Mail, Phone, MapPin } from 'lucide-react';
import api from '../services/api';
import ContactModal from '../components/ContactModal';
import ProfileModal from '../components/ProfileModal';

const AVATAR_GRADIENTS = [
  'linear-gradient(135deg, #fbbf24 0%, #d97706 100%)', // Amber/Yellow
  'linear-gradient(135deg, #60a5fa 0%, #2563eb 100%)', // Blue
  'linear-gradient(135deg, #34d399 0%, #059669 100%)', // Green
  'linear-gradient(135deg, #f472b6 0%, #db2777 100%)', // Pink
  'linear-gradient(135deg, #a78bfa 0%, #7c3aed 100%)', // Purple
  'linear-gradient(135deg, #fb7185 0%, #e11d48 100%)'  // Rose
];

const getAvatarStyle = (firstName = '', lastName = '') => {
  const fullName = `${firstName} ${lastName}`.trim();
  const code = fullName.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
  const gradient = AVATAR_GRADIENTS[code % AVATAR_GRADIENTS.length];
  return { background: gradient };
};

export default function Dashboard({ onShowToast, theme, toggleTheme }) {
  const [contacts, setContacts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState('firstName');
  const [sortDir, setSortDir] = useState('asc');

  // Modals state
  const [isContactOpen, setIsContactOpen] = useState(false);
  const [selectedContactId, setSelectedContactId] = useState(null);
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  const fileInputRef = useRef(null);
  const userEmail = localStorage.getItem('email') || 'User';

  useEffect(() => {
    fetchContacts();
  }, [page, sortBy, sortDir]);

  const fetchContacts = async (query = searchQuery) => {
    try {
      let response;
      const params = {
        page,
        size: pageSize,
        sortBy,
        direction: sortDir
      };

      if (query.trim()) {
        response = await api.get('/contacts/search', {
          params: { ...params, keyword: query.trim() }
        });
      } else {
        response = await api.get('/contacts', { params });
      }

      setContacts(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
      setTotalElements(response.data.totalElements || 0);
    } catch (err) {
      onShowToast(err.response?.data?.message || 'Failed to fetch contacts', true);
    }
  };

  const handleSearchChange = (e) => {
    const val = e.target.value;
    setSearchQuery(val);
    setPage(0);
    fetchContacts(val);
  };

  const handleDelete = async (e, id) => {
    e.stopPropagation();
    if (window.confirm('Are you sure you want to delete this contact?')) {
      try {
        await api.delete(`/contacts/${id}`);
        onShowToast('Contact deleted successfully!', false);
        fetchContacts();
      } catch (err) {
        onShowToast(err.response?.data?.message || 'Failed to delete contact', true);
      }
    }
  };

  const handleToggleFavorite = async (e, contact) => {
    e.stopPropagation();
    try {
      await api.put(`/contacts/${contact.id}`, {
        firstName: contact.firstName,
        lastName: contact.lastName,
        title: contact.title,
        company: contact.company,
        address: contact.address,
        notes: contact.notes,
        email: contact.email,
        phoneNumber: contact.phoneNumber,
        favorite: !contact.favorite,
        emails: contact.emails,
        phoneNumbers: contact.phoneNumbers
      });
      fetchContacts();
    } catch (err) {
      onShowToast(err.response?.data?.message || 'Failed to update favorite status', true);
    }
  };

  const handleExport = async () => {
    try {
      const response = await api.get('/contacts/export', { responseType: 'blob' });
      const blob = new Blob([response.data], { type: 'text/csv' });
      const link = document.createElement('a');
      link.href = window.URL.createObjectURL(blob);
      link.download = 'contacts.csv';
      link.click();
      onShowToast('Contacts exported successfully!', false);
    } catch (err) {
      onShowToast('Failed to export contacts', true);
    }
  };

  const handleImportClick = () => {
    fileInputRef.current.click();
  };

  const handleImportFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await api.post('/contacts/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      onShowToast(`Successfully imported ${response.data.importedCount} contacts!`, false);
      fetchContacts();
    } catch (err) {
      onShowToast(err.response?.data?.message || 'Failed to import contacts. Please verify CSV format.', true);
    } finally {
      e.target.value = '';
    }
  };

  const openCreateModal = () => {
    setSelectedContactId(null);
    setIsContactOpen(true);
  };

  const openEditModal = (id) => {
    setSelectedContactId(id);
    setIsContactOpen(true);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    window.location.href = '/login';
  };

  return (
    <div className="dashboard-container">
      {/* Premium Sidebar Layout */}
      <aside className="sidebar">
        <div className="sidebar-logo">
          <Users size={24} />
          <span>Contact<span>Hub</span></span>
        </div>

        <div className="sidebar-menu">
          <div className="sidebar-item active">
            <Users size={18} />
            <span>All Contacts ({totalElements})</span>
          </div>
        </div>

        <div className="sidebar-footer">
          <div className="sidebar-user">
            <div className="sidebar-user-avatar">
              {userEmail.charAt(0).toUpperCase()}
            </div>
            <div className="sidebar-user-email" title={userEmail}>
              {userEmail}
            </div>
          </div>

          <div style={{ display: 'flex', gap: '8px' }}>
            <button
              className="btn btn-secondary btn-icon"
              style={{ flex: 1 }}
              onClick={toggleTheme}
              title={theme === 'dark' ? 'Light Mode' : 'Dark Mode'}
            >
              {theme === 'dark' ? <Sun size={16} /> : <Moon size={16} />}
            </button>
            <button
              className="btn btn-secondary btn-icon"
              style={{ flex: 1 }}
              onClick={() => setIsProfileOpen(true)}
              title="My Profile"
            >
              <User size={16} />
            </button>
            <button
              className="btn btn-danger btn-icon"
              style={{ flex: 1 }}
              onClick={handleLogout}
              title="Log Out"
            >
              <LogOut size={16} />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Panel */}
      <main className="dashboard-main">
        <div className="dashboard-header">
          <div>
            <h2>My <span>Contacts</span></h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '4px' }}>
              Manage, search, and import/export your address book
            </p>
          </div>

          <div className="action-buttons">
            <input
              type="file"
              ref={fileInputRef}
              style={{ display: 'none' }}
              accept=".csv"
              onChange={handleImportFileChange}
            />
            <button className="btn btn-secondary" onClick={handleImportClick}>
              <Upload size={16} /> Import
            </button>
            <button className="btn btn-secondary" onClick={handleExport}>
              <Download size={16} /> Export
            </button>
            <button className="btn btn-primary" onClick={openCreateModal}>
              <Plus size={16} /> Add Contact
            </button>
          </div>
        </div>

        {/* Search Panel */}
        <div style={{ display: 'flex', gap: '16px', marginBottom: '24px' }}>
          <div className="search-bar-container">
            <Search className="search-icon" size={16} />
            <input
              type="text"
              className="search-input"
              placeholder="Search by name..."
              value={searchQuery}
              onChange={handleSearchChange}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>SORT:</span>
            <select
              value={sortBy}
              onChange={(e) => { setSortBy(e.target.value); setPage(0); }}
              className="form-input"
              style={{ padding: '6px 12px', fontSize: '0.85rem', cursor: 'pointer' }}
            >
              <option value="firstName">First Name</option>
              <option value="lastName">Last Name</option>
              <option value="company">Company</option>
            </select>
            <button
              className="btn btn-secondary btn-icon"
              style={{ padding: '6px 10px' }}
              onClick={() => { setSortDir(sortDir === 'asc' ? 'desc' : 'asc'); setPage(0); }}
            >
              {sortDir === 'asc' ? 'ASC' : 'DESC'}
            </button>
          </div>
        </div>

        {/* Contacts Cards Grid */}
        {contacts.length === 0 ? (
          <div className="glass-panel" style={{ padding: '40px' }}>
            <div className="empty-state">
              <UserPlus className="empty-state-icon" size={48} />
              <h3>No Contacts Found</h3>
              <p>Get started by creating a new contact or importing from a CSV file.</p>
            </div>
          </div>
        ) : (
          <>
            <div className="contacts-grid">
              {contacts.map((contact) => (
                <div
                  key={contact.id}
                  className="glass-panel contact-card"
                  onClick={() => openEditModal(contact.id)}
                  style={{ cursor: 'pointer' }}
                >
                  <div className="contact-card-header">
                    <div
                      className="contact-card-avatar"
                      style={getAvatarStyle(contact.firstName, contact.lastName)}
                    >
                      {contact.firstName ? contact.firstName.charAt(0).toUpperCase() : ''}
                    </div>
                    <div className="contact-card-actions">
                      <button
                        className="btn btn-secondary btn-icon"
                        onClick={(e) => { e.stopPropagation(); openEditModal(contact.id); }}
                        title="Edit"
                        style={{ padding: '6px' }}
                      >
                        <Edit size={14} />
                      </button>
                      <button
                        className="btn btn-danger btn-icon"
                        onClick={(e) => handleDelete(e, contact.id)}
                        title="Delete"
                        style={{ padding: '6px' }}
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </div>

                  <div className="contact-card-body">
                    <div className="contact-card-name">
                      {contact.title ? `${contact.title} ` : ''}{contact.firstName} {contact.lastName}
                    </div>
                    {contact.company && (
                      <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '8px', fontWeight: 500 }}>
                        {contact.company}
                      </div>
                    )}
                    
                    {contact.email && (
                      <div className="contact-card-meta">
                        <Mail size={14} />
                        <span>{contact.email}</span>
                      </div>
                    )}
                    
                    {contact.phoneNumber && (
                      <div className="contact-card-meta">
                        <Phone size={14} />
                        <span>{contact.phoneNumber}</span>
                      </div>
                    )}

                    {contact.address && (
                      <div className="contact-card-meta">
                        <MapPin size={14} />
                        <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {contact.address}
                        </span>
                      </div>
                    )}

                    <div className="contact-card-labels">
                      <button
                        onClick={(e) => handleToggleFavorite(e, contact)}
                        style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
                      >
                        <span className={`badge ${contact.favorite ? 'badge-favorite' : 'badge-label'}`} style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                          <Star size={10} fill={contact.favorite ? 'var(--primary)' : 'none'} />
                          {contact.favorite ? 'Favorite' : 'Mark Fav'}
                        </span>
                      </button>

                      {Object.keys(contact.emails || {}).map((label) => (
                        <span key={label} className="badge badge-label">
                          {label}
                        </span>
                      ))}

                      {Object.keys(contact.phoneNumbers || {}).map((label) => (
                        <span key={label} className="badge badge-label">
                          {label}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Pagination */}
            <div className="pagination-container">
              <span className="pagination-text">
                Showing <strong>{page * pageSize + 1}</strong> - <strong>{Math.min((page + 1) * pageSize, totalElements)}</strong> of <strong>{totalElements}</strong> contacts
              </span>
              <div className="pagination-buttons">
                <button
                  className="btn btn-secondary btn-icon"
                  disabled={page === 0}
                  onClick={() => setPage(page - 1)}
                >
                  <ChevronLeft size={16} />
                </button>
                <button
                  className="btn btn-secondary btn-icon"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage(page + 1)}
                >
                  <ChevronRight size={16} />
                </button>
              </div>
            </div>
          </>
        )}
      </main>

      <ContactModal
        isOpen={isContactOpen}
        onClose={() => setIsContactOpen(false)}
        contactId={selectedContactId}
        onSaveSuccess={fetchContacts}
        onShowToast={onShowToast}
      />

      <ProfileModal
        isOpen={isProfileOpen}
        onClose={() => setIsProfileOpen(false)}
        onShowToast={onShowToast}
      />
    </div>
  );
}
