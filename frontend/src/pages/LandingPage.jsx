import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './LandingPage.css';

const LandingPage = () => {
    const navigate = useNavigate();
    const [introPhase, setIntroPhase] = useState('dark');

    useEffect(() => {
        const timer1 = setTimeout(() => setIntroPhase('logo'), 400);
        const timer2 = setTimeout(() => setIntroPhase('tagline'), 1800);
        const timer3 = setTimeout(() => setIntroPhase('reveal'), 3200);
        const timer4 = setTimeout(() => setIntroPhase('done'), 4200);

        return () => {
            clearTimeout(timer1);
            clearTimeout(timer2);
            clearTimeout(timer3);
            clearTimeout(timer4);
        };
    }, []);

    return (
        <div className="landing-wrapper">
            {introPhase !== 'done' && (
                <div className={`intro-screen ${introPhase === 'reveal' ? 'intro-exit' : ''}`}>
                    <div className="intro-content">
                        <h1 className={`intro-logo ${introPhase === 'logo' || introPhase === 'tagline' || introPhase === 'reveal' ? 'visible' : ''}`}>
                            Fin<span className="intro-highlight">Sight</span>
                        </h1>
                        <p className={`intro-tagline ${introPhase === 'tagline' || introPhase === 'reveal' ? 'visible' : ''}`}>
                            Intelligence meets finance
                        </p>
                    </div>
                    <div className="intro-line-left"></div>
                    <div className="intro-line-right"></div>
                </div>
            )}

            <div className={`landing-container ${introPhase === 'done' ? 'landing-visible' : ''}`}>
                <div className="landing-glow-1"></div>
                <div className="landing-glow-2"></div>
                <div className="landing-glow-3"></div>

                <nav className="landing-nav">
                    <div className="nav-logo">
                        Fin<span className="highlight">Sight</span>
                    </div>
                    <div className="nav-links">
                        <button className="nav-btn" onClick={() => navigate('/login')}>Log In</button>
                        <button className="nav-btn primary" onClick={() => navigate('/signup')}>Get Started</button>
                    </div>
                </nav>

                <section className="hero-section">
                    <div className="hero-badge">AI-Powered Financial Intelligence</div>
                    <h1 className="hero-title">
                        Know your numbers.<br />
                        <span className="hero-gradient-text">Grow your business.</span>
                    </h1>
                    <p className="hero-subtitle">
                        FinSight gives small businesses the financial clarity they need —
                        real-time tracking, smart forecasting, and AI-driven insights
                        that turn raw numbers into confident decisions.
                    </p>
                    <div className="hero-buttons">
                        <button className="hero-btn primary" onClick={() => navigate('/signup')}>
                            Start Free
                        </button>
                        <button className="hero-btn secondary" onClick={() => navigate('/login')}>
                            View Demo
                        </button>
                    </div>
                </section>

                <section className="features-section">
                    <div className="features-header">
                        <p className="features-label">What FinSight Does</p>
                        <h2 className="features-title">Built for businesses<br />that move forward.</h2>
                    </div>

                    <div className="features-grid">
                        <div className="feature-item">
                            <div className="feature-number">01</div>
                            <div className="feature-line"></div>
                            <h3>Revenue Tracking</h3>
                            <p>Monitor every rand coming in. Real-time breakdowns, growth percentages, and transaction history at a glance.</p>
                        </div>

                        <div className="feature-item">
                            <div className="feature-number">02</div>
                            <div className="feature-line"></div>
                            <h3>Smart Forecasting</h3>
                            <p>AI analyses your patterns and predicts where your business is heading. Revenue, expenses, and profit — months ahead.</p>
                        </div>

                        <div className="feature-item">
                            <div className="feature-number">03</div>
                            <div className="feature-line"></div>
                            <h3>Ask AI Anything</h3>
                            <p>Have a conversation with your finances. Ask questions, get answers, make decisions backed by real data.</p>
                        </div>

                        <div className="feature-item">
                            <div className="feature-number">04</div>
                            <div className="feature-line"></div>
                            <h3>Expense Intelligence</h3>
                            <p>See where every rand goes. Fixed vs variable costs, category breakdowns, and trends that reveal what's eating your profit.</p>
                        </div>
                    </div>
                </section>

                <footer className="landing-footer">
                    <p>FinSight AI — Built for South African businesses</p>
                </footer>
            </div>
        </div>
    );
};

export default LandingPage;
