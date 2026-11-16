import React from 'react';
import Navbar from '../components/Navbar';

const ComingSoon = ({ title }) => (
  <>
    <Navbar />
    <div style={{ padding: '80px 32px', textAlign: 'center', fontFamily: 'DM Sans, sans-serif' }}>
      <h2 style={{ color: '#1A1A1A' }}>{title}</h2>
      <p style={{ color: '#737373' }}>This page is coming soon.</p>
    </div>
  </>
);

export default ComingSoon;