import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import './Transactions.css';

const Transactions = () => {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('ALL');
    const [searchTerm, setSearchTerm] = useState('');
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        if (!token) {
            navigate('/login');
            return;
        }
        fetchTransactions();
    }, [currentPage]);

    const fetchTransactions = async () => {
        try {
            const response = await fetch(`/api/transactions?page=${currentPage}&size=15`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            if (response.ok) {
                const data = await response.json();
                setTransactions(data.content || data);
                setTotalPages(data.totalPages || 1);
            }
        } catch (error) {
            console.error('Failed to fetch transactions:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this transaction?')) return;
        try {
            const response = await fetch(`/api/transactions/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) {
                setTransactions(transactions.filter(t => t.id !== id));
            }
        } catch (error) {
            console.error('Failed to delete transaction:', error);
        }
    };

    const formatCurrency = (amount) => {
        if (!amount) return 'R0.00';
        return `R${parseFloat(amount).toLocaleString('en-ZA', { minimumFractionDigits: 2 })}`;
    };

    const filteredTransactions = transactions.filter(txn => {
        const matchesFilter = filter === 'ALL' || txn.type === filter;
        const matchesSearch = !searchTerm ||
            (txn.description && txn.description.toLowerCase().includes(searchTerm.toLowerCase())) ||
            (txn.category && txn.category.toLowerCase().includes(searchTerm.toLowerCase()));
        return matchesFilter && matchesSearch;
    });

    if (loading) {
        return (
            <div className="page-loading">
                <div className="loading-spinner"></div>
                <p>Loading transactions...</p>
            </div>
        );
    }

    return (
        <div className="page-container">
            <Navbar />
            <main className="page-main">
                <div className="page-header">
                    <div>
                        <h1>Transactions</h1>
                        <p>View and manage all your financial transactions.</p>
                    </div>
                    <button className="primary-btn" onClick={() => navigate('/add-transaction')}>
                        + Add Transaction
                    </button>
                </div>

                <div className="filters-bar">
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search transactions..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <div className="filter-buttons">
                        {['ALL', 'INCOME', 'EXPENSE'].map((type) => (
                            <button
                                key={type}
                                className={`filter-btn ${filter === type ? 'active' : ''}`}
                                onClick={() => setFilter(type)}
                            >
                                {type === 'ALL' ? 'All' : type === 'INCOME' ? 'Income' : 'Expenses'}
                            </button>
                        ))}
                    </div>
                </div>

                <div className="data-table">
                    <div className="data-table-header">
                        <span>Description</span>
                        <span>Category</span>
                        <span>Type</span>
                        <span>Amount</span>
                        <span>Date</span>
                        <span>Actions</span>
                    </div>
                    {filteredTransactions.length > 0 ? (
                        filteredTransactions.map((txn, index) => (
                            <div key={index} className="data-table-row">
                                <span>{txn.description || 'N/A'}</span>
                                <span>{txn.category || 'Uncategorized'}</span>
                                <span>
                                    <span className={`type-badge ${txn.type === 'INCOME' ? 'income' : 'expense'}`}>
                                        {txn.type}
                                    </span>
                                </span>
                                <span className={txn.type === 'INCOME' ? 'amount-positive' : 'amount-negative'}>
                                    {txn.type === 'INCOME' ? '+' : '-'}{formatCurrency(txn.amount)}
                                </span>
                                <span>{txn.date || 'N/A'}</span>
                                <span>
                                    <button className="delete-btn" onClick={() => handleDelete(txn.id)}>
                                        Delete
                                    </button>
                                </span>
                            </div>
                        ))
                    ) : (
                        <div className="no-data">No transactions found.</div>
                    )}
                </div>

                {totalPages > 1 && (
                    <div className="pagination">
                        <button
                            className="page-btn"
                            disabled={currentPage === 0}
                            onClick={() => setCurrentPage(currentPage - 1)}
                        >
                            Previous
                        </button>
                        <span className="page-info">Page {currentPage + 1} of {totalPages}</span>
                        <button
                            className="page-btn"
                            disabled={currentPage >= totalPages - 1}
                            onClick={() => setCurrentPage(currentPage + 1)}
                        >
                            Next
                        </button>
                    </div>
                )}
            </main>
        </div>
    );
};

export default Transactions;
