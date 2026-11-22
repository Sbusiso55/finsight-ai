import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { PrefsProvider } from './components/fx/Prefs';
import './styles/theme.css';
import './styles/light.css';
import './styles/light-app.css';
import './styles/AppMotion.css';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <PrefsProvider>
      <App />
    </PrefsProvider>
  </React.StrictMode>
);