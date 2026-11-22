import { useEffect, useState } from 'react';
import api from '../api/axiosConfig';

export const PRO_LOCKS_ENABLED = false;

let cache = null;

export default function useSubscription() {
  const [sub, setSub] = useState(cache);

  useEffect(() => {
    if (cache) return undefined;
    let alive = true;
    api.get('/api/subscription')
      .then((res) => { cache = res.data; if (alive) setSub(res.data); })
      .catch((err) => console.error('Failed to load subscription:', err));
    return () => { alive = false; };
  }, []);

  const isPro = !PRO_LOCKS_ENABLED || !!sub?.pro;
  return { sub, isPro, loaded: !!sub || !PRO_LOCKS_ENABLED };
}