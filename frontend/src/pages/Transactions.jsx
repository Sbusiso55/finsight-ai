import React from 'react';
import Navbar from '../components/Navbar';
import './Transactions.css';

const Transactions = () => {
    const transactions = [
        { id: 1, description: 'Customer 001 Payment', amount: 8000, type: 'Income', date: 'Mar 5, 2026' },
        { id: 2, description: 'Staff Wages', amount: -9000, type: 'Expense', date: 'Mar 1, 2026' },
        { id: 3, description: "Anita's Retail", amount: 12500, type: 'Income', date: 'Mar 12, 2026' },
        { id: 4, description: 'Rent Payment', amount: -6000, type: 'Expense', date: 'Mar 1, 2026' },
        { id: 5, description: 'Customer 002 Payment', amount: 4500, type: 'Income', date: 'Mar 18, 2026' },
        { id: 6, description: 'Supplies', amount: -4800, type: 'Expense', date: 'Mar 10, 2026' },
        { id: 7, description: 'Customer 003 Payment', amount: 5000, type: 'Income', date: 'Mar 22, 2026' },
        { id: 8, description: 'Marketing', amount: -3000, type: 'Expense', date: 'Mar 15, 2026' },
        { id: 9, description: 'Utilities', amount: -2000, type: 'Expense', date: 'Mar 5, 2026' },
    ];

    return (
        <div className="transactions-wrapper">
            <Navbar />
            <div className="transactions-content">
                <div className="transactions-header">
                    <h1 className="transactions-title">Transactions</h1>
                    <p className="transactions-subtitle">All your income and expenses in one place.</p>
                </div>
                <div className="transactions-table">
                    <div className="trans-row header">
                        <span>Description</span>
                        <span>Amount</span>
                        <span>Type</span>
                        <span>Date</span>
                    </div>
                    {transactions.map((t) => (
                        <div className="trans-row" key={t.id}>
                            <span>{t.description}</span>
                            <span className={t.amount > 0 ? 'positive' : 'negative'}>
                                {t.amount > 0 ? '+' : ''}R{Math.abs(t.amount).toLocaleString()}
                            </span>
                            <span className={`trans-type ${t.type.toLowerCase()}`}>{t.type}</span>
                            <span>{t.date}</span>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default Transactions;
