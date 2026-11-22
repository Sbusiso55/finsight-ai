import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import Reveal from '../components/Reveal';
import { SplitTitle, Magnetic } from '../components/fx/Fx';
import { PrefToggles } from '../components/fx/Prefs';
import './LandingPage.css';
import './LandingCinematic.css';
import './AboutStory.css';
import './Pricing.css';

const MONTHLY = 129;

const DURATIONS = [
  { months: 1, label: '1 month', price: 129 },
  { months: 3, label: '3 months', price: 349 },
  { months: 6, label: '6 months', price: 659 },
  { months: 12, label: '12 months', price: 1199, best: true },
];

const FREE_FEATURES = [
  '1 business',
  'Add and view transactions',
  'Dashboard, cash position and margin',
  'Basic expense breakdown',
  'Last 3 months of history (older records are kept, never deleted)',
  '5 Ask AI questions a month',
];

const PRO_FEATURES = [
  'Everything in Free',
  'Unlimited businesses and full history',
  'Full reports and forecasting',
  'Exports to PDF and Excel',
  'Unlimited Ask AI questions',
  'Cash Runway, Owner\u2019s Pay and Funding Pack (coming soon)',
  'Stock scanning and live card payments (coming soon)',
];

const TRIAL_POINTS = [
  ['30 days of Pro, free', 'Every new account gets every Pro feature for 30 days.'],
  ['No card needed', 'We never ask for card details to start. Nothing is ever charged automatically.'],
  ['Free plan forever', 'After the trial you keep the free plan for as long as you like. Your data always stays safe.'],
];

const FAQS = [
  ['Will I be charged when the trial ends?', 'No. You only pay if you choose a plan yourself. If you do nothing, you simply move to the free plan.'],
  ['What happens to my data on the free plan?', 'Nothing is deleted. Older records are kept safely and come back the moment you upgrade.'],
  ['Can I cancel?', 'Yes. Pro plans do not renew without your permission, and you can stop at any time.'],
  ['What currency are prices in?', 'Prices are currently shown in South African Rand, our first launch market.'],
];

const rand = (n) => `R${n.toLocaleString('en-ZA')}`;

const Pricing = () => {
  const navigate = useNavigate();
  const [months, setMonths] = useState(12);
  const [notice, setNotice] = useState('');
  const signedIn = !!localStorage.getItem('token');

  const plan = DURATIONS.find((d) => d.months === months);
  const perMonth = plan.price / plan.months;
  const saving = MONTHLY * plan.months - plan.price;

  const choosePro = () => {
    if (!signedIn) {
      navigate('/signup');
      return;
    }
    setNotice('Payments open soon. Until then your current plan carries on as normal, and you will not be charged.');
  };

  return (
    <div className="ab-page cs-studio">
      <nav className="landing-nav cine-nav">
        <div onClick={() => navigate('/')} style={{ cursor: 'pointer' }}><Logo /></div>
        <ul className="landing-nav-links">
          <li onClick={() => navigate('/')}>Home</li>
          <li onClick={() => navigate('/about')}>About</li>
          <li onClick={() => navigate('/help')}>Help & FAQ</li>
        </ul>
        <div className="landing-nav-actions">
          <PrefToggles />
          {signedIn ? (
            <button className="landing-btn-outline" onClick={() => navigate('/dashboard')}>Dashboard</button>
          ) : (
            <button className="landing-btn-outline" onClick={() => navigate('/login')}>Sign In</button>
          )}
        </div>
      </nav>

      <section className="ab-section pr-hero">
        <Reveal as="span" className="ab-tag">Pricing</Reveal>
        <SplitTitle as="h1" text="Simple plans. No surprises." />
        <Reveal as="p" className="ab-lead" delay={300}>
          Start with 30 days of Pro, free. After that, keep the free plan forever or upgrade
          when your business is ready.
        </Reveal>
      </section>

      <section className="ab-section pr-durations-wrap">
        <div className="pr-durations" role="tablist" aria-label="Plan length">
          {DURATIONS.map((d) => (
            <button
              key={d.months}
              type="button"
              role="tab"
              aria-selected={months === d.months}
              className={`pr-duration ${months === d.months ? 'is-active' : ''}`}
              onClick={() => { setMonths(d.months); setNotice(''); }}
            >
              {d.label}
              {d.best && <span className="pr-best">Best value</span>}
            </button>
          ))}
        </div>
      </section>

      <section className="ab-section pr-plans">
        <Reveal className="pr-card cs-metal">
          <span className="ab-num">Free</span>
          <div className="pr-price"><strong>R0</strong><span>forever</span></div>
          <p className="pr-sub">Everything you need to see your money clearly.</p>
          <ul className="pr-list">
            {FREE_FEATURES.map((f) => <li key={f}>{f}</li>)}
          </ul>
          <button
            className="landing-btn-outline landing-btn-lg pr-cta"
            onClick={() => navigate(signedIn ? '/dashboard' : '/signup')}
          >
            {signedIn ? 'Go to dashboard' : 'Start for free'}
          </button>
        </Reveal>

        <Reveal delay={150} className="pr-card pr-card-pro cs-metal">
          <span className="ab-num">Pro</span>
          <div className="pr-price">
            <strong>{rand(plan.price)}</strong>
            <span>for {plan.label}</span>
          </div>
          <p className="pr-sub">
            {plan.months === 1
              ? 'Billed once. No automatic renewal.'
              : `${rand(Math.round(perMonth))} a month. You save ${rand(saving)}.`}
          </p>
          <ul className="pr-list">
            {PRO_FEATURES.map((f) => <li key={f}>{f}</li>)}
          </ul>
          <Magnetic>
            <button className="landing-btn-gold landing-btn-lg pr-cta" onClick={choosePro}>
              {signedIn ? `Choose ${plan.label}` : 'Start 30-day free trial'}
            </button>
          </Magnetic>
          {notice && <p className="pr-notice">{notice}</p>}
        </Reveal>
      </section>

      <section className="ab-section">
        <Reveal as="span" className="ab-tag">How the trial works</Reveal>
        <div className="ab-grid-3">
          {TRIAL_POINTS.map(([title, text], i) => (
            <Reveal key={title} delay={i * 150} className="ab-promise">
              <h3>{title}</h3>
              <p>{text}</p>
            </Reveal>
          ))}
        </div>
      </section>

      <section className="ab-section pr-faq">
        <Reveal as="span" className="ab-tag">Questions</Reveal>
        {FAQS.map(([q, a]) => (
          <Reveal key={q} className="ab-promise" style={{ marginTop: 28 }}>
            <h3>{q}</h3>
            <p>{a}</p>
          </Reveal>
        ))}
      </section>

      <footer className="landing-footer" style={{ marginTop: 120 }}>
        <div>&copy; 2026 Clearside Pay. All rights reserved.</div>
        <div className="landing-footer-links">
          <span onClick={() => navigate('/about')}>About</span>
          <span onClick={() => navigate('/help')}>Help & FAQ</span>
          <span onClick={() => navigate('/contact')}>Contact</span>
          <span onClick={() => navigate('/privacy')}>Privacy</span>
        </div>
        <div>Johannesburg &bull; Gauteng &bull; South Africa</div>
      </footer>
    </div>
  );
};

export default Pricing;