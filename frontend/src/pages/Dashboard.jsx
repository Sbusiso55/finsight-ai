import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './Dashboard.css';

// Returns the current month in YYYY-MM format.
const getCurrentPeriodKey = () => {
    const today = new Date();

    return `${today.getFullYear()}-${String(
        today.getMonth() + 1
    ).padStart(2, '0')}`;
};


// Creates the available reporting periods.
// Shows the current month and the previous three months.
const getReportingPeriodOptions = () => {
    const today = new Date();

    return Array.from({ length: 4 }, (_, index) => {
        const date = new Date(
            today.getFullYear(),
            today.getMonth() - index,
            1
        );

        const value = `${date.getFullYear()}-${String(
            date.getMonth() + 1
        ).padStart(2, '0')}`;

        return {
            value,
            label: index === 0
                ? 'This month'
                : date.toLocaleDateString('en-ZA', {
                    month: 'long',
                    year: 'numeric'
                })
        };
    });
};


// Converts the selected month into API date parameters.
const getReportingDateRange = (periodKey) => {
    const [year, month] = periodKey.split('-').map(Number);

    const today = new Date();

    const fromDate = new Date(year, month - 1, 1);

    const isCurrentMonth =
        year === today.getFullYear() &&
        month - 1 === today.getMonth();

    const toDate = isCurrentMonth
        ? today
        : new Date(year, month, 0);

    const formatDate = (date) =>
        `${date.getFullYear()}-${String(
            date.getMonth() + 1
        ).padStart(2, '0')}-${String(
            date.getDate()
        ).padStart(2, '0')}`;

    return {
        from: formatDate(fromDate),
        to: formatDate(toDate)
    };
};

