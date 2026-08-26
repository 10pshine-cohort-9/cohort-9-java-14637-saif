import React, { useState, useEffect, useRef } from 'react';
import { Search, Plus, Upload, Download, Trash2, Edit, ChevronLeft, ChevronRight, UserPlus, Star, ChevronDown, ChevronUp } from 'lucide-react';
import api from '../services/api';
import Navbar from '../components/Navbar';
import ContactModal from '../components/ContactModal';
import ProfileModal from '../components/ProfileModal';

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
    // Fetch immediately or debounce. Let's fetch immediately for responsive search experience
    fetchContacts(val);
  };

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortDir('asc');
    }
    setPage(0);
  };

  const handleDelete = async (id) => {
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

  const handleToggleFavorite = async (contact) => {
    try {
      const updated = { ...contact, favorite: !contact.favorite };
      // Strip Hibernate unneeded properties if needed, but our PUT matches entity structure
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
      // Clear file input
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

  const getSortIcon = (field) => {
    if (sortBy !== field) return null;
    return sortDir === 'asc' ? <ChevronUp size={16} /> : <ChevronDown size={16} />;
  };

  return (
    <div className="dashboard-layout">
      <Navbar onOpenProfile={() => setIsProfileOpen(true)} userEmail={userEmail} theme={theme} toggleTheme={toggleTheme} />

      <main className="main-content">
        <div className="panel-header">
          <div className="search-bar-container">
            <Search className="search-icon" size={18} />
            <input
              type="text"
              className="search-input"
              placeholder="Search by first name or last name..."
              value={searchQuery}
              onChange={handleSearchChange}
            />
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
              <Upload size={18} /> Import CSV
            </button>
            <button className="btn btn-secondary" onClick={handleExport}>
              <Download size={18} /> Export CSV
            </button>
            <button className="btn btn-primary" onClick={openCreateModal}>
              <Plus size={18} /> New Contact
            </button>
          </div>
        </div>

        <div className="glass-panel" style={{ padding: '24px' }}>
          {contacts.length === 0 ? (
            <div className="empty-state">
              <UserPlus className="empty-state-icon" size={48} />
              <h3>No Contacts Found</h3>
              <p>Get started by creating a new contact or importing from a CSV file.</p>
            </div>
          ) : (
            <>
              <div className="contacts-table-container">
                <table className="contacts-table">
                  <thead>
                    <tr>
                      <th onClick={() => handleSort('firstName')}>Name {getSortIcon('firstName')}</th>
                      <th onClick={() => handleSort('title')}>Title {getSortIcon('title')}</th>
                      <th onClick={() => handleSort('email')}>Email {getSortIcon('email')}</th>
                      <th onClick={() => handleSort('phoneNumber')}>Phone {getSortIcon('phoneNumber')}</th>
                      <th onClick={() => handleSort('company')}>Company {getSortIcon('company')}</th>
                      <th>Labels</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {contacts.map((contact) => (
                      <tr key={contact.id}>
                        <td>
                          <div className="contact-name-cell">
                            <button
                              onClick={() => handleToggleFavorite(contact)}
                              style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
                            >
                              <Star
                                size={18}
                                fill={contact.favorite ? '#facc15' : 'none'}
                                color={contact.favorite ? '#facc15' : 'var(--text-secondary)'}
                              />
                            </button>
                            <div className="contact-avatar">
                              {contact.firstName ? contact.firstName.charAt(0).toUpperCase() : ''}
                            </div>
                            <div>
                              <div style={{ fontWeight: 600 }}>{contact.firstName} {contact.lastName}</div>
                              {contact.favorite && <span className="badge badge-favorite">Favorite</span>}
                            </div>
                          </div>
                        </td>
                        <td>{contact.title || '-'}</td>
                        <td>{contact.email || '-'}</td>
                        <td>{contact.phoneNumber || '-'}</td>
                        <td>{contact.company || '-'}</td>
                        <td>
                          {/* Render labeled emails */}
                          {Object.entries(contact.emails || {}).map(([label, val]) => (
                            <span key={label} className="badge badge-label" title={val}>
                              {label}: email
                            </span>
                          ))}
                          {/* Render labeled phones */}
                          {Object.entries(contact.phoneNumbers || {}).map(([label, val]) => (
                            <span key={label} className="badge badge-label" title={val}>
                              {label}: phone
                            </span>
                          ))}
                          {Object.keys(contact.emails || {}).length === 0 && Object.keys(contact.phoneNumbers || {}).length === 0 && '-'}
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: '8px' }}>
                            <button className="btn btn-secondary btn-icon" onClick={() => openEditModal(contact.id)} title="Edit">
                              <Edit size={16} />
                            </button>
                            <button className="btn btn-danger btn-icon" onClick={() => handleDelete(contact.id)} title="Delete">
                              <Trash2 size={16} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination controls */}
              <div className="pagination-container">
                <span className="pagination-text">
                  Showing {page * pageSize + 1} - {Math.min((page + 1) * pageSize, totalElements)} of {totalElements} contacts
                </span>
                <div className="pagination-buttons">
                  <button
                    className="btn btn-secondary btn-icon"
                    disabled={page === 0}
                    onClick={() => setPage(page - 1)}
                  >
                    <ChevronLeft size={18} />
                  </button>
                  <button
                    className="btn btn-secondary btn-icon"
                    disabled={page >= totalPages - 1}
                    onClick={() => setPage(page + 1)}
                  >
                    <ChevronRight size={18} />
                  </button>
                </div>
              </div>
            </>
          )}
        </div>
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
