import React from 'react';
import Navbar from '../components/Navbar';
import './Reports.css';

const Reports = () => {
    return (
        <div className="reports-wrapper">
            <Navbar />
            <div className="reports-content">
                <div className="reports-header">
                    <h1 className="reports-title">Financial Reports</h1>
                    <p className="reports-subtitle">Monthly summaries and performance insights.</p>
                </div>

                <div className="reports-grid">
                    <div className="report-card">
                        <div className="report-card-header">
                            <span className="report-month">March 2026</span>
                            <span className="report-badge">Latest</span>
                        </div>
                        <div className="report-stats">
                            <div className="report-stat">
                                <p className="report-stat-label">Revenue</p>
                                <p className="report-stat-value">R30,000</p>
                            </div>
                            <div className="report-stat">
                                <p className="report-stat-label">Expenses</p>
                                <p className="report-stat-value">R24,800</p>
                            </div>
                            <div className="report-stat">
                                <p className="report-stat-label">Profit</p>
                                <p className="report-stat-value positive">R5,200</p>
                            </div>
                            <div className="report-stat">
                                <p className="report-stat-label">Margin</p>
                                <p className="report-stat-value">17.33%</p>
                            </div>
                        </div>
                        <div className="report-insights">
                            <h4>Key Insights</h4>
                            <ul>
                                <li>Revenue grew 20% from February</li>
                                <li>Staff wages are the largest expense at 36.3%</li>
                                <li>Fixed costs make up 60% of total expenses</li>
                                <li>Revenue growth outpacing expense growth</li>
                            </ul>
                        </div>
                    </div>

                    <div className="report-card">
                        <div className="report-card-header">
                            <span className="report-month">Forecast — April 2026</span>
                        </div>
                        <div className="report-stats">
                            <div className="report-stat">
                                <p className="report-stat-label">Projected Revenue</p>
                                <p className="report-stat-value">R34,000</p>
                            </div>
                            <div className="report-stat">
                                <p className="report-stat-label">Projected Expenses</p>
                                <p className="report-stat-value">R27,700</p>
                            </div>
                            <div className="report-stat">
                                <p className="report-stat-label">Projected Profit</p>
                                <p className="report-stat-value positive">R6,300</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Reports;
