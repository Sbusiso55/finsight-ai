import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import './AskAI.css';

const AskAI = () => {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSend = async (e) => {
        e.preventDefault();
        if (!input.trim() || loading) return;

        const userMessage = { role: 'user', content: input };
        setMessages(prev => [...prev, userMessage]);
        setInput('');
        setLoading(true);

        try {
            const response = await fetch('/api/ai/ask', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ question: input })
            });

            if (response.ok) {
                const data = await response.json();
                const aiMessage = {
                    role: 'assistant',
                    content: data.answer || data.response || 'I could not generate a response.'
                };
                setMessages(prev => [...prev, aiMessage]);
            } else {
                setMessages(prev => [...prev, {
                    role: 'assistant',
                    content: 'Something went wrong. Please try again.'
                }]);
            }
        } catch (error) {
            setMessages(prev => [...prev, {
                role: 'assistant',
                content: 'Could not connect to the AI service.'
            }]);
        } finally {
            setLoading(false);
        }
    };

    const suggestedQuestions = [
        'How is my business doing this month?',
        'What are my biggest expenses?',
        'Show me my profit margins',
        'What does my cash flow look like?',
        'Give me financial advice for next month'
    ];

    return (
        <div className="page-container">
            <Navbar />
            <main className="page-main">
                <div className="ai-page">
                    <div className="ai-page-header">
                        <h1>Ask FinSight AI</h1>
                        <p>Get instant AI-powered insights about your finances.</p>
                    </div>

                    <div className="chat-container">
                        {messages.length === 0 ? (
                            <div className="chat-empty">
                                <h2>What would you like to know?</h2>
                                <p>Ask me anything about your business finances.</p>
                                <div className="suggested-questions">
                                    {suggestedQuestions.map((q, index) => (
                                        <button
                                            key={index}
                                            className="suggestion-btn"
                                            onClick={() => setInput(q)}
                                        >
                                            {q}
                                        </button>
                                    ))}
                                </div>
                            </div>
                        ) : (
                            <div className="chat-messages">
                                {messages.map((msg, index) => (
                                    <div key={index} className={`chat-message ${msg.role}`}>
                                        <div className="message-label">
                                            {msg.role === 'user' ? 'You' : 'FinSight AI'}
                                        </div>
                                        <div className="message-content">
                                            {msg.content}
                                        </div>
                                    </div>
                                ))}
                                {loading && (
                                    <div className="chat-message assistant">
                                        <div className="message-label">FinSight AI</div>
                                        <div className="message-content typing">Thinking...</div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>

                    <form className="chat-input-form" onSubmit={handleSend}>
                        <input
                            type="text"
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder="Ask about your finances..."
                            className="chat-input"
                            disabled={loading}
                        />
                        <button type="submit" className="chat-send-btn" disabled={loading || !input.trim()}>
                            {loading ? '...' : 'Send'}
                        </button>
                    </form>
                </div>
            </main>
        </div>
    );
};

export default AskAI;
