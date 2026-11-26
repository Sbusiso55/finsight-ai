import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import ProLock from '../components/ProLock';
import useSubscription from '../hooks/useSubscription';
import api from '../api/axiosConfig';
import './Reports.css';

const FREE_MONTHS = 3;

const formatCurrency = (amount) => {
  const num = Number(amount) || 0;
  return `R ${num.toLocaleString('en-ZA', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
};

const pad = (n) => String(n).padStart(2, '0');

const formatDate = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;

const getPeriodOptions = () => {
  const today = new Date();
  return Array.from({ length: 6 }, (_, i) => {
    const d = new Date(today.getFullYear(), today.getMonth() - i, 1);
    return {
      value: `${d.getFullYear()}-${pad(d.getMonth() + 1)}`,
      label: i === 0 ? 'This month' : d.toLocaleDateString('en-ZA', { month: 'long', year: 'numeric' }),
      proOnly: i >= FREE_MONTHS,
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
  const { isPro } = useSubscription();
  const periodOptions = getPeriodOptions();
  const [businesses, setBusinesses] = useState([]);
  const [businessesLoaded, setBusinessesLoaded] = useState(false);
  const [selectedBusinessId, setSelectedBusinessId] = useState(null);
  const [period, setPeriod] = useState(periodOptions[0].value);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);

  const selectedOption = periodOptions.find((p) => p.value === period);
  const periodLocked = !isPro && selectedOption?.proOnly;

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
    if (!selectedBusinessId) return undefined;
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
    return () => {
      ignore = true;
    };
  }, [selectedBusinessId, period]);

  if (businessesLoaded && businesses.length === 0) {
    return (
      <div className="rep-layout">
        <Navbar />
        <main className="rep-container">
          <h1 className="rep-title">Financial Reports</h1>
          <div className="rep-card rep-empty">
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

  const summaryCards = (
    <div className="rep-grid">
      <div className="rep-card rep-stat" style={{ animationDelay: '40ms' }}>
        <span>Total Revenue</span>
        <h3 className="rep-pos">{formatCurrency(revenue)}</h3>
      </div>
      <div className="rep-card rep-stat" style={{ animationDelay: '100ms' }}>
        <span>Total Expenses</span>
        <h3 className="rep-neg">{formatCurrency(expenses)}</h3>
      </div>
      <div className="rep-card rep-stat" style={{ animationDelay: '160ms' }}>
        <span>Net Profit</span>
        <h3 className={profit >= 0 ? 'rep-pos' : 'rep-neg'}>{formatCurrency(profit)}</h3>
      </div>
      <div className="rep-card rep-stat" style={{ animationDelay: '220ms' }}>
        <span>Net Profit Margin</span>
        <h3>{margin.toFixed(2)}%</h3>
      </div>
    </div>
  );

  const incomeVsExpenses = (
    <div className="rep-card">
      <h2 className="rep-section-title">Income vs Expenses</h2>
      <div className="rep-bar-item">
        <div className="rep-bar-labels">
          <span>Income</span>
          <span>{formatCurrency(revenue)}</span>
        </div>
        <div className="rep-bar-track">
          <div className="rep-bar-fill rep-fill-pos" style={{ width: `${(revenue / maxBar) * 100}%` }} />
        </div>
      </div>
      <div className="rep-bar-item">
        <div className="rep-bar-labels">
          <span>Expenses</span>
          <span>{formatCurrency(expenses)}</span>
        </div>
        <div className="rep-bar-track">
          <div className="rep-bar-fill rep-fill-neg" style={{ width: `${(expenses / maxBar) * 100}%` }} />
        </div>
      </div>
    </div>
  );

  const renderBreakdownRow = ([category, raw]) => {
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
  };

  const expenseBreakdown = (
    <div className="rep-card">
      <h2 className="rep-section-title">Expense Breakdown</h2>
      {breakdown.length > 0 ? breakdown.map(renderBreakdownRow) : <p className="rep-msg">No expenses in this period.</p>}
    </div>
  );

  const fullReport = (
    <div key={`${selectedBusinessId}-${period}`}>
      {summaryCards}
      <div className="rep-lower">
        <ProLock feature="Income vs Expenses">{incomeVsExpenses}</ProLock>
        {expenseBreakdown}
      </div>
    </div>
  );

  let body;
  if (loading) {
    body = (
      <div className="rep-skeleton">
        <div className="rep-skeleton-grid">
          <span />
          <span />
          <span />
          <span />
        </div>
        <span className="rep-skeleton-big" />
      </div>
    );
  } else if (periodLocked) {
    body = <ProLock feature="History older than 3 months">{fullReport}</ProLock>;
  } else {
    body = fullReport;
  }

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
                aria-label="Choose business"
              >
                {businesses.map((b) => (
                  <option key={b.id} value={b.id}>{b.name}</option>
                ))}
              </select>
            )}
            <select className="rep-select" value={period} onChange={(e) => setPeriod(e.target.value)} aria-label="Choose period">
              {periodOptions.map((p) => (
                <option key={p.value} value={p.value}>
                  {p.label}{!isPro && p.proOnly ? ' (Pro)' : ''}
                </option>
              ))}
            </select>
          </div>
        </div>

        {body}
      </main>
    </div>
  );
};

export default Reports;
