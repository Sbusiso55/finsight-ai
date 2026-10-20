import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    const navItems = [
        { path: '/dashboard', label: '📊 Overview' },
        { path: '/transactions', label: '💳 Transactions' },
        { path: '/reports', label: '📋 Reports' },
        { path: '/ask-ai', label: '💬 Ask AI' },
        { path: '/settings', label: '⚙️ Settings' }
    ];

    return (
        <aside className="sidebar">
            <div className="sidebar-logo" onClick={() => navigate('/dashboard')}>
                FinSight
            </div>
            <nav className="sidebar-nav">
                {navItems.map((item) => (
                    <button
                        key={item.path}
                        className={`nav-item ${location.pathname === item.path ? 'active' : ''}`}
                        onClick={() => navigate(item.path)}
                    >
                        {item.label}
                    </button>
                ))}
            </nav>
            <button className="logout-btn" onClick={handleLogout}>
                Logout
            </button>
        </aside>
    );
};

export default Navbar;
