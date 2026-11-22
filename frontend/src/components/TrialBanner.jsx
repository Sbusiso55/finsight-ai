import React, { useEffect, useState } from 'react';
import api from '../api/axiosConfig';
import './TrialBanner.css';

const TrialBanner = () => {
  const [sub, setSub] = useState(null);
  const [hidden, setHidden] = useState(false);

  useEffect(() => {
    let alive = true;
    api.get('/api/subscription')
      .then((res) => { if (alive) setSub(res.data); })
      .catch((err) => console.error('Failed to load subscription:', err));
    return () => { alive = false; };
  }, []);

  if (!sub || hidden) return null;
  if (sub.status === 'ACTIVE') return null;

  const trial = sub.status === 'TRIAL';
  const urgent = trial && sub.daysLeft <= 7;
  const dayWord = sub.daysLeft === 1 ? 'day' : 'days';

  return (
    <div className={`cs-trial-banner ${urgent ? 'is-urgent' : ''} ${trial ? '' : 'is-free'}`}>
      <span className="cs-trial-badge">{trial ? 'Free Pro trial' : 'Free plan'}</span>
      <span className="cs-trial-text">
        {trial
          ? `You have ${sub.daysLeft} ${dayWord} of Pro features, free. No card needed and nothing will be charged. After that you keep the free plan, and all your data stays safe.`
          : 'You are on the free plan, which is free forever. All your data is safe. Upgrade any time to unlock reports, forecasts and more.'}
      </span>
      <button type="button" className="cs-trial-close" onClick={() => setHidden(true)} aria-label="Hide banner">×</button>
    </div>
  );
};

export default TrialBanner;