import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './Profile.css';

const fmt = (d) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

const money = (n) =>
  `R ${(Number(n) || 0).toLocaleString('en-ZA', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

const Notifications = () => {
  const navigate = useNavigate();
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const build = async () => {
      const list = [];
      try {
        const bizRes = await api.get('/api/businesses/owner');
        const businesses = Array.isArray(bizRes.data) ? bizRes.data : [];

        if (businesses.length === 0) {
          list.push({ level: 'info', title: 'Finish setting up',
            text: 'Add your business to start tracking your finances.',
            action: { label: 'Set Up Business', to: '/business-setup' } });
        }

        for (const biz of businesses) {
          const today = new Date();
          const from = fmt(new Date(today.getFullYear(), today.getMonth(), 1));
          const q = `businessId=${biz.id}&from=${from}&to=${fmt(today)}`;

          const [rev, exp, brk, tx] = await Promise.allSettled([
            api.get(`/api/transactions/revenue?${q}`),
            api.get(`/api/transactions/expenses?${q}`),
            api.get(`/api/expenses/breakdown?${q}`),
            api.get(`/api/transactions?businessId=${biz.id}&page=0&size=1&sort=createdAt,desc`),
          ]);

          const revenue = rev.status === 'fulfilled' ? Number(rev.value.data) || 0 : 0;
          const expenses = exp.status === 'fulfilled' ? Number(exp.value.data) || 0 : 0;
          const breakdown = brk.status === 'fulfilled' ? brk.value.data || {} : {};
          const latest = tx.status === 'fulfilled' ? tx.value.data?.content?.[0] : null;

          if (expenses > revenue && expenses > 0) {
            list.push({ level: 'warning', title: `${biz.name}: spending more than earning`,
              text: `This month you've spent ${money(expenses)} against ${money(revenue)} of income.`,
              action: { label: 'View Reports', to: '/reports' } });
          }

          if (!latest) {
            list.push({ level: 'info', title: `${biz.name}: no transactions yet`,
              text: 'Record your first transaction to see your numbers.',
              action: { label: 'Add Transaction', to: '/add-transaction' } });
          } else if (latest.createdAt || latest.transactionDate) {
            const last = new Date(latest.createdAt || latest.transactionDate);
            const days = Math.floor((today - last) / 86400000);
            if (days >= 7) {
              list.push({ level: 'info', title: `${biz.name}: nothing recorded for ${days} days`,
                text: 'Keep your records up to date so your insights stay accurate.',
                action: { label: 'Add Transaction', to: '/add-transaction' } });
            }
          }

          Object.entries(breakdown).forEach(([cat, amt]) => {
            const pct = expenses > 0 ? (Number(amt) / expenses) * 100 : 0;
            if (pct >= 50 && Object.keys(breakdown).length > 1) {
              list.push({ level: 'warning', title: `${biz.name}: ${cat} is ${pct.toFixed(0)}% of expenses`,
                text: 'One category is taking most of your spending. Check whether that is expected.',
                action: { label: 'Ask AI', to: '/ask-ai' } });
            }
          });
        }
      } catch (err) {
        console.error('Failed to build notifications:', err);
        list.push({ level: 'warning', title: 'Could not load notifications', text: 'Please try again shortly.' });
      }
      setAlerts(list);
      setLoading(false);
    };
    build();
  }, []);

  return (
    <div className="pf-layout">
      <Navbar />
      <main className="pf-container">
        <header className="pf-header">
          <span className="pf-tag">Activity</span>
          <h1>Notifications</h1>
        </header>

        {loading && <div className="pf-card pf-muted">Checking your business...</div>}

        {!loading && alerts.length === 0 && (
          <div className="pf-card">
            <h3>You're all caught up.</h3>
            <p className="pf-muted">No alerts right now. Stock and payment alerts are coming soon.</p>
          </div>
        )}

        {alerts.map((a, i) => (
          <div className={`pf-card pf-alert ${a.level}`} key={i}>
            <div>
              <h3>{a.title}</h3>
              <p className="pf-muted">{a.text}</p>
            </div>
            {a.action && (
              <button className="pf-btn-outline" onClick={() => navigate(a.action.to)}>{a.action.label}</button>
            )}
          </div>
        ))}
      </main>
    </div>
  );
};

export default Notifications;