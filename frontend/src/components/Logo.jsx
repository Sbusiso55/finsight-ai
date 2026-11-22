import React from 'react';
import './Logo.css';

const Logo = () => (
  <div className="finsight-brand-logo">
    <svg className="finsight-logo-icon" viewBox="0 0 32 32" fill="none">
      <polygon
        points="16,2 30,10 30,22 16,30 2,22 2,10"
        stroke="#E5E5E5"
        strokeWidth="1.6"
        fill="rgba(229, 229, 229, 0.05)"
      />
      <path
        d="M20.5 11.5A6 6 0 1 0 20.5 20.5"
        stroke="#E5E5E5"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
    <span className="finsight-logo-text">
      CLEARSIDE<span>PAY</span>
    </span>
  </div>
);

export default Logo;