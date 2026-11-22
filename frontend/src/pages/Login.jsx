import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import Logo from '../components/Logo';
import Reveal from '../components/Reveal';
import './Signup.css';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const body = { email, password };
      const res = await axios.post('http://localhost:8080/auth/login', body, { timeout: 15000 });
      const token = res.data?.token || res.data?.accessToken || res.data?.jwt;
      if (!token) {
        setError('Login succeeded but no token was returned.');
        return;
      }
      localStorage.setItem('token', token);
      navigate('/dashboard');
    } catch (err) {
      if (!err.response) setError('Cannot reach the server. Please try again.');
      else if (err.response.status === 401 || err.response.status === 403) setError('Invalid email or password');
      else setError(err.response.data?.message || 'Something went wrong. Please try again.');
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
          <h2 className="cs-platinum-text">Welcome back.</h2>
          <p>Sign in to access your business financial intelligence.</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-form-group">
            <label htmlFor="email">Email Address</label>
            <input id="email" type="email" placeholder="you@company.co.za" autoComplete="email"
              value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>

          <div className="auth-form-group">
            <label htmlFor="password">Password</label>
            <input id="password" type="password" placeholder="••••••••" autoComplete="current-password"
              value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>

          {error && <p className="auth-error">{error}</p>}

          <button type="submit" className="auth-submit-btn" disabled={loading}>
            {loading ? 'Signing In...' : 'Sign In'}
          </button>
        </form>

        <div className="auth-footer-text">
          Don't have an account? <Link to="/signup">Create Account</Link>
        </div>
      </Reveal>
    </div>
  );
};

export default Login;