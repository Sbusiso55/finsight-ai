import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import './AskAI.css';

const AskAI = () => {
    const [message, setMessage] = useState('');
    const [messages, setMessages] = useState([
        { role: 'ai', text: 'Hi, I\'m FinSight AI. Ask me anything about your finances — revenue, expenses, forecasts, or advice.' }
    ]);

    const handleSend = (e) => {
        e.preventDefault();
        if (!message.trim()) return;

        setMessages([...messages, { role: 'user', text: message }, { role: 'ai', text: 'I\'m analysing your financial data. This feature will be connected to the AI engine soon.' }]);
        setMessage('');
    };

    return (
        <div className="askai-wrapper">
            <Navbar />
            <div className="askai-content">
                <div className="askai-header">
                    <h1 className="askai-title">Ask AI</h1>
                    <p className="askai-subtitle">Have a conversation with your finances.</p>
                </div>
                <div className="askai-chat">
                    <div className="askai-messages">
                        {messages.map((msg, i) => (
                            <div key={i} className={`askai-msg ${msg.role}`}>
                                <div className="askai-msg-bubble">
                                    {msg.text}
                                </div>
                            </div>
                        ))}
                    </div>
                    <form className="askai-input-area" onSubmit={handleSend}>
                        <input
                            type="text"
                            placeholder="Ask about your revenue, expenses, forecasts..."
                            value={message}
                            onChange={(e) => setMessage(e.target.value)}
                        />
                        <button type="submit">Send</button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default AskAI;
