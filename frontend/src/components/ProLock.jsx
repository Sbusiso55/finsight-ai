import React from 'react';
import { useNavigate } from 'react-router-dom';
import useSubscription from '../hooks/useSubscription';
import './ProLock.css';

const ProLock = ({ children, feature = 'This feature' }) => {
  const navigate = useNavigate();
  const { isPro } = useSubscription();

  if (isPro) return children;

  return (
    <div className="pl-wrap">
      <div className="pl-preview" aria-hidden="true">{children}</div>
      <div className="pl-card">
        <span className="pl-badge">Pro</span>
        <h3>{feature} is part of Pro</h3>
        <p>Your data is safe and nothing is deleted. Upgrade any time to see it.</p>
        <button type="button" className="pl-btn" onClick={() => navigate('/pricing')}>Unlock with Pro</button>
      </div>
    </div>
  );
};

export default ProLock;