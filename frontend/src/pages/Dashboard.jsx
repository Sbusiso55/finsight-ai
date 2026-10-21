import React from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import './Dashboard.css';

const Dashboard = () => {
    const navigate = useNavigate();

    return (
        <div className="dashboard-wrapper">
            <Navbar />
            <div className="dashboard-content">
                <div className="dashboard-header">
                    <div>
                        <p className="dashboard-greeting">Good evening,</p>
                        <h1 className="dashboard-title">Welcome back.</h1>
                    </div>
                    <button className="dashboard-add-btn" onClick={() => navigate('/add-transaction')}>
                        + Add Transaction
                    </button>
                </div>

                <div className="dashboard-cards">
                    <div className="dash-card">
                        <p className="dash-card-label">Total Revenue</p>
                        <h2 className="dash-card-value">R30,000</h2>
                        <p className="dash-card-change positive">+20% from last month</p>
                    </div>
                    <div className="dash-card">
                        <p className="dash-card-label">Total Expenses</p>
                        <h2 className="dash-card-value">R24,800</h2>
                        <p className="dash-card-change negative">+18.1% from last month</p>
                    </div>
                    <div className="dash-card">
                        <p className="dash-card-label">Net Profit</p>
                        <h2 className="dash-card-value">R5,200</h2>
                        <p className="dash-card-change positive">17.33% margin</p>
                    </div>
                    <div className="dash-card">
                        <p className="dash-card-label">Cash Balance</p>
                        <h2 className="dash-card-value">R20,200</h2>
                        <p className="dash-card-change positive">Positive trend</p>
                    </div>
                </div>

                <div className="dashboard-sections">
                    <div className="dash-section">
                        <h3 className="dash-section-title">Recent Transactions</h3>
                        <div className="dash-table">
                            <div className="dash-row header">
                                <span>Description</span>
                                <span>Amount</span>
                                <span>Type</span>
                                <span>Date</span>
                            </div>
                            <div className="dash-row">
                                <span>Customer 001 Payment</span>
                                <span className="positive">+R8,000</span>
                                <span>Income</span>
                                <span>Mar 5</span>
                            </div>
                            <div className="dash-row">
                                <span>Staff Wages</span>
                                <span className="negative">-R9,000</span>
                                <span>Expense</span>
                                <span>Mar 1</span>
                            </div>
                            <div className="dash-row">
                                <span>Anita's Retail</span>
                                <span className="positive">+R12,500</span>
                                <span>Income</span>
                                <span>Mar 12</span>
                            </div>
                        </div>
                    </div>

                    <div className="dash-section">
                        <h3 className="dash-section-title">Expense Breakdown</h3>
                        <div className="expense-bars">
                            <div className="expense-bar-item">
                                <div className="expense-bar-info">
                                    <span>Staff Wages</span>
                                    <span>R9,000 (36.3%)</span>
                                </div>
                                <div className="expense-bar-track">
                                    <div className="expense-bar-fill" style={{width: '36.3%'}}></div>
                                </div>
                            </div>
                            <div className="expense-bar-item">
                                <div className="expense-bar-info">
                                    <span>Rent</span>
                                    <span>R6,000 (24.2%)</span>
                                </div>
                                <div className="expense-bar-track">
                                    <div className="expense-bar-fill" style={{width: '24.2%'}}></div>
                                </div>
                            </div>
                            <div className="expense-bar-item">
                                <div className="expense-bar-info">
                                    <span>Supplies</span>
                                    <span>R4,800 (19.4%)</span>
                                </div>
                                <div className="expense-bar-track">
                                    <div className="expense-bar-fill" style={{width: '19.4%'}}></div>
                                </div>
                            </div>
                            <div className="expense-bar-item">
                                <div className="expense-bar-info">
                                    <span>Marketing</span>
                                    <span>R3,000 (12.1%)</span>
                                </div>
                                <div className="expense-bar-track">
                                    <div className="expense-bar-fill" style={{width: '12.1%'}}></div>
                                </div>
                            </div>
                            <div className="expense-bar-item">
                                <div className="expense-bar-info">
                                    <span>Utilities</span>
                                    <span>R2,000 (8.1%)</span>
                                </div>
                                <div className="expense-bar-track">
                                    <div className="expense-bar-fill" style={{width: '8.1%'}}></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
