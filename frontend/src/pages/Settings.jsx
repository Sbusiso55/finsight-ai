import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './Settings.css';

const Settings = () => {
  const [emailNotifications, setEmailNotifications] = useState(true);
  const [aiAlerts, setAiAlerts] = useState(true);
  const [currency, setCurrency] = useState('ZAR');
  const [theme, setTheme] = useState('dark-charcoal');
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    let ignore = false;
    const loadSettings = async () => {
      try {
        const res = await api.get('/api/users/user');
        if (!ignore && res.data) {
          // If user preferences exist, they can be initialized here
        }
      } catch (err) {
        console.error('Failed to load settings preferences:', err);
      }
    };
    loadSettings();
    return () => { ignore = true; };
  }, []);

  const handleSave = (e) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  return (
    <div className="settings-layout">
      <Navbar />
      <main className="settings-container">
        <h1>Application Settings</h1>

        {saved && <div className="settings-success">Preferences saved successfully.</div>}

        <form className="settings-card" onSubmit={handleSave}>
          <h2>Preferences & Notifications</h2>

          <div className="settings-toggle-row">
            <div>
              <h4>Email Notifications</h4>
              <p>Receive weekly financial summaries and transaction digests.</p>
            </div>
            <label className="settings-switch">
              <input
                type="checkbox"
                checked={emailNotifications}
                onChange={(e) => setEmailNotifications(e.target.checked)}
              />
              <span className="settings-slider" />
            </label>
          </div>

          <div className="settings-toggle-row">
            <div>
              <h4>FinSight AI Insights & Alerts</h4>
              <p>Get real-time AI warnings when spending exceeds budget thresholds.</p>
            </div>
            <label className="settings-switch">
              <input
                type="checkbox"
                checked={aiAlerts}
                onChange={(e) => setAiAlerts(e.target.checked)}
              />
              <span className="settings-slider" />
            </label>
          </div>

          <div className="settings-field-group">
            <label>Default Currency</label>
            <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
              <option value="ZAR">ZAR (South African Rand)</option>
              <option value="USD">USD (US Dollar)</option>
            </select>
          </div>

          <div className="settings-field-group">
            <label>Interface Theme</label>
            <select value={theme} onChange={(e) => setTheme(e.target.value)}>
              <option value="dark-charcoal">Dark Charcoal & Warm Gold</option>
              <option value="fnb-light">FNB Clean Light Banking</option>
            </select>
          </div>

          <button type="submit" className="settings-save-btn">Save Preferences</button>
        </form>
      </main>
    </div>
  );
};

export default Settings;