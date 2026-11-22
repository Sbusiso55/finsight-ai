import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import './About.css';
import './Contact.css';

const SUPPORT_EMAIL = 'support.clearsidepay@gmail.com';

const topics = ['General question', 'Account or sign-in help', 'Billing', 'Report a problem', 'Partnerships'];

const Contact = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', topic: topics[0], message: '' });
  const [sent, setSent] = useState(false);

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = (e) => {
    e.preventDefault();
    const subject = encodeURIComponent(`[Clearside Pay] ${form.topic}`);
    const body = encodeURIComponent(
      `Name: ${form.name}\nEmail: ${form.email}\nTopic: ${form.topic}\n\n${form.message}`
    );
    window.location.href = `mailto:${SUPPORT_EMAIL}?subject=${subject}&body=${body}`;
    setSent(true);
  };

  return (
    <div className="about-layout">
      <header className="about-nav">
        <div onClick={() => navigate('/')} style={{ cursor: 'pointer' }}><Logo /></div>
        <div className="about-nav-links">
          <button onClick={() => navigate('/about')}>About</button>
          <button className="about-nav-cta" onClick={() => navigate('/signup')}>Get Started</button>
        </div>
      </header>

      <section className="about-hero">
        <span className="about-tag">Contact Us</span>
        <h1>We're here to help.</h1>
        <p>Questions, feedback or a problem with your account? Send us a message and we'll get back to you.</p>
      </section>

      <section className="contact-wrap">
        <form className="about-card contact-form" onSubmit={handleSubmit}>
          <div className="contact-row">
            <div className="contact-group">
              <label htmlFor="name">Full Name</label>
              <input id="name" value={form.name} onChange={update('name')} required />
            </div>
            <div className="contact-group">
              <label htmlFor="email">Email Address</label>
              <input id="email" type="email" value={form.email} onChange={update('email')} required />
            </div>
          </div>

          <div className="contact-group">
            <label htmlFor="topic">Topic</label>
            <select id="topic" value={form.topic} onChange={update('topic')}>
              {topics.map((t) => <option key={t}>{t}</option>)}
            </select>
          </div>

          <div className="contact-group">
            <label htmlFor="message">Message</label>
            <textarea id="message" rows="6" value={form.message} onChange={update('message')} required />
          </div>

          <p className="contact-note">Please don't include passwords or card details in your message.</p>

          <button type="submit" className="contact-submit">Send Message</button>

          {sent && (
            <p className="contact-success">
              Your email app should now be open with your message ready. Just press send.
            </p>
          )}
        </form>

        <aside className="about-card contact-side">
          <span className="about-tag">Email</span>
          <p><a href={`mailto:${SUPPORT_EMAIL}`}>{SUPPORT_EMAIL}</a></p>
          <span className="about-tag">Response Time</span>
          <p>Response Within 24 hours</p>
          <span className="about-tag">Account Help</span>
          <p>Use the same email address you signed up with so we can find your account.</p>
        </aside>
      </section>

      <footer className="about-footer">
        &copy; 2026 Clearside Pay. All rights reserved. &bull; {SUPPORT_EMAIL}
      </footer>
    </div>
  );
};

export default Contact;