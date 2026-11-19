import React from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import './LandingPage.css';

const LandingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="landing-container">
      <nav className="landing-nav">
        <div onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
          <Logo />
        </div>

        <ul className="landing-nav-links">
          <li onClick={() => navigate('/')}>Overview</li>
          <li onClick={() => navigate('/about')}>About</li>
          <li onClick={() => navigate('/help')}>Help & FAQ</li>
          <li onClick={() => navigate('/contact')}>Contact</li>
        </ul>

        <div className="landing-nav-actions">
          <button className="landing-btn-outline" onClick={() => navigate('/login')}>
            Sign In
          </button>
          <button className="landing-btn-gold" onClick={() => navigate('/signup')}>
            Get Started
          </button>
        </div>
      </nav>

      <header className="landing-hero">
        <div className="landing-hero-ambient" />
        <div className="landing-hero-content">
          <span className="landing-hero-tag">Precision Financial Engineering</span>
          <h1>Command Your Business Cash Flow With Clarity.</h1>
          <p>
            Built for South African entrepreneurs and growing enterprises. Real-time revenue
            intelligence, automated margin tracking, and conversational financial forecasting.
          </p>
          <div className="landing-hero-cta">
            <button className="landing-btn-gold landing-btn-lg" onClick={() => navigate('/signup')}>
              Launch Platform
            </button>
            <button className="landing-btn-outline landing-btn-lg" onClick={() => navigate('/about')}>
              Explore Architecture
            </button>
          </div>
        </div>
      </header>

      <section className="landing-specs-bar">
        <div className="landing-spec-item">
          <h4>100%</h4>
          <p>BigDecimal Precision</p>
        </div>
        <div className="landing-spec-item">
          <h4>&lt; 200ms</h4>
          <p>Real-Time Ledger Sync</p>
        </div>
        <div className="landing-spec-item">
          <h4>ZAR / R</h4>
          <p>Native Currency Engine</p>
        </div>
        <div className="landing-spec-item">
          <h4>24/7</h4>
          <p>Gemini Financial Analyst</p>
        </div>
      </section>

      <section className="landing-showcase">
        <div className="landing-section-header">
          <span>Core Capabilities</span>
          <h2>Engineered For Executive Decisions</h2>
        </div>

        <div className="landing-grid-3">
          <div className="landing-showcase-card">
            <div className="landing-card-number">01 // CASH FLOW</div>
            <h3>Live Liquidity Telemetry</h3>
            <p>
              Track opening balances, net cash movements, and closing positions across every
              reporting period without spreadsheet delays.
            </p>
          </div>

          <div className="landing-showcase-card">
            <div className="landing-card-number">02 // MARGINS</div>
            <h3>Automated Cost Classification</h3>
            <p>
              Separate fixed overheads from variable expenses instantly to monitor gross and net
              profit margins in real time.
            </p>
          </div>

          <div className="landing-showcase-card">
            <div className="landing-card-number">03 // AI ANALYST</div>
            <h3>Conversational Intelligence</h3>
            <p>
              Query your ledger in plain English to uncover spending anomalies, tax liabilities,
              and three-month revenue projections.
            </p>
          </div>
        </div>
      </section>

      <footer className="landing-footer">
        <div>&copy; 2026 Clearside Pay. All rights reserved.</div>
        <div className="landing-footer-links">
          <span onClick={() => navigate('/about')}>About</span>
          <span onClick={() => navigate('/help')}>Help & FAQ</span>
          <span onClick={() => navigate('/contact')}>Contact</span>
          <a href="mailto:support.clearsidepay@gmail.com">support.clearsidepay@gmail.com</a>
        </div>
        <div>Johannesburg &bull; Gauteng &bull; South Africa</div>
      </footer>
    </div>
  );
};

export default LandingPage;