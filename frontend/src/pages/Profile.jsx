import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './Profile.css';

const sameName = (a, b) => (a || '').trim().toLowerCase() === (b || '').trim().toLowerCase();

const Profile = () => {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [businesses, setBusinesses] = useState([]);
  const [form, setForm] = useState({ fullName: '', phoneNumber: '' });
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [loadError, setLoadError] = useState(false);
  const [deleting, setDeleting] = useState(null);
  const [confirmName, setConfirmName] = useState('');

  useEffect(() => {
    const load = async () => {
      const [pRes, bRes] = await Promise.allSettled([
        api.get('/api/profile'),
        api.get('/api/businesses/owner'),
      ]);
      if (pRes.status === 'fulfilled') {
        setProfile(pRes.value.data);
        setForm({
          fullName: pRes.value.data.fullName || '',
          phoneNumber: pRes.value.data.phoneNumber || '',
        });
      } else {
        console.error('Failed to load profile:', pRes.reason);
        setLoadError(true);
      }
      if (bRes.status === 'fulfilled' && Array.isArray(bRes.value.data)) setBusinesses(bRes.value.data);
    };
    load();
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage({ type: '', text: '' });
    try {
      const res = await api.put('/api/profile', form);
      setProfile(res.data);
      setEditing(false);
      setMessage({ type: 'success', text: 'Profile updated.' });
    } catch (err) {
      console.error('Profile update failed:', err);
      setMessage({ type: 'error', text: 'Could not save. Check your details and try again.' });
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    setForm({ fullName: profile?.fullName || '', phoneNumber: profile?.phoneNumber || '' });
    setEditing(false);
    setMessage({ type: '', text: '' });
  };

  const openDelete = (b) => {
    setDeleting(b);
    setConfirmName('');
  };

  // Optimistic delete: remove from screen now, restore only if the server fails.
  const handleDelete = async () => {
    if (!deleting || !sameName(confirmName, deleting.name)) return;
    const target = deleting;
    const previous = businesses;

    setBusinesses(businesses.filter((b) => b.id !== target.id));
    setDeleting(null);
    setConfirmName('');
    setMessage({ type: 'success', text: `${target.name} deleted.` });

    try {
      await api.delete(`/api/businesses/${target.id}`, { timeout: 15000 });
    } catch (err) {
      console.error('Delete failed:', err);
      setBusinesses(previous);
      setMessage({ type: 'error', text: `Could not delete ${target.name}. Please try again.` });
    }
  };

  const handleSignOut = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const initials = (profile?.fullName || '?')
    .split(' ').filter(Boolean).slice(0, 2).map((n) => n[0].toUpperCase()).join('');

  return (
    <div className="pf-layout">
      <Navbar />
      <main className="pf-container">
        <header className="pf-header">
          <span className="pf-tag">Account</span>
          <h1>Your Profile</h1>
        </header>

        {loadError && <div className="pf-card pf-error">Unable to load your profile. Try signing in again.</div>}

        {profile && (
          <>
            <section className="pf-card pf-identity">
              <div className="pf-avatar">{initials}</div>
              <div>
                <h2>{profile.fullName}</h2>
                <p>{profile.email}</p>
              </div>
            </section>

            <section className="pf-card">
              <div className="pf-card-header">
                <h3>Personal Details</h3>
                {!editing && <button className="pf-btn-outline" onClick={() => setEditing(true)}>Edit</button>}
              </div>

              {editing ? (
                <form className="pf-form" onSubmit={handleSave}>
                  <div className="pf-group">
                    <label htmlFor="fullName">Full Name</label>
                    <input id="fullName" value={form.fullName}
                      onChange={(e) => setForm({ ...form, fullName: e.target.value })} required />
                  </div>
                  <div className="pf-group">
                    <label htmlFor="phone">Phone Number</label>
                    <input id="phone" value={form.phoneNumber} placeholder="Optional"
                      onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} />
                  </div>
                  <div className="pf-group">
                    <label>Email Address</label>
                    <input value={profile.email} disabled />
                    <small>Contact support to change your email.</small>
                  </div>
                  <div className="pf-actions">
                    <button type="submit" className="pf-btn-primary" disabled={saving}>
                      {saving ? 'Saving...' : 'Save Changes'}
                    </button>
                    <button type="button" className="pf-btn-outline" onClick={handleCancel}>Cancel</button>
                  </div>
                </form>
              ) : (
                <dl className="pf-details">
                  <div><dt>Full Name</dt><dd>{profile.fullName}</dd></div>
                  <div><dt>Email</dt><dd>{profile.email}</dd></div>
                  <div><dt>Phone</dt><dd>{profile.phoneNumber || 'Not added'}</dd></div>
                </dl>
              )}
            </section>

            <section className="pf-card">
              <div className="pf-card-header">
                <h3>Your Businesses</h3>
                <button className="pf-btn-outline" onClick={() => navigate('/business-setup')}>+ Add</button>
              </div>
              {businesses.length === 0 ? (
                <p className="pf-muted">No business set up yet.</p>
              ) : (
                businesses.map((b) => (
                  <div className="pf-biz-row" key={b.id}>
                    <span>{b.name}</span>
                    <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                      <span className="pf-muted">{b.currency || 'ZAR'}</span>
                      <button className="pf-btn-danger" style={{ marginLeft: 0, padding: '6px 12px' }}
                        onClick={() => openDelete(b)}>
                        Delete
                      </button>
                    </div>
                  </div>
                ))
              )}
            </section>

            {message.text && <p className={`pf-message ${message.type}`}>{message.text}</p>}

            <section className="pf-card pf-footer-actions">
              <button className="pf-btn-outline" onClick={() => navigate('/help')}>Help & FAQ</button>
              <button className="pf-btn-outline" onClick={() => navigate('/contact')}>Contact Support</button>
              <button className="pf-btn-danger" onClick={handleSignOut}>Sign Out</button>
            </section>
          </>
        )}

        {deleting && (
          <div className="pf-modal-backdrop" onClick={() => setDeleting(null)}>
            <div className="pf-card pf-modal" onClick={(e) => e.stopPropagation()}>
              <h3>Delete {deleting.name}?</h3>
              <p className="pf-muted">
                This permanently deletes the business and all its transactions, categories and
                customers. This cannot be undone.
              </p>
              <div className="pf-group" style={{ marginTop: 16 }}>
                <label htmlFor="confirm">Type <strong>{deleting.name}</strong> to confirm</label>
                <input id="confirm" value={confirmName} autoFocus
                  onChange={(e) => setConfirmName(e.target.value)} />
              </div>
              <div className="pf-actions" style={{ marginTop: 16 }}>
                <button className="pf-btn-danger" style={{ marginLeft: 0 }}
                  disabled={!sameName(confirmName, deleting.name)} onClick={handleDelete}>
                  Delete Permanently
                </button>
                <button className="pf-btn-outline" onClick={() => setDeleting(null)}>Cancel</button>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export default Profile;