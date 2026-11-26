import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import Logo from '../components/Logo';
import Reveal from '../components/Reveal';
import { PrefToggles } from '../components/fx/Prefs';
import './Signup.css';

const EMPTY_FORM = { fullName: '', email: '', phoneNumber: '', password: '' };

const Signup = () => {
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await axios.post('http://localhost:8080/auth/signup', form, { timeout: 15000 });
      navigate('/login');
    } catch (err) {
      if (!err.response) {
        setError('Cannot reach the server. Please try again.');
      } else {
        const data = err.response.data;
        setError(data?.message || (typeof data === 'string' ? data : 'Could not create account'));
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container cs-studio">
      <div style={{ position: 'fixed', top: 20, right: 24, zIndex: 10 }}>
        <PrefToggles />
      </div>

      <Reveal className="auth-card cs-metal">
        <div className="auth-logo-wrapper" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
          <Logo />
        </div>

        <div className="auth-header">
          <h2 className="cs-platinum-text">Create your account.</h2>
          <p>Start with 30 days of Pro, free. No card needed.</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-form-group">
            <label htmlFor="fullName">Full Name</label>
            <input id="fullName" name="fullName" type="text" placeholder="Your full name" autoComplete="name"
              value={form.fullName} onChange={handleChange} required />
          </div>

          <div className="auth-form-group">
            <label htmlFor="email">Email Address</label>
            <input id="email" name="email" type="email" placeholder="you@company.co.za" autoComplete="email"
              value={form.email} onChange={handleChange} required />
          </div>

          <div className="auth-form-group">
            <label htmlFor="phoneNumber">Phone Number</label>
            <input id="phoneNumber" name="phoneNumber" type="tel" placeholder="082 123 4567" autoComplete="tel"
              value={form.phoneNumber} onChange={handleChange} required />
          </div>

          <div className="auth-form-group">
            <label htmlFor="password">Password</label>
            <input id="password" name="password" type="password" placeholder="At least 8 characters"
              autoComplete="new-password" value={form.password} onChange={handleChange} required minLength={8} />
          </div>

          {error && <p className="auth-error">{error}</p>}

          <button type="submit" className="auth-submit-btn" disabled={loading}>
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>

        <div className="auth-footer-text">
          Already have an account? <Link to="/login">Sign In</Link>
        </div>
        <div className="auth-footer-text" style={{ marginTop: 0 }}>
          By signing up you agree to our <Link to="/terms">Terms of Service</Link> and{' '}
          <Link to="/privacy">Privacy Policy</Link>.
        </div>
      </Reveal>
    </div>
  );
};

export default Signup;