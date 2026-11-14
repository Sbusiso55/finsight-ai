
import { useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import api from '../api/axiosConfig';
import './Reports.css';


// Get the current month as YYYY-MM.
const getCurrentPeriodKey = () => {
    const today = new Date();

    return `${today.getFullYear()}-${String(
        today.getMonth() + 1
    ).padStart(2, '0')}`;
};


// Generate the current month and previous three months.
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


// Convert selected month into API date parameters.
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


const formatMonthLabel = (periodKey) => {
    const [year, month] = periodKey.split('-').map(Number);

    return new Date(year, month - 1, 1).toLocaleDateString(
        'en-ZA',
        {
            month: 'long',
            year: 'numeric'
        }
    );
};


const Reports = () => {

    // Business information
    const [businesses, setBusinesses] = useState([]);
    const [selectedBusinessId, setSelectedBusinessId] = useState(null);
    const [businessesLoading, setBusinessesLoading] = useState(true);
    const [businessesError, setBusinessesError] = useState(false);

    // Reporting period
    const [selectedPeriod, setSelectedPeriod] = useState(
        getCurrentPeriodKey
    );

    // Monthly summary
    const [summary, setSummary] = useState(null);
    const [summaryLoading, setSummaryLoading] = useState(false);
    const [summaryError, setSummaryError] = useState(false);

    // Forecasts
    const [forecasts, setForecasts] = useState(null);
    const [forecastLoading, setForecastLoading] = useState(false);
    const [forecastError, setForecastError] = useState(false);

    const currentBusiness = businesses.find(
        business => business.id === selectedBusinessId
    );


    // Fetch businesses.
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


    // Fetch monthly summary whenever business or period changes.
    useEffect(() => {

        if (!selectedBusinessId || !currentBusiness) return;

        let ignore = false;

        const fetchMonthlySummary = async () => {

            setSummaryLoading(true);
            setSummaryError(false);
            setSummary(null);

            const { from, to } = getReportingDateRange(selectedPeriod);

            try {
                const response = await api.get('/api/monthly-summary', {
                    params: {
                        businessId: selectedBusinessId,
                        from,
                        to,
                        openingBalance: currentBusiness.openingBalance ?? 0
                    }
                });

                if (!ignore) {
                    setSummary(response.data);

                    console.log('Monthly report:', {
                        businessId: selectedBusinessId,
                        businessName: currentBusiness.name,
                        reportingPeriod: { from, to },
                        summary: response.data
                    });
                }

            } catch (error) {
                if (!ignore) {
                    console.error('Failed to fetch monthly summary:', error);
                    setSummaryError(true);
                }

            } finally {
                if (!ignore) {
                    setSummaryLoading(false);
                }
            }
        };

        void fetchMonthlySummary();

        return () => {
            ignore = true;
        };

    }, [
        selectedBusinessId,
        selectedPeriod,
        currentBusiness?.openingBalance
    ]);


    // Fetch forecasts whenever the selected business changes.
    useEffect(() => {

        if (!selectedBusinessId) return;

        let ignore = false;

        const fetchForecasts = async () => {

            setForecastLoading(true);
            setForecastError(false);
            setForecasts(null);

            try {
                const [
                    revenueResponse,
                    expensesResponse,
                    profitResponse
                ] = await Promise.all([

                    api.get('/api/forecasting/revenue', {
                        params: {
                            businessId: selectedBusinessId,
                            months: 3
                        }
                    }),

                    api.get('/api/forecasting/expenses', {
                        params: {
                            businessId: selectedBusinessId,
                            months: 3
                        }
                    }),

                    api.get('/api/forecasting/profit', {
                        params: {
                            businessId: selectedBusinessId,
                            months: 3
                        }
                    })
                ]);

                if (!ignore) {
                    setForecasts({
                        revenue: revenueResponse.data,
                        expenses: expensesResponse.data,
                        profit: profitResponse.data
                    });

                    console.log('Financial forecasts:', {
                        businessId: selectedBusinessId,
                        revenue: revenueResponse.data,
                        expenses: expensesResponse.data,
                        profit: profitResponse.data
                    });
                }

            } catch (error) {
                if (!ignore) {
                    console.error('Failed to fetch forecasts:', error);
                    setForecastError(true);
                }

            } finally {
                if (!ignore) {
                    setForecastLoading(false);
                }
            }
        };

        void fetchForecasts();

        return () => {
            ignore = true;
        };

    }, [selectedBusinessId]);


    // Format financial amounts using the business currency.
    const formatCurrency = (amount) =>
        new Intl.NumberFormat('en-ZA', {
            style: 'currency',
            currency: currentBusiness?.currency || 'ZAR'
        }).format(Number(amount || 0));


    // Derive insights from the actual summary.
    const expenseEntries = summary
        ? Object.entries(summary.expenseBreakdown || {})
        : [];

    const largestExpense = [...expenseEntries].sort(
        (a, b) => Number(b[1]) - Number(a[1])
    )[0];

    const totalExpenses = Number(summary?.totalExpenses || 0);

    const largestExpensePercentage = largestExpense && totalExpenses > 0
        ? (Number(largestExpense[1]) / totalExpenses) * 100
        : 0;

    const fixedExpenses = Number(
        summary?.fixedVsVariable?.fixed || 0
    );

    const fixedExpensePercentage = totalExpenses > 0
        ? (fixedExpenses / totalExpenses) * 100
        : 0;


    // Forecast period: current month.
    const forecastPeriod = formatMonthLabel(getCurrentPeriodKey());


    return (
        <div className="reports-wrapper">

            <Navbar />

            <div className="reports-content">

                <div className="reports-header">

                    <h1 className="reports-title">
                        Financial Reports
                    </h1>

                    <p className="reports-subtitle">
                        Monthly summaries and performance insights.
                    </p>

                </div>


                {/* Business and reporting-period selection */}
                <div className="reports-controls">

                    <div className="report-control">
                        <label htmlFor="report-business">
                            Business
                        </label>

                        {businessesLoading ? (
                            <p>Loading businesses...</p>
                        ) : businessesError ? (
                            <p>Unable to load businesses.</p>
                        ) : businesses.length === 0 ? (
                            <p>No businesses available.</p>
                        ) : (
                            <select
                                id="report-business"
                                value={selectedBusinessId ?? ''}
                                onChange={(e) =>
                                    setSelectedBusinessId(Number(e.target.value))
                                }
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


                    <div className="report-control">
                        <label htmlFor="report-period">
                            Reporting Period
                        </label>

                        <select
                            id="report-period"
                            value={selectedPeriod}
                            onChange={(e) => setSelectedPeriod(e.target.value)}
                        >
                            {getReportingPeriodOptions().map(period => (
                                <option
                                    key={period.value}
                                    value={period.value}
                                >
                                    {period.label}
                                </option>
                            ))}
                        </select>
                    </div>

                </div>


                <div className="reports-grid">


                    {/* Monthly financial summary */}
                    <div className="report-card">

                        <div className="report-card-header">

                            <span className="report-month">
                                {formatMonthLabel(selectedPeriod)}
                            </span>

                            <span className="report-badge">
                                Selected Period
                            </span>

                        </div>


                        {summaryLoading || businessesLoading ? (

                            <p className="report-message">
                                Loading financial summary...
                            </p>

                        ) : summaryError ? (

                            <p className="report-error">
                                Unable to load financial summary.
                            </p>

                        ) : summary ? (

                            <>

                                <div className="report-stats">

                                    <div className="report-stat">
                                        <p className="report-stat-label">
                                            Revenue
                                        </p>

                                        <p className="report-stat-value">
                                            {formatCurrency(summary.totalRevenue)}
                                        </p>
                                    </div>


                                    <div className="report-stat">
                                        <p className="report-stat-label">
                                            Expenses
                                        </p>

                                        <p className="report-stat-value">
                                            {formatCurrency(summary.totalExpenses)}
                                        </p>
                                    </div>


                                    <div className="report-stat">
                                        <p className="report-stat-label">
                                            Net Profit
                                        </p>

                                        <p className="report-stat-value positive">
                                            {formatCurrency(summary.netProfit)}
                                        </p>
                                    </div>


                                    <div className="report-stat">
                                        <p className="report-stat-label">
                                            Net Margin
                                        </p>

                                        <p className="report-stat-value">
                                            {Number(summary.netMargin || 0).toFixed(2)}%
                                        </p>
                                    </div>

                                </div>


                                <div className="report-secondary-stats">

                                    <div>
                                        <span>Gross Profit Margin</span>
                                        <strong>
                                            {Number(summary.grossMargin || 0).toFixed(2)}%
                                        </strong>
                                    </div>

                                    <div>
                                        <span>Opening Balance</span>
                                        <strong>
                                            {formatCurrency(summary.openingBalance)}
                                        </strong>
                                    </div>

                                    <div>
                                        <span>Closing Balance</span>
                                        <strong>
                                            {formatCurrency(summary.closingBalance)}
                                        </strong>
                                    </div>

                                </div>


                                <div className="report-insights">

                                    <h4>Key Insights</h4>

                                    <ul>

                                        {largestExpense && (
                                            <li>
                                                {largestExpense[0]} is the largest expense,
                                                accounting for {largestExpensePercentage.toFixed(2)}%
                                                of total expenses.
                                            </li>
                                        )}

                                        {totalExpenses > 0 && (
                                            <li>
                                                Fixed costs account for
                                                {' '}{fixedExpensePercentage.toFixed(2)}%
                                                of total expenses.
                                            </li>
                                        )}

                                        <li>
                                            Expense-to-revenue ratio:
                                            {' '}{Number(summary.expenseToRevenueRatio || 0).toFixed(2)}%.
                                        </li>

                                        <li>
                                            Gross profit margin:
                                            {' '}{Number(summary.grossMargin || 0).toFixed(2)}%.
                                        </li>

                                    </ul>

                                </div>


                                {expenseEntries.length > 0 && (

                                    <div className="report-expenses">

                                        <h4>Expense Breakdown</h4>

                                        {expenseEntries.map(([category, amount]) => {

                                            const percentage = totalExpenses > 0
                                                ? (Number(amount) / totalExpenses) * 100
                                                : 0;

                                            return (
                                                <div
                                                    className="report-expense-row"
                                                    key={category}
                                                >

                                                    <span>{category}</span>

                                                    <div className="report-expense-value">
                                                        <strong>
                                                            {formatCurrency(amount)}
                                                        </strong>

                                                        <span>
                                                            {percentage.toFixed(2)}%
                                                        </span>
                                                    </div>

                                                </div>
                                            );
                                        })}

                                    </div>
                                )}

                            </>

                        ) : (

                            <p className="report-message">
                                No summary data available.
                            </p>

                        )}

                    </div>


                    {/* Financial forecasts */}
                    <div className="report-card">

                        <div className="report-card-header">

                            <span className="report-month">
                                Forecast — {forecastPeriod}
                            </span>

                        </div>


                        {forecastLoading || businessesLoading ? (

                            <p className="report-message">
                                Loading forecasts...
                            </p>

                        ) : forecastError ? (

                            <p className="report-error">
                                Unable to load financial forecasts.
                            </p>

                        ) : forecasts ? (

                            <>

                                <div className="report-stats">

                                    <div className="report-stat">
                                        <p className="report-stat-label">
                                            Projected Revenue
                                        </p>

                                        <p className="report-stat-value">
                                            {formatCurrency(forecasts.revenue.forecast)}
                                        </p>

                                        <p className="forecast-range">
                                            {formatCurrency(forecasts.revenue.low)}
                                            {' '}–{' '}
                                            {formatCurrency(forecasts.revenue.high)}
                                        </p>
                                    </div>


                                    <div className="report-stat">
                                        <p className="report-stat-label">
                                            Projected Expenses
                                        </p>

                                        <p className="report-stat-value">
                                            {formatCurrency(forecasts.expenses.forecast)}
                                        </p>

                                        <p className="forecast-range">
                                            {formatCurrency(forecasts.expenses.low)}
                                            {' '}–{' '}
                                            {formatCurrency(forecasts.expenses.high)}
                                        </p>
                                    </div>


                                    <div className="report-stat">
                                        <p className="report-stat-label">
                                            Projected Profit
                                        </p>

                                        <p className="report-stat-value positive">
                                            {formatCurrency(forecasts.profit.forecast)}
                                        </p>

                                        <p className="forecast-range">
                                            {formatCurrency(forecasts.profit.low)}
                                            {' '}–{' '}
                                            {formatCurrency(forecasts.profit.high)}
                                        </p>
                                    </div>

                                </div>


                                <p className="forecast-note">
                                    Forecasts are based on the previous three
                                    completed months. The displayed ranges
                                    represent the estimated low and high values.
                                </p>

                            </>

                        ) : (

                            <p className="report-message">
                                No forecast data available.
                            </p>

                        )}

                    </div>


                </div>

            </div>

        </div>
    );
};

export default Reports;
