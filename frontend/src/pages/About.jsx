import React from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import './About.css';

const values = [
  { title: 'Clarity', text: 'Your numbers, explained in plain language. No accounting degree needed.' },
  { title: 'Precision', text: 'Every rand is tracked accurately, down to the cent.' },
  { title: 'Trust', text: 'Your data is protected and only ever visible to you.' },
  { title: 'Speed', text: 'Real-time insight, so you can act today and not at month end.' },
];

const goals = [
  'Give every small business a clear, live view of its money.',
  'Replace guesswork with AI insights and forecasts anyone can understand.',
  'Connect stock, sales and card machine payments in one place.',
  'Stay affordable for spaza shops, salons, vendors and growing retailers.',
];

const About = () => {
  const navigate = useNavigate();

  return (
    <div className="about-layout">
      <header className="about-nav">
        <div onClick={() => navigate('/')} style={{ cursor: 'pointer' }}><Logo /></div>
        <div className="about-nav-links">
          <button onClick={() => navigate('/contact')}>Contact</button>
          <button className="about-nav-cta" onClick={() => navigate('/signup')}>Get Started</button>
        </div>
      </header>

      <section className="about-hero">
        <span className="about-tag">About Clearside Pay</span>
        <h1>Financial clarity for every business.</h1>
        <p>
          Clearside Pay helps small business owners understand their money: what's coming in,
          what's going out, and what to do next. It's built for real businesses, from spaza
          shops to growing retailers.
        </p>
      </section>

      <section className="about-grid-2">
        <div className="about-card">
          <span className="about-tag">Our Mission</span>
          <h2>Make every business owner confident about their money.</h2>
          <p>
            We turn everyday transactions into clear insights, forecasts and advice, so owners
            can make smart decisions without needing an accountant on call.
          </p>
        </div>
        <div className="about-card">
          <span className="about-tag">Our Vision</span>
          <h2>A world where no business fails for lack of financial insight.</h2>
          <p>
            We're starting in South Africa, and we're building tools that any small business,
            anywhere, can rely on.
          </p>
        </div>
      </section>

      <section className="about-section">
        <span className="about-tag">Our Goals</span>
        <ul className="about-goals">
          {goals.map((g) => <li key={g}>{g}</li>)}
        </ul>
      </section>

      <section className="about-section">
        <span className="about-tag">Our Values</span>
        <div className="about-values">
          {values.map((v) => (
            <div className="about-card" key={v.title}>
              <h3>{v.title}</h3>
              <p>{v.text}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="about-card about-founder">
        <span className="about-tag">From the Founder</span>
        <p className="about-quote">
          &ldquo;"Running a business is hard enough. Understanding your money shouldn't be."&rdquo;
        </p>
        <p className="about-founder-name">Kevin Dlamini, Founder</p>
      </section>

      <section className="about-cta">
        <h2>Ready to see your business clearly?</h2>
        <button onClick={() => navigate('/signup')}>Create Free Account</button>
      </section>

      <footer className="about-footer">
        &copy; 2026 Clearside Pay. All rights reserved. &bull; support.clearsidepay@gmail.com
      </footer>
    </div>
  );
};

export default About;