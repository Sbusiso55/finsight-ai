import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import './Reports.css';

const Reports = () => {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const [summary, setSummary] = useState(null);
    const [expenses, setExpenses] = useState(null);
    const [forecast, setForecast] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!token) {
            navigate('/login');
            return;
        }
        fetchReportData();
    }, []);

    const fetchReportData = async () => {
        const headers = {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };

        try {
            const [summaryRes, expenseRes, forecastRes] = await Promise.all([
                fetch('/api/monthly-summary', { headers }),
                fetch('/api/expenses/breakdown', { headers }),
                fetch('/api/forecast', { headers })
            ]);

            if (summaryRes.ok) setSummary(await summaryRes.json());
            if (expenseRes.ok) setExpenses(await expenseRes.json());
            if (forecastRes.ok) setForecast(await forecastRes.json());
        } catch (error) {
            console.error('Failed to fetch report data:', error);
        } finally {
            setLoading(false);
        }
    };

    const formatCurrency = (amount) => {
        if (!amount) return 'R0.00';
        return `R${parseFloat(amount).toLocaleString('en-ZA', { minimumFractionDigits: 2 })}`;
    };

    if (loading) {
        return (
            <div className="page-loading">
                <div className="loading-spinner"></div>
                <p>Loading reports...</p>
            </div>
        );
    }

    return (
        <div className="page-container">
            <Navbar />
            <main className="page-main">
                <div className="page-header">
                    <div>
                        <h1>Financial Reports</h1>
                        <p>Detailed breakdown of your business finances.</p>
                    </div>
                </div>

                <div className="reports-grid">
                    <div className="report-section">
                        <h2>Revenue Summary</h2>
                        <div className="report-block">
                            <div className="report-line">
                                <span>Total Revenue</span>
                                <span className="value">{formatCurrency(summary?.totalRevenue)}</span>
                            </div>
                            <div className="report-line">
                                <span>Revenue Growth</span>
                                <span className="value positive">{summary?.revenueGrowth ? `+${summary.revenueGrowth}%` : 'N/A'}</span>
                            </div>
                            <div className="report-line">
                                <span>Average Monthly Revenue</span>
                                <span className="value">{formatCurrency(summary?.avgMonthlyRevenue)}</span>
                            </div>
                        </div>
                    </div>

                    <div className="report-section">
                        <h2>Expense Summary</h2>
                        <div className="report-block">
                            <div className="report-line">
                                <span>Total Expenses</span>
                                <span className="value">{formatCurrency(summary?.totalExpenses)}</span>
                            </div>
                            <div className="report-line">
                                <span>Fixed Costs</span>
                                <span className="value">{formatCurrency(expenses?.fixedCosts)}</span>
                            </div>
                            <div className="report-line">
                                <span>Variable Costs</span>
                                <span className="value">{formatCurrency(expenses?.variableCosts)}</span>
                            </div>
                            <div className="report-line">
                                <span>Expense to Revenue Ratio</span>
                                <span className="value">{summary?.expenseToRevenueRatio ? `${summary.expenseToRevenueRatio}%` : 'N/A'}</span>
                            </div>
                        </div>
                    </div>

                    <div className="report-section">
                        <h2>Profit Margins</h2>
                        <div className="report-block">
                            <div className="report-line">
                                <span>Gross Profit</span>
                                <span className="value">{formatCurrency(summary?.grossProfit)}</span>
                            </div>
                            <div className="report-line">
                                <span>Gross Margin</span>
                                <span className="value positive">{summary?.grossMargin ? `${summary.grossMargin}%` : 'N/A'}</span>
                            </div>
                            <div className="report-line">
                                <span>Net Profit</span>
                                <span className="value">{formatCurrency(summary?.netProfit)}</span>
                            </div>
                            <div className="report-line">
                                <span>Net Margin</span>
                                <span className="value positive">{summary?.netMargin ? `${summary.netMargin}%` : 'N/A'}</span>
                            </div>
                        </div>
                    </div>

                    <div className="report-section">
                        <h2>Cash Flow</h2>
                        <div className="report-block">
                            <div className="report-line">
                                <span>Opening Balance</span>
                                <span className="value">{formatCurrency(summary?.openingBalance)}</span>
                            </div>
                            <div className="report-line">
                                <span>Net Cash Flow</span>
                                <span className="value positive">{formatCurrency(summary?.netCashFlow)}</span>
                            </div>
                            <div className="report-line">
                                <span>Closing Balance</span>
                                <span className="value">{formatCurrency(summary?.closingBalance)}</span>
                            </div>
                        </div>
                    </div>

                    <div className="report-section full-width">
                        <h2>Forecast</h2>
                        <div className="report-block">
                            <div className="forecast-grid">
                                <div className="forecast-item">
                                    <span className="forecast-label">Projected Revenue</span>
                                    <span className="forecast-value">{formatCurrency(forecast?.projectedRevenue)}</span>
                                </div>
                                <div className="forecast-item">
                                    <span className="forecast-label">Projected Expenses</span>
                                    <span className="forecast-value">{formatCurrency(forecast?.projectedExpenses)}</span>
                                </div>
                                <div className="forecast-item">
                                    <span className="forecast-label">Projected Profit</span>
                                    <span className="forecast-value positive">{formatCurrency(forecast?.projectedProfit)}</span>
                                </div>
                            </div>
                            {forecast?.revenueRange && (
                                <div className="forecast-range">
                                    <p>Revenue Range: {formatCurrency(forecast.revenueRange.low)} — {formatCurrency(forecast.revenueRange.high)}</p>
                                    <p>Expense Range: {formatCurrency(forecast.expenseRange?.low)} — {formatCurrency(forecast.expenseRange?.high)}</p>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default Reports;
