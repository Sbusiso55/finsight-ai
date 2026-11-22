import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import TiltCard from '../components/TiltCard';
import { CountUp } from '../components/fx/Fx';
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

const valueOf = (result, fallback) =>
  result.status === 'fulfilled' ? result.value.data ?? fallback : fallback;

const Money = ({ value }) => <CountUp to={Number(value) || 0} decimals={2} prefix="R " duration={900} />;

const SkeletonRows = ({ rows = 4 }) => (
  <>
    {Array.from({ length: rows }, (_, i) => (
      <div className="am-skel-row" key={i}>
        <div style={{ flex: 1 }}>
          <div className="am-skeleton" style={{ height: 14, width: '55%', marginBottom: 8 }} />
          <div className="am-skeleton" style={{ height: 10, width: '35%' }} />
        </div>
        <div className="am-skeleton" style={{ height: 14, width: 80 }} />
      </div>
    ))}
  </>
);

const Dashboard = () => {
  const navigate = useNavigate();

  const [user, setUser] = useState(null);
  const [businesses, setBusinesses] = useState([]);
  const [businessesLoaded, setBusinessesLoaded] = useState(false);
  const [selectedBusinessId, setSelectedBusinessId] = useState(null);
  const [selectedPeriod, setSelectedPeriod] = useState(() => toPeriodKey(new Date()));
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [txError, setTxError] = useState(false);
  const [barsReady, setBarsReady] = useState(false);

  useEffect(() => {
    let ignore = false;

    const loadUserAndBusinesses = async () => {
      const [userRes, bizRes] = await Promise.allSettled([
        api.get('/api/profile'),
        api.get('/api/businesses/owner'),
      ]);
      if (ignore) return;

      if (userRes.status === 'fulfilled') setUser(userRes.value.data);
      else console.error('Failed to load user:', userRes.reason);

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
      setBarsReady(false);
      setTxError(false);
      const { from, to } = getDateRange(selectedPeriod);
      const q = `businessId=${selectedBusinessId}&from=${from}&to=${to}`;

      const [revenueRes, expensesRes, breakdownRes, txRes] = await Promise.allSettled([
        api.get(`/api/transactions/revenue?${q}`),
        api.get(`/api/transactions/expenses?${q}`),
        api.get(`/api/expenses/breakdown?${q}`),
        api.get(`/api/transactions?businessId=${selectedBusinessId}&page=0&size=5&sort=createdAt,desc`),
      ]);
      if (ignore) return;

      [revenueRes, expensesRes, breakdownRes, txRes].forEach((r) => {
        if (r.status === 'rejected') console.error('Dashboard request failed:', r.reason);
      });

      setTxError(txRes.status === 'rejected');
      setData({
        revenue: valueOf(revenueRes, 0),
        expenses: valueOf(expensesRes, 0),
        expenseBreakdown: valueOf(breakdownRes, {}),
        transactions: txRes.status === 'fulfilled' ? txRes.value.data?.content ?? [] : [],
      });
      setLoading(false);
      requestAnimationFrame(() => requestAnimationFrame(() => { if (!ignore) setBarsReady(true); }));
    };

    loadFinancials();
    return () => { ignore = true; };
  }, [selectedBusinessId, selectedPeriod]);

  const currentBusiness = businesses.find((b) => b.id === selectedBusinessId);
  const revenue = Number(data?.revenue) || 0;
  const totalExpenses = Number(data?.expenses) || 0;
  const netProfitMargin = revenue > 0 ? ((revenue - totalExpenses) / revenue) * 100 : 0;
  const breakdown = Object.entries(data?.expenseBreakdown || {});
  const netCashPosition = (Number(currentBusiness?.openingBalance) || 0) + revenue - totalExpenses;
  const showSkeleton = loading || (selectedBusinessId && !data);

  return (
    <div className="fnb-dashboard-layout">
      <Navbar />

      <main className="fnb-dashboard-container">
        {businessesLoaded && businesses.length === 0 && (
          <div className="fnb-panel am-rise" style={{ marginBottom: 24 }}>
            <h2 style={{ fontSize: 17, margin: '0 0 8px 0' }}>Set up your business</h2>
            <p className="fnb-status-text">
              You haven't added a business yet. Add one to start tracking your finances.
            </p>
            <button
              onClick={() => navigate('/business-setup')}
              style={{
                marginTop: 14,
                background: '#1A1A1A',
                color: '#E5E5E5',
                border: 'none',
                padding: '10px 20px',
                borderRadius: 10,
                fontWeight: 700,
                cursor: 'pointer',
              }}
            >
              Set Up Business
            </button>
          </div>
        )}

        <header className="fnb-topbar am-rise am-d1">
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

        <TiltCard className="fnb-account-card am-rise am-d2">
          <div>
            <span className="fnb-account-tag">Net Business Cash Position</span>
            <div className="fnb-account-balance">
              {showSkeleton
                ? <div className="am-skeleton" style={{ height: 38, width: 220 }} />
                : <Money value={netCashPosition} />}
            </div>
            <p className="fnb-account-meta">
              Opening Balance: {formatCurrency(currentBusiness?.openingBalance)} &bull; Currency:{' '}
              {currentBusiness?.currency || 'ZAR'}
            </p>
          </div>
          <button className="fnb-primary-cta" onClick={() => navigate('/add-transaction')}>
            + Record Transaction
          </button>
        </TiltCard>

        <section className="fnb-quick-actions am-rise am-d3">
          <button className="fnb-action-tile am-lift" onClick={() => navigate('/add-transaction')}>
            <div className="fnb-action-icon">+</div>
            <span>Record Entry</span>
          </button>
          <button className="fnb-action-tile am-lift" onClick={() => navigate('/transactions')}>
            <div className="fnb-action-icon">&#8644;</div>
            <span>All Transactions</span>
          </button>
          <button className="fnb-action-tile am-lift" onClick={() => navigate('/ask-ai')}>
            <div className="fnb-action-icon">AI</div>
            <span>Ask Clearside Pay</span>
          </button>
          <button className="fnb-action-tile am-lift" onClick={() => navigate('/reports')}>
            <div className="fnb-action-icon">%</div>
            <span>Reports</span>
          </button>
        </section>

        <section className="fnb-metrics-grid am-rise am-d4">
          <div className="fnb-metric-card am-lift">
            <span>Period Revenue</span>
            <h3 className="fnb-positive">
              {showSkeleton ? <span className="am-skeleton" style={{ display: 'block', height: 24, width: 120 }} /> : <Money value={revenue} />}
            </h3>
          </div>
          <div className="fnb-metric-card am-lift">
            <span>Period Expenses</span>
            <h3 className="fnb-negative">
              {showSkeleton ? <span className="am-skeleton" style={{ display: 'block', height: 24, width: 120 }} /> : <Money value={totalExpenses} />}
            </h3>
          </div>
          <div className="fnb-metric-card am-lift">
            <span>Net Profit Margin</span>
            <h3>
              {showSkeleton
                ? <span className="am-skeleton" style={{ display: 'block', height: 24, width: 80 }} />
                : <CountUp to={netProfitMargin} decimals={2} suffix="%" duration={900} />}
            </h3>
          </div>
        </section>

        <section className="fnb-lower-grid am-rise am-d5">
          <div className="fnb-panel">
            <div className="fnb-panel-header">
              <h2>Recent Transactions</h2>
              <button onClick={() => navigate('/transactions')}>View All</button>
            </div>

            {showSkeleton ? (
              <SkeletonRows rows={4} />
            ) : txError ? (
              <p className="fnb-status-text">Unable to load transactions.</p>
            ) : data?.transactions?.length > 0 ? (
              data.transactions.map((tx, i) => {
                const isIncome = tx.type === 'INCOME';
                return (
                  <div className="fnb-tx-row am-rise" style={{ animationDelay: `${i * 0.06}s` }} key={tx.id}>
                    <div>
                      <h4>{tx.description || 'Untitled Transaction'}</h4>
                      <p>{tx.category?.name || tx.type} &bull; {tx.transactionDate}</p>
                    </div>
                    <div className={`fnb-tx-amt ${isIncome ? 'fnb-positive' : 'fnb-negative'}`}>
                      {isIncome ? '+' : '-'} {formatCurrency(tx.amount)}
                    </div>
                  </div>
                );
              })
            ) : (
              <p className="fnb-status-text">No transactions recorded yet.</p>
            )}
          </div>

          <div className="fnb-panel">
            <div className="fnb-panel-header">
              <h2>Expense Breakdown</h2>
              <button onClick={() => navigate('/reports')}>Full Report</button>
            </div>

            {showSkeleton ? (
              <SkeletonRows rows={3} />
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
                      <div
                        className="fnb-bar-fill am-bar"
                        style={{ width: barsReady ? `${Math.min(100, Math.max(0, pct))}%` : '0%' }}
                      />
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