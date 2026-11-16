import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './Reports.css';

const Reports = () => {
  const [businesses, setBusinesses] = useState([]);
  const [selectedBusinessId, setSelectedBusinessId] = useState(null);
  const [reportData, setReportData] = useState(null);
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
    const loadReport = async () => {
      setLoading(true);
      const today = new Date();
      const from = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-01`;
      const to = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

      try {
        const [rev, exp, margin] = await Promise.all([
          api.get(`/api/revenue/total?businessId=${selectedBusinessId}&from=${from}&to=${to}`),
          api.get(`/api/expenses/total?businessId=${selectedBusinessId}&from=${from}&to=${to}`),
          api.get(`/api/profit-margin/net?businessId=${selectedBusinessId}&from=${from}&to=${to}`),
        ]);

        if (!ignore) {
          setReportData({
            revenue: rev.data ?? 0,
            expenses: exp.data ?? 0,
            netMargin: margin.data ?? 0,
          });
        }
      } catch (err) {
        console.error('Failed to load report:', err);
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    loadReport();
    return () => { ignore = true; };
  }, [selectedBusinessId]);

  const formatCurrency = (amount) => {
    const num = Number(amount) || 0;
    return `R ${num.toLocaleString('en-ZA', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  return (
    <div className="rep-layout">
      <Navbar />
      <main className="rep-container">
        <div className="rep-header-row">
          <h1>Financial Reports & Margins</h1>
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
        </div>

        {loading ? (
          <p className="rep-msg">Generating financial telemetry report...</p>
        ) : (
          <div className="rep-grid">
            <div className="rep-card">
              <span>Total Revenue</span>
              <h3 className="rep-pos">{formatCurrency(reportData?.revenue)}</h3>
            </div>
            <div className="rep-card">
              <span>Total Expenses</span>
              <h3 className="rep-neg">{formatCurrency(reportData?.expenses)}</h3>
            </div>
            <div className="rep-card">
              <span>Net Profit Margin</span>
              <h3>{Number(reportData?.netMargin || 0).toFixed(2)}%</h3>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export default Reports;