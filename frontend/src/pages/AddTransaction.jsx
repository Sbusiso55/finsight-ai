
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './AddTransaction.css';

const AddTransaction = () => {

    const navigate = useNavigate();

    // Form fields
    const [description, setDescription] = useState('');
    const [amount, setAmount] = useState('');
    const [type, setType] = useState('INCOME');
    const [categoryId, setCategoryId] = useState('');
    const [date, setDate] = useState('');
    const [selectedBusinessId, setSelectedBusinessId] = useState(null);

    // Businesses
    const [businesses, setBusinesses] = useState([]);
    const [businessesLoading, setBusinessesLoading] = useState(true);
    const [businessesError, setBusinessesError] = useState('');

    // Categories
    const [categories, setCategories] = useState([]);
    const [categoriesLoading, setCategoriesLoading] = useState(false);
    const [categoriesError, setCategoriesError] = useState('');

    // Form submission
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');

    // Fetch businesses belonging to the logged-in user.
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
                    setBusinessesError('Unable to load your businesses.');
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


    // Fetch categories whenever the selected business changes.
    useEffect(() => {

        if (!selectedBusinessId) return;

        let ignore = false;

        const fetchCategories = async () => {

            setCategoriesLoading(true);
            setCategoriesError('');
            setCategories([]);
            setCategoryId('');

            try {
                const response = await api.get(
                    `/api/categories/business/${selectedBusinessId}`
                );

                if (!ignore) {
                    setCategories(response.data);
                }

            } catch (error) {
                if (!ignore) {
                    console.error('Failed to fetch categories:', error);
                    setCategoriesError('Unable to load categories.');
                }

            } finally {
                if (!ignore) {
                    setCategoriesLoading(false);
                }
            }
        };

        void fetchCategories();

        return () => {
            ignore = true;
        };

    }, [selectedBusinessId]);


    // Only display categories matching the selected transaction type.
    const filteredCategories = categories.filter(
        category => category.type === type
    );


    // Submit the transaction to the backend.
    const handleSubmit = async (e) => {

        e.preventDefault();

        setSubmitError('');

        if (!selectedBusinessId) {
            setSubmitError('Please select a business.');
            return;
        }

        if (!categoryId) {
            setSubmitError('Please select a category.');
            return;
        }

        if (Number(amount) <= 0 || !Number.isFinite(Number(amount))) {
            setSubmitError('Please enter a valid positive amount.');
            return;
        }

        const transactionData = {
            description: description.trim(),
            amount: Number(amount),
            type,
            transactionDate: date,
            business: {
                id: Number(selectedBusinessId)
            },
            category: {
                id: Number(categoryId)
            }
        };

        try {
            setSubmitting(true);

            const response = await api.post(
                '/api/transactions',
                transactionData
            );

            console.log('Transaction created:', response.data);

            // Navigate only after successful creation.
            navigate('/transactions');

        } catch (error) {
            console.error('Failed to create transaction:', error);

            setSubmitError(
                error.response?.data?.message ||
                'Unable to save transaction. Please try again.'
            );

        } finally {
            setSubmitting(false);
        }
    };


    return (
        <div className="add-trans-wrapper">

            <Navbar />

            <div className="add-trans-content">

                <h1 className="add-trans-title">
                    Add Transaction
                </h1>

                <p className="add-trans-subtitle">
                    Record a new income or expense.
                </p>

                {businessesLoading ? (
                    <p>Loading businesses...</p>
                ) : businessesError ? (
                    <p>{businessesError}</p>
                ) : businesses.length === 0 ? (
                    <p>You need to add a business before recording transactions.</p>
                ) : (

                    <form
                        className="add-trans-form"
                        onSubmit={handleSubmit}
                    >

                        {/* Business selection */}
                        <div className="add-trans-field">
                            <label htmlFor="business">
                                Business
                            </label>

                            <select
                                id="business"
                                value={selectedBusinessId ?? ''}
                                onChange={(e) => {
                                    setSelectedBusinessId(Number(e.target.value));
                                    setCategoryId('');
                                }}
                                required
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
                        </div>


                        {/* Description */}
                        <div className="add-trans-field">
                            <label htmlFor="description">
                                Description
                            </label>

                            <input
                                id="description"
                                type="text"
                                placeholder="What was this for?"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                required
                            />
                        </div>


                        {/* Amount and transaction type */}
                        <div className="add-trans-row">

                            <div className="add-trans-field">
                                <label htmlFor="amount">
                                    Amount (R)
                                </label>

                                <input
                                    id="amount"
                                    type="number"
                                    min="0.01"
                                    step="0.01"
                                    placeholder="0.00"
                                    value={amount}
                                    onChange={(e) => setAmount(e.target.value)}
                                    required
                                />
                            </div>

                            <div className="add-trans-field">
                                <label htmlFor="type">
                                    Type
                                </label>

                                <select
                                    id="type"
                                    value={type}
                                    onChange={(e) => {
                                        setType(e.target.value);
                                        setCategoryId('');
                                    }}
                                >
                                    <option value="INCOME">Income</option>
                                    <option value="EXPENSE">Expense</option>
                                </select>
                            </div>

                        </div>


                        {/* Category and date */}
                        <div className="add-trans-row">

                            <div className="add-trans-field">
                                <label htmlFor="category">
                                    Category
                                </label>

                                <select
                                    id="category"
                                    value={categoryId}
                                    onChange={(e) => setCategoryId(e.target.value)}
                                    disabled={categoriesLoading || !!categoriesError || filteredCategories.length === 0}
                                    required
                                >
                                    <option value="">
                                        {categoriesLoading
                                            ? 'Loading categories...'
                                            : categoriesError
                                                ? 'Categories unavailable'
                                                : filteredCategories.length === 0
                                                    ? 'No categories available'
                                                    : 'Select a category'}
                                    </option>

                                    {filteredCategories.map(category => (
                                        <option
                                            key={category.id}
                                            value={category.id}
                                        >
                                            {category.name}
                                        </option>
                                    ))}
                                </select>

                                {categoriesError && (
                                    <p role="alert">
                                        {categoriesError}
                                    </p>
                                )}
                            </div>


                            <div className="add-trans-field">
                                <label htmlFor="date">
                                    Date
                                </label>

                                <input
                                    id="date"
                                    type="date"
                                    value={date}
                                    onChange={(e) => setDate(e.target.value)}
                                    required
                                />
                            </div>

                        </div>


                        {/* Submission error */}
                        {submitError && (
                            <p role="alert" className="form-error">
                                {submitError}
                            </p>
                        )}


                        <button
                            type="submit"
                            className="add-trans-submit"
                            disabled={
                                submitting ||
                                categoriesLoading ||
                                !!categoriesError ||
                                filteredCategories.length === 0
                            }
                        >
                            {submitting
                                ? 'Saving...'
                                : 'Save Transaction'}
                        </button>

                    </form>
                )}

            </div>

        </div>
    );
};

export default AddTransaction;
