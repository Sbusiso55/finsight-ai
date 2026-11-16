import React from 'react';
import './Logo.css';

const Logo = () => {
  return (
    <div className="finsight-brand-logo">
      <svg
        className="finsight-logo-icon"
        viewBox="0 0 32 32"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <polygon
          points="16,2 30,10 30,22 16,30 2,22 2,10"
          stroke="#E5E5E5"
          strokeWidth="1.6"
          fill="rgba(229, 229, 229, 0.05)"
        />
        <path
          d="M11 22V10H21M11 16H18.5"
          stroke="#E5E5E5"
          strokeWidth="1.8"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
      <span className="finsight-logo-text">
        FINSIGHT<span>AI</span>
      </span>
    </div>
  );
};

export default Logo;