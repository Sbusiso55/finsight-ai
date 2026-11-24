import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import Logo from '../components/Logo';
import api from '../api/axiosConfig';
import './LandingPage.css';
import './AboutStory.css';

const BillingCallback = () => {
  const location = useLocation();
  const [state, setState] = useState({ status: 'checking', sub: null, message: '' });

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const reference = params.get('reference') || params.get('trxref');
    if (!reference) {
      setState({ status: 'error', sub: null, message: 'No payment reference was found.' });
      return;
    }
    api.get(`/api/billing/confirm?reference=${encodeURIComponent(reference)}`, { timeout: 20000 })
      .then((res) => setState({ status: 'success', sub: res.data, message: '' }))
      .catch((err) => {
        const code = err.response?.status;
        setState({
          status: 'error',
          sub: null,
          message: code === 402
            ? 'Your payment was not completed, so nothing was charged. You can try again from the Pricing page.'
            : 'We could not confirm your payment yet. If money left your account, contact support and we will sort it out.',
        });
      });
  }, [location.search]);

  const goTo = (path) => { window.location.href = path; };

  const endDate = state.sub?.currentPeriodEnd
    ? new Date(state.sub.currentPeriodEnd).toLocaleDateString('en-ZA', { day: 'numeric', month: 'long', year: 'numeric' })
    : null;

  return (
    <div className="ab-page cs-studio" style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24 }}>
      <div className="ab-card cs-metal" style={{ maxWidth: 480, width: '100%', textAlign: 'center' }}>
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 20 }}><Logo /></div>

        {state.status === 'checking' && (
          <>
            <h3>Confirming your payment…</h3>
            <p>This only takes a moment. Please don't close this page.</p>
          </>
        )}

        {state.status === 'success' && (
          <>
            <span className="ab-tag">Payment confirmed</span>
            <h3>Welcome to Clearside Pay Pro.</h3>
            <p>
              Every Pro feature is unlocked{endDate ? ` until ${endDate}` : ''}. Your plan will not renew
              automatically.
            </p>
            <button className="landing-btn-gold landing-btn-lg" style={{ marginTop: 20 }} onClick={() => goTo('/dashboard')}>
              Go to dashboard
            </button>
          </>
        )}

        {state.status === 'error' && (
          <>
            <h3>Payment not confirmed</h3>
            <p>{state.message}</p>
            <div style={{ display: 'flex', gap: 10, justifyContent: 'center', marginTop: 20, flexWrap: 'wrap' }}>
              <button className="landing-btn-gold" onClick={() => goTo('/pricing')}>Back to pricing</button>
              <button className="landing-btn-outline" onClick={() => goTo('/contact')}>Contact support</button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default BillingCallback;