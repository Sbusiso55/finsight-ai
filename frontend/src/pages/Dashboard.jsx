import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

const Dashboard = () => {
    const navigate = useNavigate();
    const [businessData, setBusinessData] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [monthlySummary, setMonthlySummary] = useState(null);
    const [aiQuestion, setAiQuestion] = useState('');
    const [aiResponse, setAiResponse] = useState('');
    const [loading, setLoading] = useState(true);
    const [aiLoading, setAiLoading] = useState(false);
    const [activeTab, setActiveTab] = useState('overview');

    const token = localStorage.getItem('token');

    useEffect(() => {
        if (!token) {
            navigate('/login');
            return;
        }
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            const headers = {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            };

            const [bizRes, txnRes, summaryRes] = await Promise.all([
                fetch('/api/business', { headers }),
                fetch('/api/transactions?page=0&size=10', { headers }),
                fetch('/api/monthly-summary', { headers })
            ]);

            if (bizRes.ok) {
                const bizData = await bizRes.json();
                setBusinessData(bizData);
            }
            if (txnRes.ok) {
                const txnData = await txnRes.json();
                setTransactions(txnData.content || txnData);
            }
            if (summaryRes.ok) {
                const summaryData = await summaryRes.json();
                setMonthlySummary(summaryData);
            }
        } catch (error) {
            console.error('Failed to fetch dashboard data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAskAI = async (e) => {
        e.preventDefault();
        if (!aiQuestion.trim()) return;
        setAiLoading(true);
        try {
            const response = await fetch('/api/ai/ask', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ question: aiQuestion })
            });
            if (response.ok) {
                const data = await response.json();
                setAiResponse(data.answer || data.response || JSON.stringify(data));
            } else {
                setAiResponse('Something went wrong. Please try again.');
            }
        } catch (error) {
            setAiResponse('Could not connect to the AI service.');
        } finally {
            setAiLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    const formatCurrency = (amount) => {
        if (!amount) return 'R0.00';
        return `R${parseFloat(amount).toLocaleString('en-ZA', { minimumFractionDigits: 2 })}`;
    };

    if (loading) {
        return (
            <div className="dashboard-loading">
                <div className="loading-spinner"></div>
                <p>Loading your financial data...</p>
            </div>
        );
    }

    return (
        <div className="dashboard-container">
            <aside className="sidebar">
                <div className="sidebar-logo">FinSight</div>
                <nav className="sidebar-nav">
                    <button
                        className={`nav-item ${activeTab === 'overview' ? 'active' : ''}`}
                        onClick={() => setActiveTab('overview')}
                    >
                        📊 Overview
                    </button>
                    <button
                        className={`nav-item ${activeTab === 'transactions' ? 'active' : ''}`}
                        onClick={() => setActiveTab('transactions')}
                    >
                        💳 Transactions
                    </button>
                    <button
                        className={`nav-item ${activeTab === 'reports' ? 'active' : ''}`}
                        onClick={() => setActiveTab('reports')}
                    >
                        📋 Reports
                    </button>
                    <button
                        className={`nav-item ${activeTab === 'ai' ? 'active' : ''}`}
                        onClick={() => setActiveTab('ai')}
                    >
                        💬 Ask AI
                    </button>
                    <button
                        className={`nav-item ${activeTab === 'settings' ? 'active' : ''}`}
                        onClick={() => setActiveTab('settings')}
                    >
                        ⚙️ Settings
                    </button>
                </nav>
                <button className="logout-btn" onClick={handleLogout}>
                    Logout
                </button>
            </aside>

            <main className="dashboard-main">
                {activeTab === 'overview' && (
                    <div className="overview-tab">
                        <div className="dashboard-header">
                            <h1>Financial Overview</h1>
                            <p>Welcome back. Here's how your business is doing.</p>
                        </div>

                        <div className="metrics-grid">
                            <div className="metric-card revenue">
                                <p className="metric-label">Total Revenue</p>
                                <h2 className="metric-value">
                                    {formatCurrency(monthlySummary?.totalRevenue)}
                                </h2>
                                <span className="metric-change positive">
                                    {monthlySummary?.revenueGrowth
                                        ? `+${monthlySummary.revenueGrowth}%`
                                        : 'No data yet'}
                                </span>
                            </div>
                            <div className="metric-card expenses">
                                <p className="metric-label">Total Expenses</p>
                                <h2 className="metric-value">
                                    {formatCurrency(monthlySummary?.totalExpenses)}
                                </h2>
                                <span className="metric-change">
                                    {monthlySummary?.expenseToRevenueRatio
                                        ? `${monthlySummary.expenseToRevenueRatio}% of revenue`
                                        : 'No data yet'}
                                </span>
                            </div>
                            <div className="metric-card profit">
                                <p className="metric-label">Net Profit</p>
                                <h2 className="metric-value">
                                    {formatCurrency(monthlySummary?.netProfit)}
                                </h2>
                                <span className="metric-change positive">
                                    {monthlySummary?.netMargin
                                        ? `${monthlySummary.netMargin}% margin`
                                        : 'No data yet'}
                                </span>
                            </div>
                            <div className="metric-card cashflow">
                                <p className="metric-label">Cash Flow</p>
                                <h2 className="metric-value">
                                    {formatCurrency(monthlySummary?.netCashFlow)}
                                </h2>
                                <span className="metric-change positive">
                                    {monthlySummary?.closingBalance
                                        ? `Balance: ${formatCurrency(monthlySummary.closingBalance)}`
                                        : 'No data yet'}
                                </span>
                            </div>
                        </div>

                        <div className="recent-transactions">
                            <h3>Recent Transactions</h3>
                            <div className="transactions-table">
                                <div className="table-header">
                                    <span>Description</span>
                                    <span>Category</span>
                                    <span>Amount</span>
                                    <span>Date</span>
                                </div>
                                {transactions.length > 0 ? (
                                    transactions.slice(0, 5).map((txn, index) => (
                                        <div key={index} className="table-row">
                                            <span>{txn.description || 'N/A'}</span>
                                            <span>{txn.category || 'Uncategorized'}</span>
                                            <span className={txn.type === 'INCOME' ? 'amount-positive' : 'amount-negative'}>
                                                {txn.type === 'INCOME' ? '+' : '-'}{formatCurrency(txn.amount)}
                                            </span>
                                            <span>{txn.date || 'N/A'}</span>
                                        </div>
                                    ))
                                ) : (
                                    <div className="no-data">No transactions yet. Add your first transaction to get started.</div>
                                )}
                            </div>
                        </div>
                    </div>
                )}

                {activeTab === 'transactions' && (
                    <div className="transactions-tab">
                        <div className="dashboard-header">
                            <h1>Transactions</h1>
                            <button className="add-btn">+ Add Transaction</button>
                        </div>
                        <div className="transactions-table full">
                            <div className="table-header">
                                <span>Description</span>
                                <span>Category</span>
                                <span>Type</span>
                                <span>Amount</span>
                                <span>Date</span>
                            </div>
                            {transactions.length > 0 ? (
                                transactions.map((txn, index) => (
                                    <div key={index} className="table-row">
                                        <span>{txn.description || 'N/A'}</span>
                                        <span>{txn.category || 'Uncategorized'}</span>
                                        <span className={`type-badge ${txn.type === 'INCOME' ? 'income' : 'expense'}`}>
                                            {txn.type || 'N/A'}
                                        </span>
                                        <span className={txn.type === 'INCOME' ? 'amount-positive' : 'amount-negative'}>
                                            {txn.type === 'INCOME' ? '+' : '-'}{formatCurrency(txn.amount)}
                                        </span>
                                        <span>{txn.date || 'N/A'}</span>
                                    </div>
                                ))
                            ) : (
                                <div className="no-data">No transactions found.</div>
                            )}
                        </div>
                    </div>
                )}

                {activeTab === 'reports' && (
                    <div className="reports-tab">
                        <div className="dashboard-header">
                            <h1>Monthly Reports</h1>
                            <p>Your financial summary and insights.</p>
                        </div>
                        <div className="report-cards">
                            <div className="report-card">
                                <h3>Revenue Summary</h3>
                                <div className="report-item">
                                    <span>Total Revenue</span>
                                    <span>{formatCurrency(monthlySummary?.totalRevenue)}</span>
                                </div>
                                <div className="report-item">
                                    <span>Revenue Growth</span>
                                    <span>{monthlySummary?.revenueGrowth ? `${monthlySummary.revenueGrowth}%` : 'N/A'}</span>
                                </div>
                            </div>
                            <div className="report-card">
                                <h3>Expense Summary</h3>
                                <div className="report-item">
                                    <span>Total Expenses</span>
                                    <span>{formatCurrency(monthlySummary?.totalExpenses)}</span>
                                </div>
                                <div className="report-item">
                                    <span>Expense to Revenue Ratio</span>
                                    <span>{monthlySummary?.expenseToRevenueRatio ? `${monthlySummary.expenseToRevenueRatio}%` : 'N/A'}</span>
                                </div>
                            </div>
                            <div className="report-card">
                                <h3>Profit Margins</h3>
                                <div className="report-item">
                                    <span>Gross Margin</span>
                                    <span>{monthlySummary?.grossMargin ? `${monthlySummary.grossMargin}%` : 'N/A'}</span>
                                </div>
                                <div className="report-item">
                                    <span>Net Margin</span>
                                    <span>{monthlySummary?.netMargin ? `${monthlySummary.netMargin}%` : 'N/A'}</span>
                                </div>
                            </div>
                            <div className="report-card">
                                <h3>Cash Flow</h3>
                                <div className="report-item">
                                    <span>Net Cash Flow</span>
                                    <span>{formatCurrency(monthlySummary?.netCashFlow)}</span>
                                </div>
                                <div className="report-item">
                                    <span>Closing Balance</span>
                                    <span>{formatCurrency(monthlySummary?.closingBalance)}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {activeTab === 'ai' && (
                    <div className="ai-tab">
                        <div className="dashboard-header">
                            <h1>Ask FinSight AI</h1>
                            <p>Ask any question about your finances and get instant AI-powered insights.</p>
                        </div>
                        <form className="ai-form" onSubmit={handleAskAI}>
                            <input
                                type="text"
                                value={aiQuestion}
                                onChange={(e) => setAiQuestion(e.target.value)}
                                placeholder="e.g. How is my business doing this month?"
                                className="ai-input"
                            />
                            <button type="submit" className="ai-submit" disabled={aiLoading}>
                                {aiLoading ? 'Thinking...' : 'Ask'}
                            </button>
                        </form>
                        {aiResponse && (
                            <div className="ai-response">
                                <h3>FinSight Says:</h3>
                                <p>{aiResponse}</p>
                            </div>
                        )}
                    </div>
                )}

                {activeTab === 'settings' && (
                    <div className="settings-tab">
                        <div className="dashboard-header">
                            <h1>Settings</h1>
                            <p>Manage your account and business profile.</p>
                        </div>
                        <div className="settings-card">
                            <h3>Business Profile</h3>
                            <div className="settings-item">
                                <span>Business Name</span>
                                <span>{businessData?.name || 'Not set'}</span>
                            </div>
                            <div className="settings-item">
                                <span>Industry</span>
                                <span>{businessData?.industry || 'Not set'}</span>
                            </div>
                        </div>
                    </div>
                )}
            </main>
        </div>
    );
};

export default Dashboard;
