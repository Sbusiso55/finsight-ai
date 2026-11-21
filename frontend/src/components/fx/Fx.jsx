import React, { useEffect, useRef, useState } from 'react';
import Reveal from '../Reveal';
import { usePrefs } from './Prefs';

export const prefersReducedMotion = () =>
  window.matchMedia('(prefers-reduced-motion: reduce)').matches;

export const detectSupport = () => {
  let webgl = false;
  try {
    const c = document.createElement('canvas');
    webgl = !!(window.WebGLRenderingContext && (c.getContext('webgl2') || c.getContext('webgl')));
  } catch (e) { webgl = false; }
  const lite = window.matchMedia('(max-width: 768px)').matches || (navigator.hardwareConcurrency || 8) <= 4;
  return { cinematic: !prefersReducedMotion() && webgl, lite };
};

export const SplitTitle = ({ text, as = 'h2', className = '', delay = 0 }) => {
  let n = 0;
  const words = text.split(' ');
  return (
    <Reveal as={as} className={`cs-split ${className}`} aria-label={text}>
      {words.map((word, wi) => (
        <span className="cs-word" key={wi} aria-hidden="true">
          {word.split('').map((ch, ci) => (
            <span className="cs-char" key={ci} style={{ transitionDelay: `${delay + (n++) * 22}ms` }}>{ch}</span>
          ))}
          {wi < words.length - 1 ? '\u00A0' : ''}
        </span>
      ))}
    </Reveal>
  );
};

export const CountUp = ({ to, decimals = 0, prefix = '', suffix = '', duration = 1800 }) => {
  const ref = useRef(null);
  const [value, setValue] = useState(0);
  useEffect(() => {
    if (prefersReducedMotion()) { setValue(to); return undefined; }
    let raf;
    const observer = new IntersectionObserver(([entry]) => {
      if (!entry.isIntersecting) return;
      observer.disconnect();
      const start = performance.now();
      const tick = (now) => {
        const k = Math.min(1, (now - start) / duration);
        setValue(to * (1 - Math.pow(1 - k, 3)));
        if (k < 1) raf = requestAnimationFrame(tick);
      };
      raf = requestAnimationFrame(tick);
    }, { threshold: 0.4 });
    if (ref.current) observer.observe(ref.current);
    return () => { observer.disconnect(); cancelAnimationFrame(raf); };
  }, [to, duration]);
  return (
    <span ref={ref}>
      {prefix}{value.toLocaleString('en-ZA', { minimumFractionDigits: decimals, maximumFractionDigits: decimals })}{suffix}
    </span>
  );
};

export const Magnetic = ({ children, strength = 0.3 }) => {
  const ref = useRef(null);
  const move = (e) => {
    const r = ref.current.getBoundingClientRect();
    const dx = e.clientX - (r.left + r.width / 2);
    const dy = e.clientY - (r.top + r.height / 2);
    ref.current.style.transform = `translate(${dx * strength}px, ${dy * strength}px)`;
  };
  const leave = () => { ref.current.style.transform = ''; };
  return <span ref={ref} className="cs-magnetic" onMouseMove={move} onMouseLeave={leave}>{children}</span>;
};

export const Cursor = () => {
  const dot = useRef(null);
  const ring = useRef(null);
  const { cursor } = usePrefs();
  const enabled = cursor && window.matchMedia('(pointer: fine)').matches && !prefersReducedMotion();

  useEffect(() => {
    if (!enabled) return undefined;
    document.body.classList.add('cs-has-cursor');
    let x = window.innerWidth / 2, y = window.innerHeight / 2, rx = x, ry = y, raf;
    const move = (e) => {
      x = e.clientX; y = e.clientY;
      const hover = e.target.closest && e.target.closest('button, a, [data-magnetic]');
      if (ring.current) ring.current.classList.toggle('is-hover', !!hover);
    };
    const loop = () => {
      rx += (x - rx) * 0.18; ry += (y - ry) * 0.18;
      if (dot.current) dot.current.style.transform = `translate(${x}px, ${y}px)`;
      if (ring.current) ring.current.style.transform = `translate(${rx}px, ${ry}px)`;
      raf = requestAnimationFrame(loop);
    };
    window.addEventListener('mousemove', move);
    raf = requestAnimationFrame(loop);
    return () => {
      window.removeEventListener('mousemove', move);
      cancelAnimationFrame(raf);
      document.body.classList.remove('cs-has-cursor');
    };
  }, [enabled]);

  if (!enabled) return null;
  return (<><div ref={dot} className="cs-cursor-dot" /><div ref={ring} className="cs-cursor-ring" /></>);
};