import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import './About.css';
import './Help.css';

const SUPPORT_EMAIL = 'support.clearsidepay@gmail.com';

const sections = [
  {
    title: 'Getting Started',
    items: [
      {
        q: 'How do I create an account?',
        a: 'Click Get Started, enter your name, email and a password, then sign in. You will be asked to set up your business the first time you log in.',
      },
      {
        q: 'How do I set up my business?',
        a: 'After signing in, fill in your business details on the Business Setup page. We create default income and expense categories for you automatically, so you can start adding transactions straight away.',
      },
      {
        q: 'Is Clearside Pay free?',
        a: 'Yes, Clearside Pay is currently free while we are in early access.',
      },
    ],
  },
  {
    title: 'Transactions',
    items: [
      {
        q: 'How do I add a transaction?',
        a: 'Go to Add Transaction, choose Income or Expense, pick a category, enter the amount, date and a description, then save. It will appear at the top of your Transactions list.',
      },
      {
        q: 'Can I delete a transaction?',
        a: 'Yes. Open the Transactions page, find the transaction and click delete. Your totals and reports update automatically.',
      },
      {
        q: 'How do I find an old transaction?',
        a: 'On the Transactions page, use the search box or filter by Income or Expense.',
      },
    ],
  },
  {
    title: 'Reports & AI',
    items: [
      {
        q: 'What do the reports show?',
        a: 'Reports show your income versus expenses, net profit, profit margin and a breakdown of where your money goes. You can choose a period of up to six months.',
      },
      {
        q: 'What is Ask AI?',
        a: 'Ask AI lets you ask questions about your finances in plain language, like "Where am I spending the most?", and get clear answers and advice.',
      },
      {
        q: 'How accurate are forecasts?',
        a: 'Forecasts need at least three months of transactions. The more data you add, the more useful they become. Treat them as guidance, not guarantees.',
      },
    ],
  },
  {
    title: 'Account & Security',
    items: [
      {
        q: 'I forgot my password. What do I do?',
        a: `Password reset is coming soon. For now, email ${SUPPORT_EMAIL} from the address you signed up with and we will help you.`,
      },
      {
        q: 'Why was I logged out?',
        a: 'For your security, you are signed out automatically when your session expires. Just sign in again.',
      },
      {
        q: 'Who can see my data?',
        a: 'Only you. Your business data is linked to your account and is never shown to other users. We will never ask for your password by email.',
      },
    ],
  },
  {
    title: 'Coming Soon',
    items: [
      {
        q: 'Can I scan stock with barcodes or QR codes?',
        a: 'Stock scanning with barcodes and QR codes, low-stock alerts and supplier tracking are in development.',
      },
      {
        q: 'Can I connect my card machine?',
        a: 'Live card machine payments are in development. We are testing carefully before anything handles real money.',
      },
    ],
  },
];

const Help = () => {
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [open, setOpen] = useState(null);

  const term = search.trim().toLowerCase();
  const filtered = sections
    .map((s) => ({
      ...s,
      items: s.items.filter(
        (i) => !term || i.q.toLowerCase().includes(term) || i.a.toLowerCase().includes(term)
      ),
    }))
    .filter((s) => s.items.length > 0);

  const toggle = (key) => setOpen(open === key ? null : key);

  return (
    <div className="about-layout">
      <header className="about-nav">
        <div onClick={() => navigate('/')} style={{ cursor: 'pointer' }}><Logo /></div>
        <div className="about-nav-links">
          <button onClick={() => navigate('/about')}>About</button>
          <button onClick={() => navigate('/contact')}>Contact</button>
          <button className="about-nav-cta" onClick={() => navigate('/signup')}>Get Started</button>
        </div>
      </header>

      <section className="about-hero">
        <span className="about-tag">Help Centre</span>
        <h1>How can we help?</h1>
        <p>Find quick answers about your account, transactions and reports.</p>
        <input
          className="help-search"
          type="text"
          placeholder="Search for a question..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </section>

      <section className="help-wrap">
        {filtered.length === 0 && (
          <div className="about-card help-empty">
            <p>No results for "{search}". Try another word, or contact us below.</p>
          </div>
        )}

        {filtered.map((s) => (
          <div className="help-section" key={s.title}>
            <span className="about-tag">{s.title}</span>
            {s.items.map((item) => {
              const key = `${s.title}-${item.q}`;
              const isOpen = open === key || term.length > 0;
              return (
                <div className={`help-item ${isOpen ? 'open' : ''}`} key={key}>
                  <button className="help-question" onClick={() => toggle(key)}>
                    <span>{item.q}</span>
                    <span className="help-icon">{isOpen ? '−' : '+'}</span>
                  </button>
                  {isOpen && <p className="help-answer">{item.a}</p>}
                </div>
              );
            })}
          </div>
        ))}
      </section>

      <section className="about-cta">
        <h2>Still need help?</h2>
        <button onClick={() => navigate('/contact')}>Contact Support</button>
      </section>

      <footer className="about-footer">
        &copy; 2026 Clearside Pay. All rights reserved. &bull; {SUPPORT_EMAIL}
      </footer>
    </div>
  );
};

export default Help;