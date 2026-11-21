import React from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import Reveal from '../components/Reveal';
import { PrefToggles } from '../components/fx/Prefs';
import './LandingPage.css';
import './LandingCinematic.css';
import './AboutStory.css';

const SUPPORT = 'support.clearsidepay@gmail.com';
const UPDATED = '21 November 2026';

const SECTIONS = [
  ['Who we are',
    'Clearside Pay is a financial insights platform for small businesses, based in Johannesburg, South Africa. We are the responsible party for the personal information described in this policy, as defined in the Protection of Personal Information Act, 2013 (POPIA).'],
  ['What we collect',
    'Account details: your full name, email address, phone number and a securely hashed password. Business details: business name, industry, currency, opening balance, and the transactions, categories and customers you record.'],
  ['Why we collect it',
    'Only to run your Clearside Pay account: to calculate your cash position, margins, reports and forecasts, to answer your questions in Ask AI, to keep your account secure, and to respond when you contact us. We do not sell your information, and we do not use it for advertising.'],
  ['AI features',
    'When you use Ask AI, the relevant financial figures from your account are sent to our AI provider (Google Gemini) to generate an answer. We send only what is needed for your question.'],
  ['Where your data is stored',
    'Your data is stored on secure cloud infrastructure, which may be located outside South Africa (currently in the European Union). Where information is transferred across borders, we only use providers bound by data protection standards comparable to POPIA.'],
  ['How we protect it',
    'Passwords are stored as one-way hashes, sessions expire automatically, every request is checked against your account so no one else can access your business records, and our database and security keys are kept out of our code.'],
  ['How long we keep it',
    'We keep your information for as long as your account is active. When you delete a business, its transactions, categories and customers are permanently removed. To close your account entirely, contact us.'],
  ['Your rights',
    `Under POPIA you may ask to see the personal information we hold about you, ask us to correct or delete it, and object to how it is processed. Email ${SUPPORT} and we will respond within a reasonable time. You also have the right to lodge a complaint with the Information Regulator of South Africa (inforegulator.org.za).`],
  ['Changes to this policy',
    'If we make important changes, we will update this page and the date below, and let you know in the app or by email.'],
];

const Privacy = () => {
  const navigate = useNavigate();
  return (
    <div className="ab-page cs-studio">
      <nav className="landing-nav cine-nav">
        <div onClick={() => navigate('/')} style={{ cursor: 'pointer' }}><Logo /></div>
        <ul className="landing-nav-links">
          <li onClick={() => navigate('/')}>Home</li>
          <li onClick={() => navigate('/about')}>About</li>
          <li onClick={() => navigate('/contact')}>Contact</li>
        </ul>
        <div className="landing-nav-actions">
          <PrefToggles />
          <button className="landing-btn-outline" onClick={() => navigate('/login')}>Sign In</button>
        </div>
      </nav>

      <section className="ab-section" style={{ paddingTop: 160, maxWidth: 800 }}>
        <Reveal as="span" className="ab-tag">Legal</Reveal>
        <Reveal as="h2" className="cs-platinum-text">Privacy Policy</Reveal>
        <Reveal as="p" className="ab-lead">
          Your business data is yours. This policy explains what we collect, why, and the
          control you have over it.
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
          <p>Questions about privacy? Email <a href={`mailto:${SUPPORT}`} style={{ color: 'inherit' }}>{SUPPORT}</a>.</p>
        </Reveal>
      </section>

      <footer className="landing-footer">
        <div>&copy; 2026 Clearside Pay. All rights reserved.</div>
        <div>Johannesburg &bull; Gauteng &bull; South Africa</div>
      </footer>
    </div>
  );
};

export default Privacy;