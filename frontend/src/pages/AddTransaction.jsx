import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './AddTransaction.css';

const AddTransaction = () => {
  const navigate = useNavigate();
  const [businessId, setBusinessId] = useState(null);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({
    description: '',
    amount: '',
    type: 'INCOME',
    categoryId: '',
    transactionDate: new Date().toISOString().slice(0, 10),
  });
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [noBusiness, setNoBusiness] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        const bizRes = await api.get('/api/businesses/owner');
        const list = Array.isArray(bizRes.data) ? bizRes.data : [];
        if (list.length === 0) {
          setNoBusiness(true);
          return;
        }
        setBusinessId(list[0].id);
        const catRes = await api.get(`/api/categories/business/${list[0].id}`);
        setCategories(Array.isArray(catRes.data) ? catRes.data : []);
      } catch (err) {
        setError('Could not load your business details');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const visibleCategories = categories.filter((c) => !c.type || c.type === form.type);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value, ...(name === 'type' ? { categoryId: '' } : {}) }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      await api.post('/api/transactions', {
        businessId,
        categoryId: Number(form.categoryId),
        amount: Number(form.amount),
        type: form.type,
        description: form.description,
        transactionDate: form.transactionDate,
      });
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save transaction');
    } finally {
      setSaving(false);
    }
  };

  if (noBusiness) {
    return (
      <div className="add-trans-wrapper">
        <Navbar />
        <div className="add-trans-content">
          <h1 className="add-trans-title">Add Transaction</h1>
          <p className="add-trans-subtitle">You need to set up your business first.</p>
          <button className="add-trans-submit" style={{ width: '100%' }} onClick={() => navigate('/business-setup')}>
            Set Up Business
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="add-trans-wrapper">
      <Navbar />
      <div className="add-trans-content">
        <h1 className="add-trans-title">Add Transaction</h1>
        <p className="add-trans-subtitle">Record a new income or expense.</p>

        <form className="add-trans-form" onSubmit={handleSubmit}>
          <div className="add-trans-field">
            <label htmlFor="description">Description</label>
            <input id="description" name="description" type="text" placeholder="What was this for?"
              value={form.description} onChange={handleChange} required />
          </div>

          <div className="add-trans-row">
            <div className="add-trans-field">
              <label htmlFor="amount">Amount (R)</label>
              <input id="amount" name="amount" type="number" min="0.01" step="0.01" placeholder="0.00"
                value={form.amount} onChange={handleChange} required />
            </div>
            <div className="add-trans-field">
              <label htmlFor="type">Type</label>
              <select id="type" name="type" value={form.type} onChange={handleChange}>
                <option value="INCOME">Income</option>
                <option value="EXPENSE">Expense</option>
              </select>
            </div>
          </div>

          <div className="add-trans-row">
            <div className="add-trans-field">
              <div className="add-trans-label-row">
                <label htmlFor="categoryId">Category</label>
                <Link to="/settings#categories" className="add-trans-link">+ New category</Link>
              </div>
              <select id="categoryId" name="categoryId" value={form.categoryId} onChange={handleChange}
                required disabled={loading}>
                <option value="">
                  {loading ? 'Loading...' : visibleCategories.length ? 'Select category' : 'No categories yet'}
                </option>
                {visibleCategories.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
            <div className="add-trans-field">
              <label htmlFor="transactionDate">Date</label>
              <input id="transactionDate" name="transactionDate" type="date"
                value={form.transactionDate} onChange={handleChange} required />
            </div>
          </div>

          {error && <p className="add-trans-error">{error}</p>}

          <button type="submit" className="add-trans-submit" disabled={saving || !businessId}>
            {saving ? 'Saving...' : 'Save Transaction'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default AddTransaction;