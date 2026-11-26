import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './AskAI.css';

const SUGGESTIONS = [
  'What were my top three expenses this month?',
  'Did I make a profit last month?',
  'How is my cash flow looking?',
];

const AskAI = () => {
  const [query, setQuery] = useState('');
  const [response, setResponse] = useState('');
  const [failed, setFailed] = useState(false);
  const [loading, setLoading] = useState(false);

  const ask = async (text) => {
    if (!text.trim()) return;
    setLoading(true);
    setResponse('');
    setFailed(false);
    try {
      const res = await api.post('/api/ai/query', { prompt: text });
      setResponse(res.data?.answer || res.data?.message || 'Analysis complete.');
    } catch (err) {
      console.error('AI query failed:', err);
      setFailed(true);
      setResponse('The Clearside Pay analyst is not available yet. We are still setting it up, so please try again soon.');
    } finally {
      setLoading(false);
    }
  };

  const handleAsk = (e) => {
    e.preventDefault();
    ask(query);
  };

  const pickSuggestion = (text) => {
    setQuery(text);
    ask(text);
  };

  return (
    <div className="ai-layout">
      <Navbar />
      <main className="ai-container">
        <h1 className="ai-title">Clearside Pay Analyst</h1>
        <p className="ai-subtitle">Ask questions about your cash flow, expenses, or forecasts in plain English.</p>

        <form className="ai-form" onSubmit={handleAsk}>
          <input
            type="text"
            className="ai-input"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="e.g. What were my top three expenses this month?"
            aria-label="Your question"
            required
          />
          <button type="submit" className="ai-btn" disabled={loading || !query.trim()}>
            {loading ? 'Thinking...' : 'Ask AI'}
          </button>
        </form>

        <div className="ai-suggestions">
          {SUGGESTIONS.map((s) => (
            <button key={s} type="button" className="ai-chip" onClick={() => pickSuggestion(s)} disabled={loading}>
              {s}
            </button>
          ))}
        </div>

        {loading && (
          <div className="ai-response-box ai-thinking" aria-live="polite">
            <span className="ai-dot" />
            <span className="ai-dot" />
            <span className="ai-dot" />
          </div>
        )}

        {!loading && response && (
          <div className={`ai-response-box${failed ? ' ai-response-muted' : ''}`} aria-live="polite">
            <h3>{failed ? 'Coming soon' : 'AI Response'}</h3>
            <p>{response}</p>
          </div>
        )}
      </main>
    </div>
  );
};

export default AskAI;
