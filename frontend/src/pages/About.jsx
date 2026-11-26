import React, { lazy, Suspense, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import Reveal from '../components/Reveal';
import { SplitTitle, Cursor, Magnetic, detectSupport } from '../components/fx/Fx';
import { PrefToggles } from '../components/fx/Prefs';
import './LandingPage.css';
import './LandingCinematic.css';
import './AboutStory.css';

const AboutScene = lazy(() => import('../components/cinematic/AboutScene'));

const MILESTONES = [
  { when: 'October 2026', title: 'Founded in Johannesburg',
    text: 'Clearside Pay was founded with one question: why do so many small businesses not know where their money goes? We set out to build the answer.' },
  { when: 'October 2026', title: 'The engine goes live',
    text: 'Income, expenses, cash flow, margins and forecasting, all calculated to the cent and running on secure cloud infrastructure.' },
  { when: 'November 2026', title: 'Security first',
    text: 'Before handling anyone\u2019s money, we strengthened our access controls so every business only ever sees its own data.' },
  { when: 'November 2026', title: 'Clearside Pay',
    text: 'A new name and identity for what the product had become: a clear view of every rand.' },
  { when: 'Next', title: 'Payments and stock', next: true,
    text: 'Live card machine payments and barcode stock scanning, fully tested in sandbox before any real money moves.' },
];

const VALUES = [
  ['01', 'Clarity', 'Your numbers, explained in plain language. No accounting degree needed.'],
  ['02', 'Precision', 'Every rand is tracked accurately, down to the cent.'],
  ['03', 'Trust', 'Your data is protected and only ever visible to you.'],
  ['04', 'Speed', 'Real-time insight, so you can act today and not at month end.'],
];

const PROMISES = [
  ['Your data stays yours', 'Your business data is only used to run your Clearside Pay account. We never sell it.'],
  ['Tested before it ships', 'New features are tested before release, and payments are proven in sandbox before real money moves.'],
  ['Made for South Africa', 'Rand-native from day one, built for spaza shops, salons, vendors and growing retailers.'],
];

const SECURITY = [
  ['Private by design', 'Every request is checked against your account, so no one can see or change another business\u2019s records.'],
  ['Encrypted sign-in', 'Passwords are never stored in plain text, and sessions expire automatically.'],
  ['Secrets locked away', 'Database and security keys are kept out of our code and rotated when needed.'],
  ['Your data, your say', 'Delete a business and all of its records are permanently removed.'],
];

const About = () => {
  const navigate = useNavigate();
  const [{ cinematic, lite }] = useState(detectSupport);

  return (
    <div className="ab-page cs-studio">
      <Cursor />
      <nav className="landing-nav cine-nav">
        <div onClick={() => navigate('/')} style={{ cursor: 'pointer' }}><Logo /></div>
        <ul className="landing-nav-links">
          <li onClick={() => navigate('/')}>Home</li>
          <li onClick={() => navigate('/pricing')}>Pricing</li>
          <li onClick={() => navigate('/help')}>Help & FAQ</li>
          <li onClick={() => navigate('/contact')}>Contact</li>
        </ul>
        <div className="landing-nav-actions">
          <PrefToggles />
          <button className="landing-btn-outline" onClick={() => navigate('/login')}>Sign In</button>
          <Magnetic>
            <button className="landing-btn-gold" onClick={() => navigate('/signup')}>Get Started</button>
          </Magnetic>
        </div>
      </nav>

      <header className="ab-hero">
        {cinematic && (
          <div className="ab-hero-canvas">
            <Suspense fallback={null}><AboutScene lite={lite} /></Suspense>
          </div>
        )}
        <div className="ab-hero-copy">
          <Reveal as="span" className="ab-tag">About Clearside Pay</Reveal>
          <SplitTitle as="h1" text="Built in Johannesburg for the businesses that keep South Africa moving." />
          <Reveal as="p" delay={600}>
            Clearside Pay gives small business owners a clear, live view of their money: what is
            coming in, what is going out, and what to do next.
          </Reveal>
        </div>
      </header>

      <section className="ab-section">
        <Reveal as="span" className="ab-tag">Our story</Reveal>
        <SplitTitle text="It started with one question." />
        <Reveal as="p" className="ab-lead">
          Most small businesses don't fail because the owner doesn't work hard. They fail because
          the numbers arrive too late, or not at all. Clearside Pay was founded in Johannesburg in
          2026 to change that, giving every business owner the kind of financial insight that
          used to need an accountant on call, live and in plain language.
        </Reveal>
        <div className="ab-stats">
          <Reveal className="ab-stat cs-metal"><strong>R0,01</strong><span>every calculation is accurate down to the cent</span></Reveal>
          <Reveal delay={150} className="ab-stat cs-metal"><strong>1</strong><span>owner per business. Nobody else can see your data</span></Reveal>
          <Reveal delay={300} className="ab-stat cs-metal"><strong>0</strong><span>times we will sell your data. Ever.</span></Reveal>
        </div>
      </section>

      <section className="ab-section">
        <Reveal as="span" className="ab-tag">The journey</Reveal>
        <SplitTitle text="How we got here." />
        <div className="ab-timeline">
          {MILESTONES.map((m) => (
            <Reveal key={m.title} className={`ab-milestone ${m.next ? 'is-next' : ''}`}>
              <span className="ab-when">{m.when}</span>
              <h3>{m.title}</h3>
              <p>{m.text}</p>
            </Reveal>
          ))}
        </div>
      </section>

      <section className="ab-section ab-grid-2">
        <Reveal className="ab-card cs-metal">
          <span className="ab-num">Mission</span>
          <h3>Make every business owner confident about their money.</h3>
          <p>We turn everyday transactions into clear insights, forecasts and advice, so owners can make smart decisions without waiting for month end.</p>
        </Reveal>
        <Reveal delay={150} className="ab-card cs-metal">
          <span className="ab-num">Vision</span>
          <h3>No business should fail for lack of financial insight.</h3>
          <p>We're starting in South Africa and building tools that any small business, anywhere, can rely on.</p>
        </Reveal>
      </section>

      <section className="ab-section">
        <Reveal as="span" className="ab-tag">What we stand for</Reveal>
        <SplitTitle text="Four values. No exceptions." />
        <div className="ab-grid-4">
          {VALUES.map(([n, title, text], i) => (
            <Reveal key={n} delay={i * 120} className="ab-card cs-metal">
              <span className="ab-num">{n}</span>
              <h3>{title}</h3>
              <p>{text}</p>
            </Reveal>
          ))}
        </div>
      </section>

      <section className="ab-section">
        <Reveal as="span" className="ab-tag">Our promise</Reveal>
        <div className="ab-grid-3">
          {PROMISES.map(([title, text], i) => (
            <Reveal key={title} delay={i * 150} className="ab-promise">
              <h3>{title}</h3>
              <p>{text}</p>
            </Reveal>
          ))}
        </div>
      </section>

      <section className="ab-section">
        <Reveal as="span" className="ab-tag">Security</Reveal>
        <SplitTitle text="Built to protect your business." />
        <div className="ab-grid-4">
          {SECURITY.map(([title, text], i) => (
            <Reveal key={title} delay={i * 120} className="ab-card cs-metal">
              <h3>{title}</h3>
              <p>{text}</p>
            </Reveal>
          ))}
        </div>
      </section>

      <section className="ab-section ab-founder">
        <Reveal as="blockquote">
          &ldquo;Big companies have accountants and analysts. Small businesses have Clearside Pay.
          Our goal is to make that a fair fight.&rdquo;
        </Reveal>
        <Reveal as="p" delay={300} className="ab-sign">Kevin Dlamini &middot; Founder, Clearside Pay</Reveal>
      </section>

      <section className="ab-section ab-cta">
        <SplitTitle text="Ready to see your business clearly?" />
        <Reveal delay={400}>
          <Magnetic>
            <button className="landing-btn-gold landing-btn-lg" onClick={() => navigate('/signup')}>Create Free Account</button>
          </Magnetic>
        </Reveal>
      </section>

      <footer className="landing-footer">
        <div>&copy; 2026 Clearside Pay. All rights reserved.</div>
        <div className="landing-footer-links">
          <span onClick={() => navigate('/pricing')}>Pricing</span>
          <span onClick={() => navigate('/help')}>Help & FAQ</span>
          <span onClick={() => navigate('/contact')}>Contact</span>
          <span onClick={() => navigate('/privacy')}>Privacy</span>
          <span onClick={() => navigate('/terms')}>Terms</span>
          <a href="mailto:support.clearsidepay@gmail.com">support.clearsidepay@gmail.com</a>
        </div>
        <div>Johannesburg &bull; Gauteng &bull; South Africa</div>
      </footer>
    </div>
  );
};

export default About;