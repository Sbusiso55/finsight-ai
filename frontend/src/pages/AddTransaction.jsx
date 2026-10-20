import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import './AddTransaction.css';

const AddTransaction = () => {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const [formData, setFormData] = useState({
        description: '',
        amount: '',
        type: 'EXPENSE',
        category: '',
        date: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        if (!formData.description || !formData.amount || !formData.date) {
            setError('Please fill in all required fields.');
            setLoading(false);
            return;
        }

        try {
            const response = await fetch('/api/transactions', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    ...formData,
                    amount: parseFloat(formData.amount)
                })
            });

            if (response.ok) {
                navigate('/transactions');
            } else {
                setError('Failed to add transaction. Please try again.');
            }
        } catch (err) {
            setError('Something went wrong. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const categories = [
        'Salaries', 'Rent', 'Utilities', 'Marketing',
        'Supplies', 'Transport', 'Insurance', 'Sales',
        'Services', 'Consulting', 'Other'
    ];

    return (
        <div className="page-container">
            <Navbar />
            <main className="page-main">
                <div className="page-header">
                    <div>
                        <h1>Add Transaction</h1>
                        <p>Record a new income or expense transaction.</p>
                    </div>
                    <button className="secondary-btn" onClick={() => navigate('/transactions')}>
                        Back to Transactions
                    </button>
                </div>

                <div className="form-container">
                    <form onSubmit={handleSubmit}>
                        {error && <div className="form-error">{error}</div>}

                        <div className="form-group">
                            <label>Description *</label>
                            <input
                                type="text"
                                name="description"
                                value={formData.description}
                                onChange={handleChange}
                                placeholder="What was this transaction for?"
                            />
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label>Amount (R) *</label>
                                <input
                                    type="number"
                                    name="amount"
                                    value={formData.amount}
                                    onChange={handleChange}
                                    placeholder="0.00"
                                    step="0.01"
                                    min="0"
                                />
                            </div>
                            <div className="form-group">
                                <label>Type</label>
                                <select name="type" value={formData.type} onChange={handleChange}>
                                    <option value="EXPENSE">Expense</option>
                                    <option value="INCOME">Income</option>
                                </select>
                            </div>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label>Category</label>
                                <select name="category" value={formData.category} onChange={handleChange}>
                                    <option value="">Select a category</option>
                                    {categories.map((cat) => (
                                        <option key={cat} value={cat}>{cat}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group">
                                <label>Date *</label>
                                <input
                                    type="date"
                                    name="date"
                                    value={formData.date}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>

                        <button type="submit" className="submit-btn" disabled={loading}>
                            {loading ? 'Adding...' : 'Add Transaction'}
                        </button>
                    </form>
                </div>
            </main>
        </div>
    );
};

export default AddTransaction;
