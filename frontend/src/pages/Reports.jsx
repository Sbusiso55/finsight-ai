import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './Reports.css';

const formatCurrency = (amount) => {
  const num = Number(amount) || 0;
  return `R ${num.toLocaleString('en-ZA', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
};

const formatDate = (d) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

const getPeriodOptions = () => {
  const today = new Date();
  return Array.from({ length: 6 }, (_, i) => {
    const d = new Date(today.getFullYear(), today.getMonth() - i, 1);
    return {
      value: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`,
      label: i === 0 ? 'This month' : d.toLocaleDateString('en-ZA', { month: 'long', year: 'numeric' }),
    };
  });
};

const getDateRange = (key) => {
  const [y, m] = key.split('-').map(Number);
  const today = new Date();
  const isCurrent = y === today.getFullYear() && m - 1 === today.getMonth();
  return {
    from: formatDate(new Date(y, m - 1, 1)),
    to: formatDate(isCurrent ? today : new Date(y, m, 0)),
  };
};

const valueOf = (r, fallback) => (r.status === 'fulfilled' ? r.value.data ?? fallback : fallback);

const Reports = () => {
  const navigate = useNavigate();
  const [businesses, setBusinesses] = useState([]);
  const [businessesLoaded, setBusinessesLoaded] = useState(false);
  const [selectedBusinessId, setSelectedBusinessId] = useState(null);
  const [period, setPeriod] = useState(getPeriodOptions()[0].value);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await api.get('/api/businesses/owner');
        const list = Array.isArray(res.data) ? res.data : [];
        setBusinesses(list);
        if (list.length > 0) setSelectedBusinessId(list[0].id);
      } catch (err) {
        console.error('Failed to load businesses:', err);
      } finally {
        setBusinessesLoaded(true);
      }
    };
    load();
  }, []);

  useEffect(() => {
    if (!selectedBusinessId) return;
    let ignore = false;

    const loadReport = async () => {
      setLoading(true);
      const { from, to } = getDateRange(period);
      const q = `businessId=${selectedBusinessId}&from=${from}&to=${to}`;

      const [rev, exp, breakdown] = await Promise.allSettled([
        api.get(`/api/transactions/revenue?${q}`),
        api.get(`/api/transactions/expenses?${q}`),
        api.get(`/api/expenses/breakdown?${q}`),
      ]);
      if (ignore) return;

      [rev, exp, breakdown].forEach((r) => {
        if (r.status === 'rejected') console.error('Report request failed:', r.reason);
      });

      setReport({
        revenue: Number(valueOf(rev, 0)) || 0,
        expenses: Number(valueOf(exp, 0)) || 0,
        breakdown: valueOf(breakdown, {}),
      });
      setLoading(false);
    };

    loadReport();
    return () => { ignore = true; };
  }, [selectedBusinessId, period]);

  if (businessesLoaded && businesses.length === 0) {
    return (
      <div className="rep-layout">
        <Navbar />
        <main className="rep-container">
          <h1 className="rep-title">Financial Reports</h1>
          <div className="rep-card">
            <p className="rep-msg">You need to set up your business first.</p>
            <button className="rep-primary-btn" onClick={() => navigate('/business-setup')}>
              Set Up Business
            </button>
          </div>
        </main>
      </div>
    );
  }

  const revenue = report?.revenue || 0;
  const expenses = report?.expenses || 0;
  const profit = revenue - expenses;
  const margin = revenue > 0 ? (profit / revenue) * 100 : 0;
  const breakdown = Object.entries(report?.breakdown || {}).sort((a, b) => Number(b[1]) - Number(a[1]));
  const maxBar = Math.max(revenue, expenses, 1);

  return (
    <div className="rep-layout">
      <Navbar />
      <main className="rep-container">
        <div className="rep-header-row">
          <div>
            <h1 className="rep-title">Financial Reports</h1>
            <p className="rep-subtitle">Your business performance for the selected period.</p>
          </div>
          <div className="rep-controls">
            {businesses.length > 1 && (
              <select
                className="rep-select"
                value={selectedBusinessId || ''}
                onChange={(e) => setSelectedBusinessId(Number(e.target.value))}
              >
                {businesses.map((b) => (
                  <option key={b.id} value={b.id}>{b.name}</option>
                ))}
              </select>
            )}
            <select className="rep-select" value={period} onChange={(e) => setPeriod(e.target.value)}>
              {getPeriodOptions().map((p) => (
                <option key={p.value} value={p.value}>{p.label}</option>
              ))}
            </select>
          </div>
        </div>

        {loading ? (
          <p className="rep-msg">Generating report...</p>
        ) : (
          <>
            <div className="rep-grid">
              <div className="rep-card">
                <span>Total Revenue</span>
                <h3 className="rep-pos">{formatCurrency(revenue)}</h3>
              </div>
              <div className="rep-card">
                <span>Total Expenses</span>
                <h3 className="rep-neg">{formatCurrency(expenses)}</h3>
              </div>
              <div className="rep-card">
                <span>Net Profit</span>
                <h3 className={profit >= 0 ? 'rep-pos' : 'rep-neg'}>{formatCurrency(profit)}</h3>
              </div>
              <div className="rep-card">
                <span>Net Profit Margin</span>
                <h3>{margin.toFixed(2)}%</h3>
              </div>
            </div>

            <div className="rep-lower">
              <div className="rep-card">
                <h2 className="rep-section-title">Income vs Expenses</h2>
                <div className="rep-bar-item">
                  <div className="rep-bar-labels"><span>Income</span><span>{formatCurrency(revenue)}</span></div>
                  <div className="rep-bar-track">
                    <div className="rep-bar-fill rep-fill-pos" style={{ width: `${(revenue / maxBar) * 100}%` }} />
                  </div>
                </div>
                <div className="rep-bar-item">
                  <div className="rep-bar-labels"><span>Expenses</span><span>{formatCurrency(expenses)}</span></div>
                  <div className="rep-bar-track">
                    <div className="rep-bar-fill rep-fill-neg" style={{ width: `${(expenses / maxBar) * 100}%` }} />
                  </div>
                </div>
              </div>

              <div className="rep-card">
                <h2 className="rep-section-title">Expense Breakdown</h2>
                {breakdown.length > 0 ? (
                  breakdown.map(([category, raw]) => {
                    const amount = Number(raw) || 0;
                    const pct = expenses > 0 ? (amount / expenses) * 100 : 0;
                    return (
                      <div className="rep-bar-item" key={category}>
                        <div className="rep-bar-labels">
                          <span>{category}</span>
                          <span>{formatCurrency(amount)} ({pct.toFixed(1)}%)</span>
                        </div>
                        <div className="rep-bar-track">
                          <div className="rep-bar-fill" style={{ width: `${Math.min(100, pct)}%` }} />
                        </div>
                      </div>
                    );
                  })
                ) : (
                  <p className="rep-msg">No expenses in this period.</p>
                )}
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
};

export default Reports;