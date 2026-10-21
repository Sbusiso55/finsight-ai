import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import './AddTransaction.css';

const AddTransaction = () => {
    const navigate = useNavigate();
    const [description, setDescription] = useState('');
    const [amount, setAmount] = useState('');
    const [type, setType] = useState('Income');
    const [category, setCategory] = useState('');
    const [date, setDate] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        navigate('/transactions');
    };

    return (
        <div className="add-trans-wrapper">
            <Navbar />
            <div className="add-trans-content">
                <h1 className="add-trans-title">Add Transaction</h1>
                <p className="add-trans-subtitle">Record a new income or expense.</p>
                <form className="add-trans-form" onSubmit={handleSubmit}>
                    <div className="add-trans-field">
                        <label>Description</label>
                        <input type="text" placeholder="What was this for?" value={description} onChange={(e) => setDescription(e.target.value)} required />
                    </div>
                    <div className="add-trans-row">
                        <div className="add-trans-field">
                            <label>Amount (R)</label>
                            <input type="number" placeholder="0.00" value={amount} onChange={(e) => setAmount(e.target.value)} required />
                        </div>
                        <div className="add-trans-field">
                            <label>Type</label>
                            <select value={type} onChange={(e) => setType(e.target.value)}>
                                <option value="Income">Income</option>
                                <option value="Expense">Expense</option>
                            </select>
                        </div>
                    </div>
                    <div className="add-trans-row">
                        <div className="add-trans-field">
                            <label>Category</label>
                            <input type="text" placeholder="e.g. Rent, Wages, Sales" value={category} onChange={(e) => setCategory(e.target.value)} required />
                        </div>
                        <div className="add-trans-field">
                            <label>Date</label>
                            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
                        </div>
                    </div>
                    <button type="submit" className="add-trans-submit">Save Transaction</button>
                </form>
            </div>
        </div>
    );
};

export default AddTransaction;
