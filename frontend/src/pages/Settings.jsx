import React, { useState, useEffect, useCallback } from 'react';
import { useLocation } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './Settings.css';

const PREFS_KEY = 'clearside.preferences';

const loadPrefs = () => {
  try {
    const raw = localStorage.getItem(PREFS_KEY);
    return raw ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
};

const Settings = () => {
  const location = useLocation();
  const stored = loadPrefs();

  const [emailNotifications, setEmailNotifications] = useState(stored.emailNotifications ?? true);
  const [aiAlerts, setAiAlerts] = useState(stored.aiAlerts ?? true);
  const [currency, setCurrency] = useState(stored.currency ?? 'ZAR');
  const [saved, setSaved] = useState(false);

  const [businessId, setBusinessId] = useState(null);
  const [categories, setCategories] = useState([]);
  const [catLoading, setCatLoading] = useState(true);
  const [catError, setCatError] = useState('');
  const [newCat, setNewCat] = useState({ name: '', type: 'EXPENSE' });
  const [adding, setAdding] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [editForm, setEditForm] = useState({ name: '', type: 'EXPENSE' });
  const [busyId, setBusyId] = useState(null);

  const loadCategories = useCallback(async (bizId) => {
    const res = await api.get(`/api/categories/business/${bizId}`);
    setCategories(Array.isArray(res.data) ? res.data : []);
  }, []);

  useEffect(() => {
    let ignore = false;
    const load = async () => {
      try {
        const bizRes = await api.get('/api/businesses/owner');
        const list = Array.isArray(bizRes.data) ? bizRes.data : [];
        if (ignore) return;
        if (list.length === 0) {
          setCatLoading(false);
          return;
        }
        setBusinessId(list[0].id);
        await loadCategories(list[0].id);
      } catch (err) {
        if (!ignore) setCatError('Could not load your categories.');
      } finally {
        if (!ignore) setCatLoading(false);
      }
    };
    load();
    return () => {
      ignore = true;
    };
  }, [loadCategories]);

  useEffect(() => {
    if (location.hash === '#categories' && !catLoading) {
      const el = document.getElementById('categories');
      if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }, [location.hash, catLoading]);

  const handleSave = (e) => {
    e.preventDefault();
    localStorage.setItem(PREFS_KEY, JSON.stringify({ emailNotifications, aiAlerts, currency }));
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  const errorText = (err, fallback) => err.response?.data?.message || fallback;

  const handleAdd = async (e) => {
    e.preventDefault();
    if (!newCat.name.trim() || !businessId) return;
    setCatError('');
    setAdding(true);
    try {
      await api.post('/api/categories', {
        name: newCat.name.trim(),
        type: newCat.type,
        business: { id: businessId },
      });
      setNewCat({ name: '', type: newCat.type });
      await loadCategories(businessId);
    } catch (err) {
      setCatError(errorText(err, 'Could not add the category.'));
    } finally {
      setAdding(false);
    }
  };

  const startEdit = (c) => {
    setEditingId(c.id);
    setEditForm({ name: c.name || '', type: c.type || 'EXPENSE' });
    setCatError('');
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditForm({ name: '', type: 'EXPENSE' });
  };

  const handleUpdate = async (c) => {
    if (!editForm.name.trim()) return;
    setCatError('');
    setBusyId(c.id);
    try {
      await api.put(`/api/categories/${c.id}`, {
        name: editForm.name.trim(),
        type: editForm.type,
        costBehavior: c.costBehavior ?? null,
        costClassification: c.costClassification ?? null,
      });
      cancelEdit();
      await loadCategories(businessId);
    } catch (err) {
      setCatError(errorText(err, 'Could not update the category.'));
    } finally {
      setBusyId(null);
    }
  };

  const handleDelete = async (c) => {
    if (!window.confirm(`Delete "${c.name}"?`)) return;
    setCatError('');
    setBusyId(c.id);
    try {
      await api.delete(`/api/categories/${c.id}`);
      await loadCategories(businessId);
    } catch (err) {
      setCatError(errorText(err, 'Could not delete this category. If transactions use it, move or delete those first.'));
    } finally {
      setBusyId(null);
    }
  };

  const income = categories.filter((c) => c.type === 'INCOME');
  const expense = categories.filter((c) => c.type !== 'INCOME');

  const renderEditRow = (c) => (
    <li key={c.id} className="cat-row">
      <div className="cat-edit">
        <input
          type="text"
          value={editForm.name}
          onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
          aria-label="Category name"
          autoFocus
        />
        <select
          value={editForm.type}
          onChange={(e) => setEditForm({ ...editForm, type: e.target.value })}
          aria-label="Category type"
        >
          <option value="INCOME">Income</option>
          <option value="EXPENSE">Expense</option>
        </select>
        <div className="cat-actions">
          <button type="button" className="cat-btn cat-btn-primary" onClick={() => handleUpdate(c)} disabled={busyId === c.id}>
            {busyId === c.id ? 'Saving...' : 'Save'}
          </button>
          <button type="button" className="cat-btn" onClick={cancelEdit}>
            Cancel
          </button>
        </div>
      </div>
    </li>
  );

  const renderViewRow = (c) => (
    <li key={c.id} className="cat-row">
      <span className="cat-name">{c.name}</span>
      <div className="cat-actions">
        <button type="button" className="cat-btn" onClick={() => startEdit(c)}>
          Edit
        </button>
        <button type="button" className="cat-btn cat-btn-danger" onClick={() => handleDelete(c)} disabled={busyId === c.id}>
          {busyId === c.id ? '...' : 'Delete'}
        </button>
      </div>
    </li>
  );

  const renderRow = (c) => (editingId === c.id ? renderEditRow(c) : renderViewRow(c));

  return (
    <div className="settings-layout">
      <Navbar />
      <main className="settings-container">
        <h1 className="settings-title">Settings</h1>
        <p className="settings-subtitle">Manage your preferences and how your money is organised.</p>

        {saved && <div className="settings-success">Preferences saved.</div>}

        <form className="settings-card" onSubmit={handleSave} style={{ animationDelay: '60ms' }}>
          <h2>Preferences &amp; Notifications</h2>

          <div className="settings-toggle-row">
            <div>
              <h4>Email Notifications</h4>
              <p>Receive weekly financial summaries and transaction digests.</p>
            </div>
            <label className="settings-switch">
              <input type="checkbox" checked={emailNotifications} onChange={(e) => setEmailNotifications(e.target.checked)} />
              <span className="settings-slider" />
            </label>
          </div>

          <div className="settings-toggle-row">
            <div>
              <h4>Clearside Pay Insights &amp; Alerts</h4>
              <p>Get a warning when spending goes over your budget.</p>
            </div>
            <label className="settings-switch">
              <input type="checkbox" checked={aiAlerts} onChange={(e) => setAiAlerts(e.target.checked)} />
              <span className="settings-slider" />
            </label>
          </div>

          <div className="settings-field-group">
            <label htmlFor="currency">Default Currency</label>
            <select id="currency" value={currency} onChange={(e) => setCurrency(e.target.value)}>
              <option value="ZAR">ZAR (South African Rand)</option>
              <option value="USD">USD (US Dollar)</option>
            </select>
          </div>

          <p className="settings-hint">Use the light/dark switch to change the look of the app.</p>

          <button type="submit" className="settings-save-btn">
            Save Preferences
          </button>
        </form>

        <section id="categories" className="settings-card" style={{ animationDelay: '140ms' }}>
          <h2>Categories</h2>
          <p className="settings-hint">Group your income and expenses so your reports make sense.</p>

          {catLoading && (
            <div className="cat-skeleton">
              <span />
              <span />
              <span />
            </div>
          )}

          {!catLoading && !businessId && <p className="settings-hint">Set up your business first to add categories.</p>}

          {!catLoading && businessId && (
            <>
              <form className="cat-add" onSubmit={handleAdd}>
                <input
                  type="text"
                  placeholder="New category, e.g. Stock purchases"
                  value={newCat.name}
                  onChange={(e) => setNewCat({ ...newCat, name: e.target.value })}
                  aria-label="New category name"
                />
                <select
                  value={newCat.type}
                  onChange={(e) => setNewCat({ ...newCat, type: e.target.value })}
                  aria-label="New category type"
                >
                  <option value="INCOME">Income</option>
                  <option value="EXPENSE">Expense</option>
                </select>
                <button type="submit" className="cat-btn cat-btn-primary" disabled={adding || !newCat.name.trim()}>
                  {adding ? 'Adding...' : 'Add'}
                </button>
              </form>

              {catError && <p className="settings-error">{catError}</p>}

              <div className="cat-columns">
                <div>
                  <h4 className="cat-heading">Income</h4>
                  {income.length > 0 ? (
                    <ul className="cat-list">{income.map(renderRow)}</ul>
                  ) : (
                    <p className="settings-hint">No income categories yet.</p>
                  )}
                </div>
                <div>
                  <h4 className="cat-heading">Expense</h4>
                  {expense.length > 0 ? (
                    <ul className="cat-list">{expense.map(renderRow)}</ul>
                  ) : (
                    <p className="settings-hint">No expense categories yet.</p>
                  )}
                </div>
              </div>
            </>
          )}
        </section>
      </main>
    </div>
  );
};

export default Settings;
