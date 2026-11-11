import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './Auth.css';
import api from "../api/axiosConfig";

const Login = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleLogin = async (e) => {
        e.preventDefault();

        setError('');
        setLoading(true);

        try {
            const response = await api.post('/auth/login', {
                email,
                password,
            });

            const token = response.data.token;

            if (!token) {
                throw new Error('The server did not return a login token.');
            }

            localStorage.setItem('token', token);

            navigate('/dashboard');
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.response?.data ||
                err.message ||
                'Login failed. Please check your details and try again.'
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-wrapper">
            <div className="auth-glow"></div>
            <div className="auth-container">
                <h1 className="auth-logo">Fin<span>Sight</span></h1>
                <p className="auth-subtitle">Welcome back. Sign in to continue.</p>
                <form className="auth-form" onSubmit={handleLogin}>
                    <div className="auth-field">
                        <label>Email</label>
                        <input
                            type="email"
                            placeholder="Enter your email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>
                    <div className="auth-field">
                        <label>Password</label>
                        <input
                            type="password"
                            placeholder="Enter your password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    {error && <p role="alert">{String(error)}</p>}

                    <button
                        type="submit"
                        className="auth-submit"
                        disabled={loading}
                    >
                        {loading ? 'Signing in...' : 'Sign In'}
                    </button>
                </form>
                <p className="auth-switch">
                    Don't have an account? <Link to="/signup">Sign Up</Link>
                </p>
            </div>
        </div>
    );
};

export default Login;
