import React, { lazy, Suspense, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import Lenis from 'lenis';
import Logo from '../components/Logo';
import Reveal from '../components/Reveal';
import { SplitTitle, Cursor, Magnetic, detectSupport } from '../components/fx/Fx';
import { PrefToggles, usePrefs } from '../components/fx/Prefs';
import './LandingPage.css';
import './LandingCinematic.css';

gsap.registerPlugin(ScrollTrigger);

const CinematicScene = lazy(() => import('../components/cinematic/CinematicScene'));

const SCENES = [
  { align: 'center', tag: 'Clearside Pay', title: 'Financial clarity, engineered.',
    text: 'Built for South African businesses, from the spaza shop on the corner to the growing retailer.' },
  { align: 'left', tag: 'The problem', title: 'Running a business is hard enough.',
    text: 'Receipts, cash, card slips and stock everywhere, and no clear picture of where the money went.' },
  { align: 'left', tag: 'The answer', title: "Understanding your money shouldn't be.",
    points: ['Live cash position and margins', 'Expense breakdowns and reports', 'Ask about your numbers in plain language'] },
  { align: 'left', tag: 'Coming soon', title: 'Card payments, live.',
    text: 'Tap, chip, swipe, Apple Pay, Google Pay and QR. Every payment lands on your dashboard the moment it clears.' },
  { align: 'left', tag: 'Coming soon', title: 'Stock, scanned.',
    text: 'Scan barcodes or QR codes to track stock in and out, with low-stock alerts and supplier tracking.' },
  { align: 'center', tag: 'Get started', title: 'See your business clearly.', cta: true },
];

const CARDS = [
  ['01 // CASH FLOW', 'Know your cash position', 'See your opening balance, money in, money out and what is left, for any month, without spreadsheets.'],
  ['02 // MARGINS', 'See what you really keep', 'Fixed and variable costs are separated automatically, so you can see your gross and net profit margins at a glance.'],
  ['03 // INSIGHTS', 'Ask about your numbers', 'Ask questions in plain language, like "where am I spending the most?", and get clear answers and forecasts.'],
];

const SPECS = [
  ['R0,01', 'Accurate to the cent'],
  ['30 days', 'Of Pro free, no card needed'],
  ['1', 'Owner per business, nobody else sees your data'],
  ['R0', 'Free plan, forever'],
];

const LandingPage = () => {
  const navigate = useNavigate();
  const { theme } = usePrefs();
  const trackRef = useRef(null);
  const progress = useRef(0);
  const barRef = useRef(null);
  const [{ cinematic, lite }] = useState(detectSupport);
  const [active, setActive] = useState(true);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (!cinematic) { setReady(true); return undefined; }
    const t = setTimeout(() => setReady(true), 8000);
    return () => clearTimeout(t);
  }, [cinematic]);

  useEffect(() => {
    if (!cinematic) return undefined;

    const lenis = new Lenis({ duration: 1.2, smoothWheel: true });
    lenis.on('scroll', ScrollTrigger.update);
    const raf = (time) => lenis.raf(time * 1000);
    gsap.ticker.add(raf);
    gsap.ticker.lagSmoothing(0);

    const ctx = gsap.context(() => {
      ScrollTrigger.create({
        trigger: trackRef.current, start: 'top top', end: 'bottom bottom',
        onUpdate: (self) => {
          progress.current = self.progress;
          if (barRef.current) barRef.current.style.transform = `scaleX(${self.progress})`;
        },
      });
      ScrollTrigger.create({
        trigger: trackRef.current, start: 'top bottom', end: 'bottom top',
        onToggle: (self) => setActive(self.isActive),
      });
      gsap.utils.toArray('.cine-step').forEach((step, i, all) => {
        const copy = step.querySelector('.cine-copy');
        const tl = gsap.timeline({
          scrollTrigger: { trigger: step, start: 'top bottom', end: 'bottom top', scrub: true },
        });
        tl.fromTo(copy,
          { autoAlpha: i === 0 ? 1 : 0, y: i === 0 ? 0 : 60 },
          { autoAlpha: 1, y: 0, duration: 0.35, ease: 'none' })
          .to({}, { duration: 0.3 });
        if (i < all.length - 1) tl.to(copy, { autoAlpha: 0, y: -60, duration: 0.35, ease: 'none' });
      });
    }, trackRef);

    return () => {
      ctx.revert();
      gsap.ticker.remove(raf);
      lenis.destroy();
    };
  }, [cinematic]);

  return (
    <div className="landing-container">
      <Cursor />
      <div className={`cs-loader ${ready ? 'is-done' : ''}`} aria-hidden={ready}>
        <Logo />
        <div className="cs-loader-bar"><span /></div>
      </div>

      <nav className="landing-nav cine-nav">
        <div onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
          <Logo />
        </div>
        <ul className="landing-nav-links">
          <li onClick={() => navigate('/about')}>About</li>
          <li onClick={() => navigate('/pricing')}>Pricing</li>
          <li onClick={() => navigate('/help')}>Help & FAQ</li>
          <li onClick={() => navigate('/contact')}>Contact</li>
        </ul>
        <div className="landing-nav-actions">
          <PrefToggles />
          <button className="landing-btn-outline" onClick={() => navigate('/login')}>Sign In</button>
          <Magnetic>
            <button className="landing-btn-gold" onClick={() => navigate('/signup')}>Get Started</button>
          </Magnetic>
        </div>
      </nav>

      <section ref={trackRef} className={`cine-track ${cinematic ? '' : 'static'}`}>
        {cinematic && (
          <div className="cine-canvas">
            <Suspense fallback={null}>
              <CinematicScene
                progress={progress}
                active={active}
                lite={lite}
                theme={theme}
                onReady={() => setReady(true)}
              />
            </Suspense>
          </div>
        )}

        {SCENES.map((s) => (
          <div key={s.title} className={`cine-step ${s.align}`}>
            <div className="cine-copy">
              <span className="cine-tag">{s.tag}</span>
              <SplitTitle text={s.title} />
              {s.text && <p>{s.text}</p>}
              {s.points && (
                <ul className="cine-points">
                  {s.points.map((pt) => <li key={pt}>{pt}</li>)}
                </ul>
              )}
              {s.cta && (
                <div className="cine-cta">
                  <Magnetic>
                    <button className="landing-btn-gold landing-btn-lg" onClick={() => navigate('/signup')}>
                      Start 30 Days of Pro Free
                    </button>
                  </Magnetic>
                  <button className="landing-btn-outline landing-btn-lg" onClick={() => navigate('/pricing')}>
                    See Pricing
                  </button>
                </div>
              )}
            </div>
          </div>
        ))}
        {cinematic && <div className="cine-progress" ref={barRef} />}
        {cinematic && <div className="cine-scroll-hint">Scroll</div>}
      </section>

      <div className="landing-after cs-studio">
        <section className="landing-specs-bar">
          {SPECS.map(([value, label]) => (
            <div className="landing-spec-item" key={label}><h4>{value}</h4><p>{label}</p></div>
          ))}
        </section>

        <section className="landing-showcase">
          <div className="landing-section-header">
            <span>Core Capabilities</span>
            <SplitTitle text="Everything your business needs to see." />
          </div>
          <div className="landing-grid-3">
            {CARDS.map(([num, title, text], i) => (
              <Reveal key={num} delay={i * 150} className="landing-showcase-card cs-metal">
                <div className="landing-card-number">{num}</div>
                <h3>{title}</h3>
                <p>{text}</p>
              </Reveal>
            ))}
          </div>
        </section>

        <footer className="landing-footer">
          <div>&copy; 2026 Clearside Pay. All rights reserved.</div>
          <div className="landing-footer-links">
            <span onClick={() => navigate('/about')}>About</span>
            <span onClick={() => navigate('/pricing')}>Pricing</span>
            <span onClick={() => navigate('/help')}>Help & FAQ</span>
            <span onClick={() => navigate('/contact')}>Contact</span>
            <span onClick={() => navigate('/privacy')}>Privacy</span>
            <a href="mailto:support.clearsidepay@gmail.com">support.clearsidepay@gmail.com</a>
          </div>
          <div>Johannesburg &bull; Gauteng &bull; South Africa</div>
        </footer>
      </div>
    </div>
  );
};

export default LandingPage;