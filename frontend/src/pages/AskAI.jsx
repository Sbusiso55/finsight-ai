import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './AskAI.css';

const AskAI = () => {
  const [query, setQuery] = useState('');
  const [response, setResponse] = useState('');
  const [loading, setLoading] = useState(false);

  const handleAsk = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;
    setLoading(true);
    setResponse('');

    try {
      const res = await api.post('/api/ai/query', { prompt: query });
      setResponse(res.data?.answer || res.data?.message || 'Analysis complete.');
    } catch (err) {
      console.error('AI query failed:', err);
      setResponse('Unable to reach Clearside Pay analyst at the moment.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="ai-layout">
      <Navbar />
      <main className="ai-container">
        <h1>Clearside Pay Analyst</h1>
        <p className="ai-subtitle">Ask questions about your cash flow, expenses, or forecasts in plain English.</p>

        <form className="ai-form" onSubmit={handleAsk}>
          <input
            type="text"
            className="ai-input"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="e.g. What were my top three expenses this month?"
            required
          />
          <button type="submit" className="ai-btn" disabled={loading}>
            {loading ? 'Analyzing...' : 'Ask AI'}
          </button>
        </form>

        {response && (
          <div className="ai-response-box">
            <h3>AI Response</h3>
            <p>{response}</p>
          </div>
        )}
      </main>
    </div>
  );
};

export default AskAI;