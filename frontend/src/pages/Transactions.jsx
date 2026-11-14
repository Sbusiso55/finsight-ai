
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './Transactions.css';

const Transactions = () => {

    const navigate = useNavigate();

    // Business information
    const [businesses, setBusinesses] = useState([]);
    const [selectedBusinessId, setSelectedBusinessId] = useState(null);
    const [businessesLoading, setBusinessesLoading] = useState(true);
    const [businessesError, setBusinessesError] = useState(false);

    // Transaction information
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(false);

    // Pagination
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const pageSize = 10;

    // Refresh transaction list after updates or deletions
    const [refreshKey, setRefreshKey] = useState(0);

    // Editing
    const [editingTransaction, setEditingTransaction] = useState(null);
    const [editForm, setEditForm] = useState({
        description: '',
        amount: '',
        type: 'INCOME',
        categoryId: '',
        date: ''
    });

    const [editCategories, setEditCategories] = useState([]);
    const [editCategoriesLoading, setEditCategoriesLoading] = useState(false);
    const [savingEdit, setSavingEdit] = useState(false);
    const [editError, setEditError] = useState('');

    // Deletion
    const [deletingId, setDeletingId] = useState(null);
    const [actionError, setActionError] = useState('');

    const currentBusiness = businesses.find(
        business => business.id === selectedBusinessId
    );


    // Fetch user's businesses.
    useEffect(() => {

        let ignore = false;

        const fetchBusinesses = async () => {
            try {
                const response = await api.get('/api/businesses/owner');

                if (!ignore) {
                    setBusinesses(response.data);

                    if (response.data.length > 0) {
                        setSelectedBusinessId(response.data[0].id);
                    }
                }

            } catch (error) {
                if (!ignore) {
                    console.error('Failed to fetch businesses:', error);
                    setBusinessesError(true);
                }

            } finally {
                if (!ignore) {
                    setBusinessesLoading(false);
                }
            }
        };

        void fetchBusinesses();

        return () => {
            ignore = true;
        };

    }, []);


    // Fetch transactions for the selected business and page.
    useEffect(() => {

        if (!selectedBusinessId) return;

        let ignore = false;

        const fetchTransactions = async () => {

            setLoading(true);
            setError(false);

            try {
                const response = await api.get('/api/transactions', {
                    params: {
                        businessId: selectedBusinessId,
                        page,
                        size: pageSize
                    }
                });

                if (!ignore) {
                    setTransactions(response.data.content || []);
                    setTotalPages(response.data.totalPages || 0);

                    console.log('Transactions fetched:', {
                        businessId: selectedBusinessId,
                        page,
                        totalElements: response.data.totalElements,
                        totalPages: response.data.totalPages,
                        transactions: response.data.content?.map(t => ({
                            id: t.id,
                            description: t.description,
                            amount: t.amount,
                            type: t.type,
                            transactionDate: t.transactionDate
                        }))
                    });
                }

            } catch (error) {
                if (!ignore) {
                    console.error('Failed to fetch transactions:', error);
                    setError(true);
                    setTransactions([]);
                }

            } finally {
                if (!ignore) {
                    setLoading(false);
                }
            }
        };

        void fetchTransactions();

        return () => {
            ignore = true;
        };

    }, [selectedBusinessId, page, refreshKey]);


    // Load categories when an edit form opens.
    useEffect(() => {

        if (!editingTransaction || !selectedBusinessId) return;

        let ignore = false;

        const fetchEditCategories = async () => {

            setEditCategoriesLoading(true);
            setEditCategories([]);

            try {
                const response = await api.get(
                    `/api/categories/business/${selectedBusinessId}`
                );

                if (!ignore) {
                    setEditCategories(response.data);
                }

            } catch (error) {
                if (!ignore) {
                    console.error('Failed to fetch edit categories:', error);
                    setEditError('Unable to load categories.');
                }

            } finally {
                if (!ignore) {
                    setEditCategoriesLoading(false);
                }
            }
        };

        void fetchEditCategories();

        return () => {
            ignore = true;
        };

    }, [editingTransaction, selectedBusinessId]);


    // Open the edit form with the existing transaction values.
    const handleEdit = (transaction) => {

        setEditError('');
        setActionError('');

        setEditingTransaction(transaction);

        setEditForm({
            description: transaction.description || '',
            amount: String(transaction.amount ?? ''),
            type: transaction.type,
            categoryId: String(transaction.category?.id ?? ''),
            date: transaction.transactionDate || ''
        });
    };


    // Update form state.
    const handleEditChange = (field, value) => {
        setEditForm(previous => ({
            ...previous,
            [field]: value
        }));
    };


    // Save transaction changes.
    const handleUpdate = async (e) => {

        e.preventDefault();
        setEditError('');

        const numericAmount = Number(editForm.amount);

        if (!Number.isFinite(numericAmount) || numericAmount <= 0) {
            setEditError('Enter a valid positive amount.');
            return;
        }

        if (!editForm.categoryId || !editForm.date) {
            setEditError('Select a category and transaction date.');
            return;
        }

        const transactionData = {
            description: editForm.description.trim(),
            amount: numericAmount,
            type: editForm.type,
            transactionDate: editForm.date,
            category: {
                id: Number(editForm.categoryId)
            }
        };

        try {
            setSavingEdit(true);

            await api.put(
                `/api/transactions/${editingTransaction.id}`,
                transactionData,
                {
                    params: {
                        businessId: selectedBusinessId
                    }
                }
            );

            setEditingTransaction(null);
            setRefreshKey(previous => previous + 1);

        } catch (error) {
            console.error('Failed to update transaction:', error);

            setEditError(
                error.response?.data?.message ||
                'Unable to update transaction. Please try again.'
            );

        } finally {
            setSavingEdit(false);
        }
    };


    // Delete transaction after confirmation.
    const handleDelete = async (transaction) => {

        console.log('[DELETE] Button clicked:', {
            transactionId: transaction.id,
            businessId: selectedBusinessId
        });

        const confirmed = window.confirm(
            `Are you sure you want to delete "${transaction.description}"?`
        );

        console.log('[DELETE] User confirmation:', confirmed);

        if (!confirmed) return;

        setActionError('');
        setDeletingId(transaction.id);

        try {
            console.log('[DELETE] Sending API request...');

            const response = await api.delete(
                `/api/transactions/${transaction.id}`,
                {
                    params: {
                        businessId: selectedBusinessId
                    }
                }
            );

            console.log('[DELETE] API response:', response.status);

            if (transactions.length === 1 && page > 0) {
                setPage(previous => previous - 1);
            } else {
                setRefreshKey(previous => previous + 1);
            }

        } catch (error) {

            console.error('[DELETE] Request failed:', {
                message: error.message,
                status: error.response?.status,
                response: error.response?.data
            });

            setActionError(
                error.response?.data?.message ||
                'Unable to delete transaction. Please try again.'
            );

        } finally {
            setDeletingId(null);
        }
    };

    // Format currency.
    const formatCurrency = (amount) =>
        new Intl.NumberFormat('en-ZA', {
            style: 'currency',
            currency: currentBusiness?.currency || 'ZAR'
        }).format(Math.abs(Number(amount || 0)));


    // Format transaction date.
    const formatDate = (date) => {

        if (!date) return '—';

        const parsedDate = new Date(`${date}T00:00:00`);

        if (Number.isNaN(parsedDate.getTime())) {
            return date;
        }

        return new Intl.DateTimeFormat('en-ZA', {
            day: 'numeric',
            month: 'short',
            year: 'numeric'
        }).format(parsedDate);
    };


    const filteredEditCategories = editCategories.filter(
        category => category.type === editForm.type
    );


    return (
        <div className="transactions-wrapper">

            <Navbar />

            <div className="transactions-content">

                <div className="transactions-header">

                    <div>
                        <h1 className="transactions-title">
                            Transactions
                        </h1>

                        <p className="transactions-subtitle">
                            All your income and expenses in one place.
                        </p>

                        {businessesLoading ? (
                            <p>Loading businesses...</p>
                        ) : businessesError ? (
                            <p>Unable to load businesses.</p>
                        ) : businesses.length === 0 ? (
                            <p>You haven't added a business yet.</p>
                        ) : businesses.length > 1 && (
                            <select
                                value={selectedBusinessId ?? ''}
                                disabled={!!editingTransaction}
                                onChange={(e) => {
                                    setSelectedBusinessId(Number(e.target.value));
                                    setPage(0);
                                }}
                            >
                                {businesses.map(business => (
                                    <option
                                        key={business.id}
                                        value={business.id}
                                    >
                                        {business.name}
                                    </option>
                                ))}
                            </select>
                        )}
                    </div>

                    <button
                        className="dashboard-add-btn"
                        onClick={() => navigate('/add-transaction')}
                    >
                        + Add Transaction
                    </button>

                </div>


                {actionError && (
                    <p className="form-error" role="alert">
                        {actionError}
                    </p>
                )}


                <div className="transactions-table">

                    <div className="trans-row header">
                        <span>Description</span>
                        <span>Amount</span>
                        <span>Type</span>
                        <span>Date</span>
                        <span>Actions</span>
                    </div>


                    {loading ? (

                        <p>Loading transactions...</p>

                    ) : error ? (

                        <p>Unable to load transactions. Please try again.</p>

                    ) : transactions.length > 0 ? (

                        transactions.map(transaction => {

                            const isIncome = transaction.type === 'INCOME';

                            return (
                                <div
                                    className="trans-row"
                                    key={transaction.id}
                                >

                                    <span>
                                        {transaction.description || 'No description'}
                                    </span>

                                    <span className={
                                        isIncome ? 'positive' : 'negative'
                                    }>
                                        {isIncome ? '+' : '-'}
                                        {formatCurrency(transaction.amount)}
                                    </span>

                                    <span className={
                                        `trans-type ${isIncome ? 'income' : 'expense'}`
                                    }>
                                        {isIncome ? 'Income' : 'Expense'}
                                    </span>

                                    <span>
                                        {formatDate(transaction.transactionDate)}
                                    </span>

                                    <div className="trans-actions">

                                        <button
                                            className="trans-action-btn edit"
                                            onClick={() => handleEdit(transaction)}
                                            disabled={deletingId === transaction.id}
                                        >
                                            Edit
                                        </button>

                                        <button
                                            type="button"
                                            className="trans-action-btn delete"
                                            onClick={() => handleDelete(transaction)}
                                            disabled={deletingId === transaction.id}
                                        >
                                            {deletingId === transaction.id ? 'Deleting...' : 'Delete'}
                                        </button>

                                    </div>

                                </div>
                            );
                        })

                    ) : (

                        <p>No transactions found for this business.</p>

                    )}

                </div>


                {!loading && !error && totalPages > 1 && (
                    <div className="transactions-pagination">

                        <button
                            onClick={() => setPage(previous => previous - 1)}
                            disabled={page === 0}
                        >
                            Previous
                        </button>

                        <span>
                            Page {page + 1} of {totalPages}
                        </span>

                        <button
                            onClick={() => setPage(previous => previous + 1)}
                            disabled={page >= totalPages - 1}
                        >
                            Next
                        </button>

                    </div>
                )}

            </div>


            {/* Edit transaction modal */}
            {editingTransaction && (
                <div
                    className="edit-modal-backdrop"
                    onClick={(e) => {
                        if (e.target === e.currentTarget && !savingEdit) {
                            setEditingTransaction(null);
                        }
                    }}
                >

                    <div
                        className="edit-modal"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="edit-transaction-title"
                    >

                        <h2 id="edit-transaction-title">
                            Edit Transaction
                        </h2>

                        <p className="edit-modal-subtitle">
                            Update the transaction details below.
                        </p>


                        <form onSubmit={handleUpdate}>

                            <div className="add-trans-field">
                                <label htmlFor="edit-description">
                                    Description
                                </label>

                                <input
                                    id="edit-description"
                                    type="text"
                                    value={editForm.description}
                                    onChange={(e) =>
                                        handleEditChange('description', e.target.value)
                                    }
                                    required
                                />
                            </div>


                            <div className="add-trans-row">

                                <div className="add-trans-field">
                                    <label htmlFor="edit-amount">
                                        Amount (R)
                                    </label>

                                    <input
                                        id="edit-amount"
                                        type="number"
                                        min="0.01"
                                        step="0.01"
                                        value={editForm.amount}
                                        onChange={(e) =>
                                            handleEditChange('amount', e.target.value)
                                        }
                                        required
                                    />
                                </div>

                                <div className="add-trans-field">
                                    <label htmlFor="edit-type">
                                        Type
                                    </label>

                                    <select
                                        id="edit-type"
                                        value={editForm.type}
                                        onChange={(e) => {
                                            handleEditChange('type', e.target.value);
                                            handleEditChange('categoryId', '');
                                        }}
                                    >
                                        <option value="INCOME">Income</option>
                                        <option value="EXPENSE">Expense</option>
                                    </select>
                                </div>

                            </div>


                            <div className="add-trans-row">

                                <div className="add-trans-field">
                                    <label htmlFor="edit-category">
                                        Category
                                    </label>

                                    <select
                                        id="edit-category"
                                        value={editForm.categoryId}
                                        onChange={(e) =>
                                            handleEditChange('categoryId', e.target.value)
                                        }
                                        disabled={
                                            editCategoriesLoading ||
                                            filteredEditCategories.length === 0
                                        }
                                        required
                                    >
                                        <option value="">
                                            {editCategoriesLoading
                                                ? 'Loading categories...'
                                                : 'Select a category'}
                                        </option>

                                        {filteredEditCategories.map(category => (
                                            <option
                                                key={category.id}
                                                value={category.id}
                                            >
                                                {category.name}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                <div className="add-trans-field">
                                    <label htmlFor="edit-date">
                                        Date
                                    </label>

                                    <input
                                        id="edit-date"
                                        type="date"
                                        value={editForm.date}
                                        onChange={(e) =>
                                            handleEditChange('date', e.target.value)
                                        }
                                        required
                                    />
                                </div>

                            </div>


                            {editError && (
                                <p className="form-error" role="alert">
                                    {editError}
                                </p>
                            )}


                            <div className="edit-modal-actions">

                                <button
                                    type="button"
                                    className="edit-cancel-btn"
                                    disabled={savingEdit}
                                    onClick={() => setEditingTransaction(null)}
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className="edit-save-btn"
                                    disabled={
                                        savingEdit ||
                                        editCategoriesLoading ||
                                        filteredEditCategories.length === 0
                                    }
                                >
                                    {savingEdit ? 'Saving...' : 'Save Changes'}
                                </button>

                            </div>

                        </form>

                    </div>

                </div>
            )}

        </div>
    );
};

export default Transactions;
