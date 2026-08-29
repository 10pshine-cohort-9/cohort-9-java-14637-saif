import React, { useState, useEffect } from 'react';
import { X, Plus, Trash2 } from 'lucide-react';
import api from '../services/api';

export default function ContactModal({ isOpen, onClose, contactId, onSaveSuccess, onShowToast }) {
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    title: '',
    company: '',
    address: '',
    notes: '',
    favorite: false,
    email: '',
    phoneNumber: ''
  });

  const [emailsList, setEmailsList] = useState([]); // [{ label: '', value: '' }]
  const [phonesList, setPhonesList] = useState([]);  // [{ label: '', value: '' }]
  const [errors, setErrors] = useState({});

  useEffect(() => {
    let active = true;
    const controller = new AbortController();

    if (isOpen) {
      setErrors({});
      if (contactId) {
        fetchContact(contactId, controller.signal, () => active);
      } else {
        setForm({
          firstName: '',
          lastName: '',
          title: '',
          company: '',
          address: '',
          notes: '',
          favorite: false,
          email: '',
          phoneNumber: ''
        });
        setEmailsList([]);
        setPhonesList([]);
      }
    }

    return () => {
      active = false;
      controller.abort();
    };
  }, [isOpen, contactId]);

  const fetchContact = async (id, signal, isActive) => {
    try {
      const response = await api.get(`/contacts/${id}`, { signal });
      if (!isActive()) return;
      const data = response.data;
      setForm({
        firstName: data.firstName || '',
        lastName: data.lastName || '',
        title: data.title || '',
        company: data.company || '',
        address: data.address || '',
        notes: data.notes || '',
        favorite: data.favorite || false,
        email: data.email || '',
        phoneNumber: data.phoneNumber || ''
      });

      // Deserialize maps
      const emailsArray = Object.entries(data.emails || {}).map(([label, value]) => ({ label, value }));
      setEmailsList(emailsArray);

      const phonesArray = Object.entries(data.phoneNumbers || {}).map(([label, value]) => ({ label, value }));
      setPhonesList(phonesArray);
    } catch (err) {
      if (err.name === 'CanceledError' || err.name === 'AbortError' || err.code === 'ERR_CANCELED' || err.message === 'canceled') {
        return; // Suppress expected cancellation errors
      }
      if (!isActive()) return;
      onShowToast(err.response?.data?.message || 'Failed to fetch contact details', true);
      onClose();
    }
  };

  const handleAddEmail = () => {
    setEmailsList([...emailsList, { label: '', value: '' }]);
  };

  const handleRemoveEmail = (index) => {
    setEmailsList(emailsList.filter((_, i) => i !== index));
  };

  const handleEmailChange = (index, field, value) => {
    const updated = [...emailsList];
    updated[index] = { ...updated[index], [field]: value };
    setEmailsList(updated);
  };

  const handleAddPhone = () => {
    setPhonesList([...phonesList, { label: '', value: '' }]);
  };

  const handleRemovePhone = (index) => {
    setPhonesList(phonesList.filter((_, i) => i !== index));
  };

  const handlePhoneChange = (index, field, value) => {
    const updated = [...phonesList];
    updated[index] = { ...updated[index], [field]: value };
    setPhonesList(updated);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});

    // Validate labeled fields labels aren't empty
    const emailsMap = new Map();
    for (const item of emailsList) {
      const trimmedLabel = item.label.trim();
      if (!trimmedLabel) {
        setErrors({ emails: 'Email labels are required' });
        return;
      }
      if (emailsMap.has(trimmedLabel)) {
        setErrors({ emails: 'Duplicate email labels are not allowed' });
        return;
      }
      emailsMap.set(trimmedLabel, item.value.trim());
    }

    const phonesMap = new Map();
    for (const item of phonesList) {
      const trimmedLabel = item.label.trim();
      if (!trimmedLabel) {
        setErrors({ phoneNumbers: 'Phone labels are required' });
        return;
      }
      if (phonesMap.has(trimmedLabel)) {
        setErrors({ phoneNumbers: 'Duplicate phone labels are not allowed' });
        return;
      }
      phonesMap.set(trimmedLabel, item.value.trim());
    }

    const payload = {
      ...form,
      emails: Object.fromEntries(emailsMap),
      phoneNumbers: Object.fromEntries(phonesMap)
    };

    try {
      if (contactId) {
        await api.put(`/contacts/${contactId}`, payload);
        onShowToast('Contact updated successfully!', false);
      } else {
        await api.post('/contacts', payload);
        onShowToast('Contact created successfully!', false);
      }
      onSaveSuccess();
      onClose();
    } catch (err) {
      if (err.response?.data?.errors) {
        setErrors(err.response.data.errors);
      } else {
        onShowToast(err.response?.data?.message || 'Failed to save contact', true);
      }
    }
  };

  if (!isOpen) return null;

  const srOnlyStyle = {
    position: 'absolute',
    width: '1px',
    height: '1px',
    padding: 0,
    margin: '-1px',
    overflow: 'hidden',
    clip: 'rect(0, 0, 0, 0)',
    border: 0
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content glass-panel">
        <div className="modal-header">
          <h2 className="modal-title">{contactId ? 'Edit Contact' : 'Create Contact'}</h2>
          <button className="modal-close" onClick={onClose} aria-label="Close Modal">
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="firstName">First Name *</label>
              <input
                id="firstName"
                type="text"
                className="form-input"
                value={form.firstName}
                onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                required
              />
              {errors.firstName && <span className="form-error">{errors.firstName}</span>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="lastName">Last Name *</label>
              <input
                id="lastName"
                type="text"
                className="form-input"
                value={form.lastName}
                onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                required
              />
              {errors.lastName && <span className="form-error">{errors.lastName}</span>}
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '80px 1fr', gap: '15px' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="title">Title</label>
              <input
                id="title"
                type="text"
                className="form-input"
                placeholder="e.g. Mr."
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
              />
              {errors.title && <span className="form-error">{errors.title}</span>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="company">Company</label>
              <input
                id="company"
                type="text"
                className="form-input"
                placeholder="e.g. Tech Corp"
                value={form.company}
                onChange={(e) => setForm({ ...form, company: e.target.value })}
              />
              {errors.company && <span className="form-error">{errors.company}</span>}
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="email">Primary Email</label>
              <input
                id="email"
                type="email"
                className="form-input"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
              {errors.email && <span className="form-error">{errors.email}</span>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="phoneNumber">Primary Phone</label>
              <input
                id="phoneNumber"
                type="text"
                className="form-input"
                value={form.phoneNumber}
                onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
              />
              {errors.phoneNumber && <span className="form-error">{errors.phoneNumber}</span>}
            </div>
          </div>

          {/* Labeled Emails Section */}
          <div style={{ marginBottom: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
              <label className="form-label" style={{ margin: 0 }}>Additional Emails</label>
              <button type="button" className="btn btn-secondary btn-icon" onClick={handleAddEmail}>
                <Plus size={16} /> Add
              </button>
            </div>
            {errors.emails && <div className="form-error" style={{ marginBottom: '10px' }}>{errors.emails}</div>}
            {emailsList.map((item, idx) => (
              <div key={idx} className="map-field-row">
                <label htmlFor={`email-label-${idx}`} style={srOnlyStyle}>Email Label {idx + 1}</label>
                <input
                  id={`email-label-${idx}`}
                  type="text"
                  placeholder="Label (e.g. Work)"
                  className="form-input map-field-label"
                  value={item.label}
                  onChange={(e) => handleEmailChange(idx, 'label', e.target.value)}
                  required
                />
                <label htmlFor={`email-value-${idx}`} style={srOnlyStyle}>Email Address {idx + 1}</label>
                <input
                  id={`email-value-${idx}`}
                  type="email"
                  placeholder="Email Address"
                  className="form-input map-field-value"
                  value={item.value}
                  onChange={(e) => handleEmailChange(idx, 'value', e.target.value)}
                  required
                />
                <button type="button" className="btn btn-danger btn-icon" onClick={() => handleRemoveEmail(idx)} aria-label={`Remove Email ${idx + 1}`}>
                  <Trash2 size={16} />
                </button>
              </div>
            ))}
          </div>

          {/* Labeled Phone Numbers Section */}
          <div style={{ marginBottom: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
              <label className="form-label" style={{ margin: 0 }}>Additional Phones</label>
              <button type="button" className="btn btn-secondary btn-icon" onClick={handleAddPhone}>
                <Plus size={16} /> Add
              </button>
            </div>
            {errors.phoneNumbers && <div className="form-error" style={{ marginBottom: '10px' }}>{errors.phoneNumbers}</div>}
            {phonesList.map((item, idx) => (
              <div key={idx} className="map-field-row">
                <label htmlFor={`phone-label-${idx}`} style={srOnlyStyle}>Phone Label {idx + 1}</label>
                <input
                  id={`phone-label-${idx}`}
                  type="text"
                  placeholder="Label (e.g. Home)"
                  className="form-input map-field-label"
                  value={item.label}
                  onChange={(e) => handlePhoneChange(idx, 'label', e.target.value)}
                  required
                />
                <label htmlFor={`phone-value-${idx}`} style={srOnlyStyle}>Phone Number {idx + 1}</label>
                <input
                  id={`phone-value-${idx}`}
                  type="text"
                  placeholder="Phone Number"
                  className="form-input map-field-value"
                  value={item.value}
                  onChange={(e) => handlePhoneChange(idx, 'value', e.target.value)}
                  required
                />
                <button type="button" className="btn btn-danger btn-icon" onClick={() => handleRemovePhone(idx)} aria-label={`Remove Phone ${idx + 1}`}>
                  <Trash2 size={16} />
                </button>
              </div>
            ))}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="address">Address</label>
            <input
              id="address"
              type="text"
              className="form-input"
              value={form.address}
              onChange={(e) => setForm({ ...form, address: e.target.value })}
            />
            {errors.address && <span className="form-error">{errors.address}</span>}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="notes">Notes</label>
            <textarea
              id="notes"
              className="form-input"
              rows="3"
              style={{ resize: 'vertical' }}
              value={form.notes}
              onChange={(e) => setForm({ ...form, notes: e.target.value })}
            />
            {errors.notes && <span className="form-error">{errors.notes}</span>}
          </div>

          <div className="form-group" style={{ flexDirection: 'row', alignItems: 'center', gap: '10px' }}>
            <input
              type="checkbox"
              id="favorite"
              style={{ width: '18px', height: '18px', cursor: 'pointer' }}
              checked={form.favorite}
              onChange={(e) => setForm({ ...form, favorite: e.target.checked })}
            />
            <label htmlFor="favorite" className="form-label" style={{ margin: 0, cursor: 'pointer' }}>Mark as Favorite</label>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '20px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              {contactId ? 'Update Contact' : 'Save Contact'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
