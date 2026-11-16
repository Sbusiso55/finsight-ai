import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './Transactions.css';

const formatCurrency = (amount) => {
  const num = Number(amount) || 0;
  return `R ${num.toLocaleString('en-ZA', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
};

const Transactions = () => {
  const navigate = useNavigate();
  const [businesses, setBusinesses] = useState([]);
  const [businessesLoaded, setBusinessesLoaded] = useState(false);
  const [selectedBusinessId, setSelectedBusinessId] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [search, setSearch] = useState('');

  useEffect(() => {
    const loadBusinesses = async () => {
      try {
        const res = await api.get('/api/businesses/owner');
        const list = Array.isArray(res.data) ? res.data : [];
        setBusinesses(list);
        if (list.length > 0) setSelectedBusinessId(list[0].id);
      } catch (err) {
        setError('Could not load your businesses');
      } finally {
        setBusinessesLoaded(true);
      }
    };
    loadBusinesses();
  }, []);

  const loadTransactions = useCallback(async () => {
    if (!selectedBusinessId) return;
    setLoading(true);
    setError('');
    try {
      const res = await api.get(
        `/api/transactions?businessId=${selectedBusinessId}&page=0&size=50&sort=createdAt,desc`
      );
      setTransactions(res.data?.content || []);
    } catch (err) {
      setError('Could not load transactions');
    } finally {
      setLoading(false);
    }
  }, [selectedBusinessId]);

  useEffect(() => {
    loadTransactions();
  }, [loadTransactions]);

  const handleDelete = async (tx) => {
    if (!window.confirm(`Delete "${tx.description || 'this transaction'}"?`)) return;
    try {
      await api.delete(`/api/transactions/${tx.id}?businessId=${selectedBusinessId}`);
      setTransactions((prev) => prev.filter((t) => t.id !== tx.id));
    } catch (err) {
      setError('Could not delete transaction');
    }
  };

  const visible = transactions.filter((tx) => {
    const matchesType = typeFilter === 'ALL' || tx.type === typeFilter;
    const text = `${tx.description || ''} ${tx.category?.name || ''}`.toLowerCase();
    return matchesType && text.includes(search.toLowerCase());
  });

  const totalIn = visible.filter((t) => t.type === 'INCOME').reduce((s, t) => s + Number(t.amount), 0);
  const totalOut = visible.filter((t) => t.type === 'EXPENSE').reduce((s, t) => s + Number(t.amount), 0);

  if (businessesLoaded && businesses.length === 0) {
    return (
      <div className="tx-layout">
        <Navbar />
        <main className="tx-container">
          <h1 className="tx-title">Transaction Ledger</h1>
          <div className="tx-card">
            <p className="tx-msg">You need to set up your business first.</p>
            <button className="tx-primary-btn" onClick={() => navigate('/business-setup')}>
              Set Up Business
            </button>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="tx-layout">
      <Navbar />
      <main className="tx-container">
        <div className="tx-header-row">
          <div>
            <h1 className="tx-title">Transaction Ledger</h1>
            <p className="tx-subtitle">Every income and expense recorded for your business.</p>
          </div>
          <button className="tx-primary-btn" onClick={() => navigate('/add-transaction')}>
            + Add Transaction
          </button>
        </div>

        <div className="tx-summary">
          <div className="tx-summary-card">
            <span>Money In</span>
            <h3 className="tx-pos">{formatCurrency(totalIn)}</h3>
          </div>
          <div className="tx-summary-card">
            <span>Money Out</span>
            <h3 className="tx-neg">{formatCurrency(totalOut)}</h3>
          </div>
          <div className="tx-summary-card">
            <span>Net</span>
            <h3>{formatCurrency(totalIn - totalOut)}</h3>
          </div>
        </div>

        <div className="tx-filters">
          <input
            className="tx-search"
            type="text"
            placeholder="Search description or category..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <select className="tx-select" value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
            <option value="ALL">All types</option>
            <option value="INCOME">Income</option>
            <option value="EXPENSE">Expenses</option>
          </select>
          {businesses.length > 1 && (
            <select
              className="tx-select"
              value={selectedBusinessId || ''}
              onChange={(e) => setSelectedBusinessId(Number(e.target.value))}
            >
              {businesses.map((b) => (
                <option key={b.id} value={b.id}>{b.name}</option>
              ))}
            </select>
          )}
        </div>

        <div className="tx-card">
          {error && <p className="tx-error">{error}</p>}

          {loading ? (
            <p className="tx-msg">Loading transactions...</p>
          ) : visible.length > 0 ? (
            visible.map((tx) => {
              const isIncome = tx.type === 'INCOME';
              return (
                <div className="tx-row" key={tx.id}>
                  <div>
                    <h4>{tx.description || 'Untitled Transaction'}</h4>
                    <p>{tx.category?.name || tx.type} &bull; {tx.transactionDate}</p>
                  </div>
                  <div className="tx-row-right">
                    <span className={`tx-amt ${isIncome ? 'tx-pos' : 'tx-neg'}`}>
                      {isIncome ? '+' : '-'} {formatCurrency(tx.amount)}
                    </span>
                    <button className="tx-delete-btn" onClick={() => handleDelete(tx)}>
                      Delete
                    </button>
                  </div>
                </div>
              );
            })
          ) : (
            <p className="tx-msg">No transactions found.</p>
          )}
        </div>
      </main>
    </div>
  );
};

export default Transactions;