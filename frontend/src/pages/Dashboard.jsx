import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './Dashboard.css';

const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
};

const toPeriodKey = (date) =>
  `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;

const formatDate = (date) =>
  `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;

const getPeriodOptions = () => {
  const today = new Date();
  return Array.from({ length: 4 }, (_, i) => {
    const date = new Date(today.getFullYear(), today.getMonth() - i, 1);
    return {
      value: toPeriodKey(date),
      label: i === 0 ? 'This month' : date.toLocaleDateString('en-ZA', { month: 'long', year: 'numeric' }),
    };
  });
};

const getDateRange = (periodKey) => {
  const [year, month] = periodKey.split('-').map(Number);
  const today = new Date();
  const from = new Date(year, month - 1, 1);
  const isCurrentMonth = year === today.getFullYear() && month - 1 === today.getMonth();
  const to = isCurrentMonth ? today : new Date(year, month, 0);
  return { from: formatDate(from), to: formatDate(to) };
};

const formatCurrency = (amount) => {
  const num = Number(amount) || 0;
  return `R ${num.toLocaleString('en-ZA', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
};

const Dashboard = () => {
  const navigate = useNavigate();

  const [user, setUser] = useState(null);
  const [businesses, setBusinesses] = useState([]);
  const [businessesLoaded, setBusinessesLoaded] = useState(false);
  const [selectedBusinessId, setSelectedBusinessId] = useState(null);
  const [selectedPeriod, setSelectedPeriod] = useState(() => toPeriodKey(new Date()));
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  useEffect(() => {
    let ignore = false;

    const loadUserAndBusinesses = async () => {
      const [userRes, bizRes] = await Promise.allSettled([
        api.get('/api/users/user'),
        api.get('/api/businesses/owner'),
      ]);
      if (ignore) return;

      if (userRes.status === 'fulfilled') {
        setUser(userRes.value.data);
      } else {
        console.error('Failed to load user:', userRes.reason);
      }

      if (bizRes.status === 'fulfilled') {
        const list = Array.isArray(bizRes.value.data) ? bizRes.value.data : [];
        setBusinesses(list);
        if (list.length > 0) setSelectedBusinessId(list[0].id);
      } else {
        console.error('Failed to load businesses:', bizRes.reason);
      }

      setBusinessesLoaded(true);
    };

    loadUserAndBusinesses();
    return () => { ignore = true; };
  }, []);

  useEffect(() => {
    if (!selectedBusinessId) return;
    let ignore = false;

    const loadFinancials = async () => {
      setLoading(true);
      setError(false);
      const { from, to } = getDateRange(selectedPeriod);
      const q = `businessId=${selectedBusinessId}&from=${from}&to=${to}`;

      try {
        const [revenueRes, expensesRes, marginRes, breakdownRes, txRes] = await Promise.all([
          api.get(`/api/revenue/total?${q}`),
          api.get(`/api/expenses/total?${q}`),
          api.get(`/api/profit-margin/net?${q}`),
          api.get(`/api/expenses/breakdown?${q}`),
          api.get(`/api/transactions?businessId=${selectedBusinessId}&page=0&size=5`),
        ]);
        if (ignore) return;
        setData({
          revenue: revenueRes.data ?? 0,
          expenses: expensesRes.data ?? 0,
          netProfitMargin: marginRes.data ?? 0,
          expenseBreakdown: breakdownRes.data ?? {},
          transactions: txRes.data?.content ?? [],
        });
      } catch (err) {
        if (!ignore) {
          console.error('Error loading dashboard metrics:', err);
          setError(true);
        }
      } finally {
        if (!ignore) setLoading(false);
      }
    };

    loadFinancials();
    return () => { ignore = true; };
  }, [selectedBusinessId, selectedPeriod]);

  const currentBusiness = businesses.find((b) => b.id === selectedBusinessId);
  const totalExpenses = Number(data?.expenses) || 0;
  const breakdown = Object.entries(data?.expenseBreakdown || {});
  const netCashPosition =
    (Number(currentBusiness?.openingBalance) || 0) + (Number(data?.revenue) || 0) - totalExpenses;

  return (
    <div className="fnb-dashboard-layout">
      <Navbar />

      <main className="fnb-dashboard-container">
        {businessesLoaded && businesses.length === 0 && (
          <div className="fnb-panel" style={{ marginBottom: 24 }}>
            <h2 style={{ fontSize: 17, margin: '0 0 8px 0' }}>Set up your business</h2>
            <p className="fnb-status-text">
              You haven't added a business yet. Add one to start tracking your finances.
            </p>
          </div>
        )}

        <header className="fnb-topbar">
          <div>
            <h1 className="fnb-greeting">
              {getGreeting()}, {user ? user.fullName : 'Entrepreneur'}
            </h1>
            <p className="fnb-subgreeting">
              {currentBusiness ? currentBusiness.name : 'No business selected'} &bull; Live Financial Feed
            </p>
          </div>

          <div className="fnb-controls">
            {businesses.length > 1 && (
              <select
                className="fnb-select"
                value={selectedBusinessId || ''}
                onChange={(e) => setSelectedBusinessId(Number(e.target.value))}
              >
                {businesses.map((biz) => (
                  <option key={biz.id} value={biz.id}>{biz.name}</option>
                ))}
              </select>
            )}

            <select
              className="fnb-select"
              value={selectedPeriod}
              onChange={(e) => setSelectedPeriod(e.target.value)}
            >
              {getPeriodOptions().map((p) => (
                <option key={p.value} value={p.value}>{p.label}</option>
              ))}
            </select>
          </div>
        </header>

        <section className="fnb-account-card">
          <div>
            <span className="fnb-account-tag">Net Business Cash Position</span>
            <div className="fnb-account-balance">{formatCurrency(netCashPosition)}</div>
            <p className="fnb-account-meta">
              Opening Balance: {formatCurrency(currentBusiness?.openingBalance)} &bull; Currency:{' '}
              {currentBusiness?.currency || 'ZAR'}
            </p>
          </div>
          <button className="fnb-primary-cta" onClick={() => navigate('/add-transaction')}>
            + Record Transaction
          </button>
        </section>

        <section className="fnb-quick-actions">
          <button className="fnb-action-tile" onClick={() => navigate('/add-transaction')}>
            <div className="fnb-action-icon">+</div>
            <span>Record Entry</span>
          </button>
          <button className="fnb-action-tile" onClick={() => navigate('/transactions')}>
            <div className="fnb-action-icon">&#8644;</div>
            <span>All Transactions</span>
          </button>
          <button className="fnb-action-tile" onClick={() => navigate('/ask-ai')}>
            <div className="fnb-action-icon">AI</div>
            <span>Ask FinSight</span>
          </button>
          <button className="fnb-action-tile" onClick={() => navigate('/reports')}>
            <div className="fnb-action-icon">%</div>
            <span>Reports</span>
          </button>
        </section>

        <section className="fnb-metrics-grid">
          <div className="fnb-metric-card">
            <span>Period Revenue</span>
            <h3 className="fnb-positive">{formatCurrency(data?.revenue)}</h3>
          </div>
          <div className="fnb-metric-card">
            <span>Period Expenses</span>
            <h3 className="fnb-negative">{formatCurrency(data?.expenses)}</h3>
          </div>
          <div className="fnb-metric-card">
            <span>Net Profit Margin</span>
            <h3>{Number(data?.netProfitMargin || 0).toFixed(2)}%</h3>
          </div>
        </section>

        <section className="fnb-lower-grid">
          <div className="fnb-panel">
            <div className="fnb-panel-header">
              <h2>Recent Transactions</h2>
              <button onClick={() => navigate('/transactions')}>View All</button>
            </div>

            {loading ? (
              <p className="fnb-status-text">Loading recent transactions...</p>
            ) : error ? (
              <p className="fnb-status-text">Unable to load transactions.</p>
            ) : data?.transactions?.length > 0 ? (
              data.transactions.map((tx) => {
                const isIncome = tx.type === 'INCOME';
                return (
                  <div className="fnb-tx-row" key={tx.id}>
                    <div>
                      <h4>{tx.description || 'Untitled Transaction'}</h4>
                      <p>{tx.categoryName || tx.type} &bull; {tx.transactionDate}</p>
                    </div>
                    <div className={`fnb-tx-amt ${isIncome ? 'fnb-positive' : 'fnb-negative'}`}>
                      {isIncome ? '+' : '-'} {formatCurrency(tx.amount)}
                    </div>
                  </div>
                );
              })
            ) : (
              <p className="fnb-status-text">No transactions recorded for this period.</p>
            )}
          </div>

          <div className="fnb-panel">
            <div className="fnb-panel-header">
              <h2>Expense Breakdown</h2>
              <button onClick={() => navigate('/reports')}>Full Report</button>
            </div>

            {loading ? (
              <p className="fnb-status-text">Loading expense breakdown...</p>
            ) : breakdown.length > 0 ? (
              breakdown.map(([category, rawAmount]) => {
                const amount = Number(rawAmount) || 0;
                const pct = totalExpenses > 0 ? (amount / totalExpenses) * 100 : 0;
                return (
                  <div className="fnb-bar-item" key={category}>
                    <div className="fnb-bar-labels">
                      <span>{category}</span>
                      <span>{formatCurrency(amount)} ({pct.toFixed(1)}%)</span>
                    </div>
                    <div className="fnb-bar-track">
                      <div className="fnb-bar-fill" style={{ width: `${Math.min(100, Math.max(0, pct))}%` }} />
                    </div>
                  </div>
                );
              })
            ) : (
              <p className="fnb-status-text">No categorized expenses in this period.</p>
            )}
          </div>
        </section>
      </main>
    </div>
  );
};

export default Dashboard;