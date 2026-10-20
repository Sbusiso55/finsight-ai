import React from 'react';
import { Link } from 'react-router-dom';
import './LandingPage.css';

const LandingPage = () => {
    return (
        <div className="landing-container">
            <nav className="landing-nav">
                <div className="nav-logo">FinSight</div>
                <div className="nav-links">
                    <a href="#features">Features</a>
                    <a href="#how-it-works">How It Works</a>
                    <a href="#about">About</a>
                    <Link to="/login" className="nav-login">Sign In</Link>
                    <Link to="/signup" className="nav-cta">Get Started</Link>
                </div>
            </nav>

            <section className="hero-section">
                <div className="hero-content">
                    <h1>Financial Intelligence,<br />Powered By AI</h1>
                    <p className="hero-subtitle">
                        FinSight helps small businesses and individuals track income,
                        expenses, and transactions, then delivers real-time analysis,
                        forecasting, and actionable insights through a conversational AI interface.
                    </p>
                    <div className="hero-buttons">
                        <Link to="/signup" className="btn-primary">Start For Free</Link>
                        <a href="#features" className="btn-secondary">See Features</a>
                    </div>
                </div>
            </section>

            <section className="stats-section">
                <div className="stat-item">
                    <h3>Real-Time</h3>
                    <p>Financial Tracking</p>
                </div>
                <div className="stat-item">
                    <h3>AI-Powered</h3>
                    <p>Insights & Forecasting</p>
                </div>
                <div className="stat-item">
                    <h3>Secure</h3>
                    <p>Bank-Level Encryption</p>
                </div>
                <div className="stat-item">
                    <h3>Simple</h3>
                    <p>No Accounting Degree Needed</p>
                </div>
            </section>

            <section id="features" className="features-section">
                <h2>What FinSight Does For You</h2>
                <div className="features-grid">
                    <div className="feature-card">
                        <div className="feature-icon">📊</div>
                        <h3>Revenue Tracking</h3>
                        <p>Track every transaction, see revenue growth over time, and understand where your money comes from.</p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">💰</div>
                        <h3>Expense Analysis</h3>
                        <p>Break down expenses by category, identify fixed vs variable costs, and find where you can cut spending.</p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">📈</div>
                        <h3>Profit Margins</h3>
                        <p>Calculate gross and net profit margins automatically and track how your profitability changes month to month.</p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">🔮</div>
                        <h3>AI Forecasting</h3>
                        <p>Get revenue, expense, and profit forecasts based on your historical data with confidence ranges.</p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">💬</div>
                        <h3>Ask Your Finances</h3>
                        <p>Ask questions like "How is my business doing?" and get conversational AI-powered answers based on your real data.</p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">📋</div>
                        <h3>Monthly Reports</h3>
                        <p>Generate detailed monthly summaries with cash flow, profit margins, expense ratios, and growth trends.</p>
                    </div>
                </div>
            </section>

            <section id="how-it-works" className="how-section">
                <h2>How It Works</h2>
                <div className="steps-grid">
                    <div className="step-card">
                        <div className="step-number">01</div>
                        <h3>Sign Up</h3>
                        <p>Create your account and set up your business profile in under 2 minutes.</p>
                    </div>
                    <div className="step-card">
                        <div className="step-number">02</div>
                        <h3>Add Transactions</h3>
                        <p>Input your income and expenses manually or connect your accounts.</p>
                    </div>
                    <div className="step-card">
                        <div className="step-number">03</div>
                        <h3>Get Insights</h3>
                        <p>FinSight analyses your data and delivers real-time financial intelligence and forecasts.</p>
                    </div>
                </div>
            </section>

            <section id="about" className="about-section">
                <h2>Built For Small Businesses In South Africa</h2>
                <p>
                    FinSight was built by a team of developers who understand that most small
                    business owners don't have time to sit with spreadsheets or hire accountants.
                    We built an AI that does the heavy lifting so you can focus on growing your business.
                </p>
            </section>

            <footer className="landing-footer">
                <div className="footer-content">
                    <div className="footer-brand">
                        <h3>FinSight</h3>
                        <p>AI-Powered Financial Intelligence</p>
                    </div>
                    <div className="footer-links">
                        <a href="#features">Features</a>
                        <a href="#how-it-works">How It Works</a>
                        <a href="#about">About</a>
                    </div>
                </div>
                <div className="footer-bottom">
                    <p>2026 FinSight. All rights reserved.</p>
                </div>
            </footer>
        </div>
    );
};

export default LandingPage;
