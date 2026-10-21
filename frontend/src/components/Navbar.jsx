import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const navItems = [
        { label: 'Dashboard', path: '/dashboard' },
        { label: 'Transactions', path: '/transactions' },
        { label: 'Reports', path: '/reports' },
        { label: 'Ask AI', path: '/ask-ai' },
        { label: 'Settings', path: '/settings' },
    ];

    return (
        <nav className="navbar">
            <div className="navbar-logo" onClick={() => navigate('/dashboard')}>
                Fin<span>Sight</span>
            </div>
            <div className="navbar-links">
                {navItems.map((item) => (
                    <button
                        key={item.path}
                        className={`navbar-link ${location.pathname === item.path ? 'active' : ''}`}
                        onClick={() => navigate(item.path)}
                    >
                        {item.label}
                    </button>
                ))}
            </div>
            <button className="navbar-logout" onClick={() => navigate('/login')}>
                Log Out
            </button>
        </nav>
    );
};

export default Navbar;
