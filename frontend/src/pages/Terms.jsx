import React from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import Reveal from '../components/Reveal';
import { PrefToggles } from '../components/fx/Prefs';
import './LandingPage.css';
import './LandingCinematic.css';
import './AboutStory.css';

const SUPPORT = 'support.clearsidepay@gmail.com';
const UPDATED = '26 November 2026';

const SECTIONS = [
  ['About these terms',
    'These terms apply when you use Clearside Pay. By creating an account, you agree to them. If you do not agree, please do not use the service. Please also read our Privacy Policy, which explains how we handle your information.'],
  ['Your account',
    'You must give accurate details when you sign up and keep your password private. You are responsible for activity on your account. Tell us straight away if you think someone else has accessed it.'],
  ['What Clearside Pay does',
    'Clearside Pay helps you record and understand your business finances. The reports, forecasts and AI answers are estimates based on the information you enter. They are not financial, tax or legal advice. Please check important decisions with a qualified professional.'],
  ['The free trial and the free plan',
    'New accounts get 30 days of Pro features for free. No card is needed, and nothing is charged when the trial ends. After the trial, your account moves to the free plan, which you can keep for as long as you like. Your data is never deleted because you are on the free plan.'],
  ['Paid Pro plans',
    'You can buy Pro for 1, 3, 6 or 12 months. You pay once, upfront, and the plan does not renew automatically. If you buy Pro during your free trial, your paid time starts when the trial ends. Prices are shown in South African Rand on the Pricing page before you pay.'],
  ['Payments',
    'Payments are processed securely by our payment provider, Paystack. Clearside Pay never sees or stores your full card details.'],
  ['Cancellations and refunds',
    `Because plans do not renew, there is nothing to cancel: your plan simply ends on its end date. If you were charged in error, or believe you are entitled to a refund under South African consumer law, email ${SUPPORT} and we will respond within a reasonable time.`],
  ['Fair use',
    'Please only use Clearside Pay for your own lawful business. Do not try to access other people\u2019s data, interfere with the service, or use it for fraud. We may suspend accounts that break these rules.'],
  ['Your data',
    'Your business data belongs to you. We only use it to run your account, as described in our Privacy Policy, and we never sell it. You can delete a business at any time, which permanently removes its records.'],
  ['Changes and availability',
    'We are always improving Clearside Pay, so features may change. We work to keep the service available, but it may sometimes be unavailable for maintenance or reasons outside our control. If we make important changes to these terms, we will update this page and let you know.'],
  ['Our responsibility',
    'We take care to keep Clearside Pay accurate and secure. To the extent the law allows, we are not responsible for losses caused by decisions made using estimates in the app, or by events outside our control. Nothing in these terms limits your rights under South African law, including the Consumer Protection Act.'],
  ['Law',
    'These terms are governed by the laws of the Republic of South Africa.'],
];

const Terms = () => {
  const navigate = useNavigate();
  return (
    <div className="ab-page cs-studio">
      <nav className="landing-nav cine-nav">
        <div onClick={() => navigate('/')} style={{ cursor: 'pointer' }}><Logo /></div>
        <ul className="landing-nav-links">
          <li onClick={() => navigate('/')}>Home</li>
          <li onClick={() => navigate('/pricing')}>Pricing</li>
          <li onClick={() => navigate('/privacy')}>Privacy</li>
        </ul>
        <div className="landing-nav-actions">
          <PrefToggles />
          <button className="landing-btn-outline" onClick={() => navigate('/login')}>Sign In</button>
        </div>
      </nav>

      <section className="ab-section" style={{ paddingTop: 160, maxWidth: 800 }}>
        <Reveal as="span" className="ab-tag">Legal</Reveal>
        <Reveal as="h2" className="cs-platinum-text">Terms of Service</Reveal>
        <Reveal as="p" className="ab-lead">
          The rules for using Clearside Pay, written as plainly as we can.
        </Reveal>
        <p className="ab-sign" style={{ textAlign: 'left' }}>Last updated: {UPDATED}</p>

        {SECTIONS.map(([title, text]) => (
          <Reveal key={title} className="ab-promise" style={{ marginTop: 40 }}>
            <h3>{title}</h3>
            <p>{text}</p>
          </Reveal>
        ))}

        <Reveal className="ab-promise" style={{ marginTop: 40, marginBottom: 120 }}>
          <h3>Contact</h3>
          <p>Questions about these terms? Email <a href={`mailto:${SUPPORT}`} style={{ color: 'inherit' }}>{SUPPORT}</a>.</p>
        </Reveal>
      </section>

      <footer className="landing-footer">
        <div>&copy; 2026 Clearside Pay. All rights reserved.</div>
        <div className="landing-footer-links">
          <span onClick={() => navigate('/privacy')}>Privacy</span>
          <span onClick={() => navigate('/contact')}>Contact</span>
        </div>
        <div>Johannesburg &bull; Gauteng &bull; South Africa</div>
      </footer>
    </div>
  );
};

export default Terms;