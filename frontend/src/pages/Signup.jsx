import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Logo from '../components/Logo';
import './Signup.css';

const Signup = () => {
  const [form, setForm] = useState({ fullName: '', email: '', phoneNumber: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await axios.post('http://localhost:8080/auth/signup', form);
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Could not create account');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-logo-wrapper">
          <Logo />
        </div>

        <div className="auth-header">
          <h2>Create your account.</h2>
          <p>Start understanding your business finances today.</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-form-group">
            <label>Full Name</label>
            <input name="fullName" type="text" placeholder="Kevin Dlamini"
              value={form.fullName} onChange={handleChange} required />
          </div>

          <div className="auth-form-group">
            <label>Email Address</label>
            <input name="email" type="email" placeholder="kevin@company.co.za"
              value={form.email} onChange={handleChange} required />
          </div>

          <div className="auth-form-group">
            <label>Phone Number</label>
            <input name="phoneNumber" type="tel" placeholder="082 123 4567"
              value={form.phoneNumber} onChange={handleChange} required />
          </div>

          <div className="auth-form-group">
            <label>Password</label>
            <input name="password" type="password" placeholder="••••••••"
              value={form.password} onChange={handleChange} required minLength={8} />
          </div>

          {error && <p className="auth-error">{error}</p>}

          <button type="submit" className="auth-submit-btn" disabled={loading}>
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>

        <div className="auth-footer-text">
          Already have an account? <a href="/login">Sign In</a>
        </div>
      </div>
    </div>
  );
};

export default Signup;