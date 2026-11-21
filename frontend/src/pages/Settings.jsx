import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import Logo from '../components/Logo';
import Reveal from '../components/Reveal';
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
      if (!err.response) setError('Cannot reach the server. Please try again.');
      else setError(err.response.data?.message || (typeof err.response.data === 'string' ? err.response.data : 'Could not create account'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container cs-studio">
      <Reveal className="auth-card cs-metal">
        <div className="auth-logo-wrapper" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
          <Logo />
        </div>

        <div className="auth-header">
          <h2 className="cs-platinum-text">Create your account.</h2>
          <p>Start understanding your business finances today.</p>
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
      </Reveal>
    </div>
  );
};

export default Signup;