import React, { createContext, useContext, useEffect, useState } from 'react';

const PrefsContext = createContext({ theme: 'dark', setTheme: () => {}, cursor: true, setCursor: () => {} });

const read = (key, fallback) => {
  try {
    const v = localStorage.getItem(key);
    return v === null ? fallback : v;
  } catch (e) {
    return fallback;
  }
};

export const PrefsProvider = ({ children }) => {
  const [theme, setTheme] = useState(() => read('cs-theme', 'dark'));
  const [cursor, setCursor] = useState(() => read('cs-cursor', 'on') === 'on');

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    try { localStorage.setItem('cs-theme', theme); } catch (e) { /* storage blocked */ }
  }, [theme]);

  useEffect(() => {
    try { localStorage.setItem('cs-cursor', cursor ? 'on' : 'off'); } catch (e) { /* storage blocked */ }
  }, [cursor]);

  return (
    <PrefsContext.Provider value={{ theme, setTheme, cursor, setCursor }}>
      {children}
    </PrefsContext.Provider>
  );
};

export const usePrefs = () => useContext(PrefsContext);

const CursorIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round">
    <path d="M5 3l14 7-6 2-2 6z" />
  </svg>
);

const SunIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round">
    <circle cx="12" cy="12" r="4" />
    <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
  </svg>
);

const MoonIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round">
    <path d="M20 14.5A8 8 0 019.5 4a8 8 0 1010.5 10.5z" />
  </svg>
);

export const PrefToggles = () => {
  const { theme, setTheme, cursor, setCursor } = usePrefs();
  const hasMouse = window.matchMedia('(pointer: fine)').matches;
  const isLight = theme === 'light';

  return (
    <div className="cs-toggles">
      {hasMouse && (
        <button
          type="button"
          className={`cs-toggle ${cursor ? 'is-on' : ''}`}
          onClick={() => setCursor(!cursor)}
          aria-pressed={cursor}
          aria-label={cursor ? 'Turn custom cursor off' : 'Turn custom cursor on'}
          title={cursor ? 'Custom cursor: on' : 'Custom cursor: off'}
        >
          <CursorIcon />
        </button>
      )}
      <button
        type="button"
        className="cs-toggle"
        onClick={() => setTheme(isLight ? 'dark' : 'light')}
        aria-label={isLight ? 'Switch to dark mode' : 'Switch to light mode'}
        title={isLight ? 'Dark mode' : 'Light mode'}
      >
        {isLight ? <MoonIcon /> : <SunIcon />}
      </button>
    </div>
  );
};