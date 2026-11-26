import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './AddTransaction.css';

const BusinessSetup = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    industry: '',
    currency: 'ZAR',
    openingBalance: '',
    openingBalanceDate: new Date().toISOString().slice(0, 10),
    seasonal: false,
  });
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      await api.post('/api/businesses', {
        ...form,
        openingBalance: form.openingBalance === '' ? 0 : Number(form.openingBalance),
      });
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create business');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="add-trans-wrapper">
      <Navbar />
      <div className="add-trans-content">
        <h1 className="add-trans-title">Set up your business</h1>
        <p className="add-trans-subtitle">Tell us about your business to start tracking its finances.</p>

        <form className="add-trans-form" onSubmit={handleSubmit}>
          <div className="add-trans-field">
            <label htmlFor="name">Business Name</label>
            <input id="name" name="name" type="text" placeholder="e.g. Kev's Spaza Shop"
              value={form.name} onChange={handleChange} required />
          </div>

          <div className="add-trans-row">
            <div className="add-trans-field">
              <label htmlFor="industry">Industry</label>
              <select id="industry" name="industry" value={form.industry} onChange={handleChange} required>
                <option value="">Select industry</option>
                <option value="Retail">Retail</option>
                <option value="Food & Beverage">Food & Beverage</option>
                <option value="Services">Services</option>
                <option value="Beauty & Wellness">Beauty & Wellness</option>
                <option value="Transport">Transport</option>
                <option value="Other">Other</option>
              </select>
            </div>
            <div className="add-trans-field">
              <label htmlFor="currency">Currency</label>
              <select id="currency" name="currency" value={form.currency} onChange={handleChange}>
                <option value="ZAR">ZAR (R)</option>
              </select>
            </div>
          </div>

          <div className="add-trans-row">
            <div className="add-trans-field">
              <label htmlFor="openingBalance">Opening Balance (R)</label>
              <input id="openingBalance" name="openingBalance" type="number" min="0" step="0.01" placeholder="0.00"
                value={form.openingBalance} onChange={handleChange} />
            </div>
            <div className="add-trans-field">
              <label htmlFor="openingBalanceDate">Balance Date</label>
              <input id="openingBalanceDate" name="openingBalanceDate" type="date"
                value={form.openingBalanceDate} onChange={handleChange} required />
            </div>
          </div>

          <label className="add-trans-check">
            <input name="seasonal" type="checkbox" checked={form.seasonal} onChange={handleChange} />
            My business is seasonal (busier at certain times of year)
          </label>

          {error && <p className="add-trans-error">{error}</p>}

          <button type="submit" className="add-trans-submit" disabled={saving}>
            {saving ? 'Creating...' : 'Create Business'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default BusinessSetup;