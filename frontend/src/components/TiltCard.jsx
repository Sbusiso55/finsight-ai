import React, { useRef } from 'react';

const TiltCard = ({ children, className = '', max = 6 }) => {
  const ref = useRef(null);
  const canTilt = typeof window !== 'undefined'
    && window.matchMedia('(pointer: fine)').matches
    && !window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  const move = (e) => {
    if (!canTilt || !ref.current) return;
    const r = ref.current.getBoundingClientRect();
    const x = (e.clientX - r.left) / r.width;
    const y = (e.clientY - r.top) / r.height;
    ref.current.style.transform = `perspective(900px) rotateX(${(0.5 - y) * max}deg) rotateY(${(x - 0.5) * max}deg)`;
    ref.current.style.setProperty('--shine-x', `${x * 100}%`);
    ref.current.style.setProperty('--shine-y', `${y * 100}%`);
  };

  const leave = () => {
    if (!ref.current) return;
    ref.current.style.transform = '';
  };

  return (
    <section ref={ref} className={`am-tilt ${className}`} onMouseMove={move} onMouseLeave={leave}>
      {children}
    </section>
  );
};

export default TiltCard;