const Dashboard = () => {
    const navigate = useNavigate();

    // Store the logged-in user's details
    const [user, setUser] = useState(null);
    const [businesses, setBusinesses] = useState([]);
    const [selectedBusinessId, setSelectedBusinessId] = useState(null);
    const [businessesLoading, setBusinessesLoading] = useState(true);
    const [businessesError, setBusinessesError] = useState(false);

    const [financialData, setFinancialData] = useState(null);
    const [financialLoading, setFinancialLoading] = useState(false);
    const [financialError, setFinancialError] = useState(false);
    const [selectedPeriod, setSelectedPeriod] = useState(
        getCurrentPeriodKey
    );

    const currentBusiness = businesses.find(
        business => business.id === selectedBusinessId
    );

    // Fetch the logged-in user's details when Dashboard loads
    useEffect(() => {
        const fetchUser = async () => {
            try {
                const response = await api.get('/api/users/user');
                setUser(response.data);
            } catch (error) {
                console.error('Failed to fetch user:', error);
            }
        };

       void fetchUser();
    }, []);


    useEffect(() => {
        if (!user) return;

        const fetchBusinesses = async () => {
            try {
                const response = await api.get('/api/businesses/owner');

                setBusinesses(response.data);

                if (response.data.length > 0) {
                    setSelectedBusinessId(response.data[0].id);
                }

                console.log('User businesses:', response.data);

            } catch (error) {
                console.error('Failed to fetch businesses:', error);
                setBusinessesError(true);

            } finally {
                setBusinessesLoading(false);
            }
        };

        void fetchBusinesses();
    }, [user]);

    useEffect(() => {
        if (!selectedBusinessId) return;

        let ignore = false;

        const fetchFinancialData = async () => {
            setFinancialLoading(true);
            setFinancialError(false);
            setFinancialData(null);

            const { from, to } = getReportingDateRange(selectedPeriod);

            try {
                const [
                    revenue,
                    expenses,
                    netProfitMargin,
                    expenseBreakdown,
                    transactions
                ] = await Promise.all([
                    api.get(`/api/businesses/${selectedBusinessId}/revenue/total`, {
                        params: { from, to }
                    }),

                    api.get('/api/expenses/total', {
                        params: { businessId: selectedBusinessId, from, to }
                    }),

                    api.get(`/api/businesses/${selectedBusinessId}/revenue/net-profit-margin`, {
                        params: { from, to }
                    }),

                    api.get('/api/expenses/breakdown/percentages', {
                        params: { businessId: selectedBusinessId, from, to }
                    }),

                    api.get('/api/transactions', {
                        params: {
                            businessId: selectedBusinessId,
                            page: 0,
                            size: 5
                        }
                    })
                ]);

                if (!ignore) {
                    setFinancialData({
                        revenue: revenue.data,
                        expenses: expenses.data,
                        netProfitMargin: netProfitMargin.data,
                        expenseBreakdown: expenseBreakdown.data,
                        transactions: transactions.data
                    });

                    console.log('Dashboard financial data:', {
                        businessId: selectedBusinessId,
                        businessName: businesses.find(
                            business => business.id === selectedBusinessId
                        )?.name,

                        reportingPeriod: {
                            from,
                            to
                        },

                        revenue: revenue.data,
                        expenses: expenses.data,
                        netProfitMargin: netProfitMargin.data,
                        expenseBreakdown: expenseBreakdown.data,

                        transactionCount: transactions.data?.content?.length
                    });
                }

            } catch (error) {
                if (!ignore) {
                    console.error('Failed to fetch dashboard data:', error);
                    setFinancialError(true);
                }
            } finally {
                if (!ignore) {
                    setFinancialLoading(false);
                }
            }
        };

        void fetchFinancialData();

        return () => {
            ignore = true;
        };
    }, [selectedBusinessId, selectedPeriod]);

// Format financial amounts using the business currency
    const formatCurrency = (amount) =>
        new Intl.NumberFormat('en-ZA', {
            style: 'currency',
            currency: currentBusiness?.currency || 'ZAR'
        }).format(Number(amount || 0));

    return (
        <div className="dashboard-wrapper">
            <Navbar />

            <div className="dashboard-content">

                <div className="dashboard-header">
                    <div className="dashboard-period-selector">
                        <label htmlFor="reporting-period">
                            Reporting Period
                        </label>

                        <select
                            id="reporting-period"
                            value={selectedPeriod}
                            onChange={(e) => setSelectedPeriod(e.target.value)}
                        >
                            {getReportingPeriodOptions().map(period => (
                                <option key={period.value} value={period.value}>
                                    {period.label}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div>
                        <p className="dashboard-greeting">
                            Good evening, {user ? user.fullName : '...'}
                        </p>

                        <h1 className="dashboard-title">
                            {currentBusiness
                                ? currentBusiness.name
                                : 'Welcome back.'}
                        </h1>
                        {businessesLoading ? (
                            <p>Loading your businesses...</p>
                        ) : businessesError ? (
                            <p>Unable to load your businesses. Please try again.</p>
                        ) : businesses.length === 0 ? (
                            <p>You haven't added a business yet.</p>
                        ) : businesses.length > 1 && (
                            <select
                                value={selectedBusinessId ?? ''}
                                onChange={(e) =>
                                    setSelectedBusinessId(Number(e.target.value))
                                }
                            >
                                {businesses.map(business => (
                                    <option key={business.id} value={business.id}>
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

                <div className="dashboard-cards">
                    <div className="dash-card">
                        <p className="dash-card-label">Total Revenue</p>
                        <h2 className="dash-card-value">
                            {financialLoading
                                ? 'Loading...'
                                : financialError
                                    ? 'Unavailable'
                                    : formatCurrency(financialData?.revenue)}
                        </h2>
                        <p className="dash-card-change positive">
                            Revenue this month
                        </p>
                    </div>

                    <div className="dash-card">
                        <p className="dash-card-label">Total Expenses</p>
                        <h2 className="dash-card-value">
                            {financialLoading
                                ? 'Loading...'
                                : financialError
                                    ? 'Unavailable'
                                    : formatCurrency(financialData?.expenses)}
                        </h2>
                        <p className="dash-card-change negative">
                            Expenses this month
                        </p>
                    </div>


                    <div className="dash-card">
                        <p className="dash-card-label">Net Profit</p>
                        <h2 className="dash-card-value">
                            {financialLoading
                                ? 'Loading...'
                                : financialError
                                    ? 'Unavailable'
                                    : formatCurrency(
                                        Number(financialData?.revenue || 0) -
                                        Number(financialData?.expenses || 0)
                                    )}
                        </h2>
                        <p className="dash-card-change positive">
                            {financialData
                                ? `${Number(financialData.netProfitMargin).toFixed(2)}% margin`
                                : 'Net profit margin'}
                        </p>
                    </div>
                </div>

                <div className="dashboard-sections">
                    <div className="dash-section">
                        <h3 className="dash-section-title">
                            Recent Transactions
                        </h3>


                        <div className="dash-table">
                            <div className="dash-row header">
                                <span>Description</span>
                                <span>Amount</span>
                                <span>Type</span>
                                <span>Date</span>
                            </div>


                            {financialLoading ? (
                                <p>Loading transactions...</p>
                            ) : financialError ? (
                                <p>Unable to load transactions.</p>
                            ) : financialData?.transactions?.content?.length > 0 ? (
                                financialData.transactions.content.map(transaction => (
                                    <div className="dash-row" key={transaction.id}>
            <span>
                {transaction.description || 'No description'}
            </span>

                                        <span className={
                                            transaction.type === 'INCOME'
                                                ? 'positive'
                                                : 'negative'
                                        }>
                {transaction.type === 'INCOME' ? '+' : '-'}
                                            {formatCurrency(transaction.amount)}
            </span>

                                        <span>
                {transaction.type}
            </span>

                                        <span>
                {new Intl.DateTimeFormat('en-ZA', {
                    day: 'numeric',
                    month: 'short'
                }).format(
                    new Date(`${transaction.transactionDate}T00:00:00`)
                )}
            </span>
                                    </div>
                                ))
                            ) : (
                                <p>No recent transactions found.</p>
                            )}

                        </div>

                    </div>

                    {financialLoading ? (
                        <p>Loading expense breakdown...</p>
                    ) : financialError ? (
                        <p>Unable to load expense breakdown.</p>
                    ) : Object.entries(financialData?.expenseBreakdown || {}).length > 0 ? (
                        Object.entries(financialData.expenseBreakdown).map(
                            ([category, rawAmount]) => {

                                const amount = Number(rawAmount) || 0;
                                const totalExpenses = Number(financialData.expenses) || 0;

                                // Calculate this category's share of total expenses.
                                const percentage = totalExpenses > 0
                                    ? (amount / totalExpenses) * 100
                                    : 0;

                                return (
                                    <div className="expense-bar-item" key={category}>

                                        <div className="expense-bar-info">
                                            <span>{category}</span>

                                            <span>
                            {formatCurrency(amount)} (
                                                {percentage.toFixed(2)}%)
                        </span>
                                        </div>

                                        <div className="expense-bar-track">
                                            <div
                                                className="expense-bar-fill"
                                                style={{
                                                    width: `${Math.min(100, Math.max(0, percentage))}%`
                                                }}
                                            />
                                        </div>

                                    </div>
                                );
                            }
                        )
                    ) : (
                        <p>No expense data available.</p>
                    )}

                </div>
            </div>
        </div>
    );
};

export default Dashboard;
