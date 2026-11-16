import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import Logo from './Logo';
import './Navbar.css';

const links = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/transactions', label: 'Transactions' },
  { to: '/add-transaction', label: 'Add Transaction' },
  { to: '/reports', label: 'Reports' },
  { to: '/ask-ai', label: 'Ask AI' },
  { to: '/notifications', label: 'Notifications' },
  { to: '/profile', label: 'Profile' },
  { to: '/settings', label: 'Settings' },
];

const Navbar = () => {
  const navigate = useNavigate();

  const handleSignOut = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <nav className="fs-navbar">
      <div className="fs-navbar-left">
        <Logo />
      </div>

      <div className="fs-navbar-links">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) => (isActive ? 'fs-nav-link active' : 'fs-nav-link')}
          >
            {link.label}
          </NavLink>
        ))}
      </div>

      <button className="fs-signout-btn" onClick={handleSignOut}>
        Sign Out
      </button>
    </nav>
  );
};

export default Navbar;