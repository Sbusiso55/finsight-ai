import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './Transactions.css';

const Transactions = () => {
  const [businesses, setBusinesses] = useState([]);
  const [selectedBusinessId, setSelectedBusinessId] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let ignore = false;
    const loadBusinesses = async () => {
      try {
        const res = await api.get('/api/businesses/owner');
        if (!ignore) {
          const list = Array.isArray(res.data) ? res.data : [];
          setBusinesses(list);
          if (list.length > 0) setSelectedBusinessId(list[0].id);
        }
      } catch (err) {
        console.error('Failed to load businesses:', err);
      }
    };
    loadBusinesses();
    return () => { ignore = true; };
  }, []);

  useEffect(() => {
    if (!selectedBusinessId) return;
    let ignore = false;
    const loadTransactions = async () => {
      setLoading(true);
      try {
        const res = await api.get(`/api/transactions?businessId=${selectedBusinessId}&page=0&size=50`);
        if (!ignore) {
          setTransactions(res.data?.content || []);
        }
      } catch (err) {
        console.error('Failed to load transactions:', err);
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    loadTransactions();
    return () => { ignore = true; };
  }, [selectedBusinessId]);

  const formatCurrency = (amount) => {
    const num = Number(amount) || 0;
    return `R ${num.toLocaleString('en-ZA', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  return (
    <div className="tx-layout">
      <Navbar />
      <main className="tx-container">
        <div className="tx-header-row">
          <h1>Transaction Ledger</h1>
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
          {loading ? (
            <p className="tx-msg">Loading transactions...</p>
          ) : transactions.length > 0 ? (
            transactions.map((tx) => {
              const isIncome = tx.type === 'INCOME';
              return (
                <div className="tx-row" key={tx.id}>
                  <div>
                    <h4>{tx.description || 'Untitled Transaction'}</h4>
                    <p>{tx.categoryName || tx.type} &bull; {tx.transactionDate}</p>
                  </div>
                  <div className={isIncome ? 'tx-pos tx-amt' : 'tx-neg tx-amt'}>
                    {isIncome ? '+' : '-'} {formatCurrency(tx.amount)}
                  </div>
                </div>
              );
            })
          ) : (
            <p className="tx-msg">No transactions found for this business.</p>
          )}
        </div>
      </main>
    </div>
  );
};

export default Transactions;