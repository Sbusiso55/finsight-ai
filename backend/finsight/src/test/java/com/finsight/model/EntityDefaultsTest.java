package com.finsight.model;

import com.finsight.model.enums.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Plain unit tests (no database): default values on new entity objects. */
class EntityDefaultsTest {

    @Test
    void newUserIsEnabled() {
        assertThat(new User().isEnabled()).isTrue();
    }

    @Test
    void newBusinessDefaultsToRandZeroBalanceNotSeasonal() {
        Business b = new Business();
        assertThat(b.getCurrency()).isEqualTo("ZAR");
        assertThat(b.getOpeningBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(b.isSeasonal()).isFalse();
    }

    @Test
    void newTransactionDefaultsToManualSource() {
        assertThat(new Transaction().getSource()).isEqualTo(TransactionSource.MANUAL);
    }

    @Test
    void newForecastDefaultsToAverageMonthlyChange() {
        assertThat(new Forecast().getMethod()).isEqualTo(ForecastMethod.AVERAGE_MONTHLY_CHANGE);
    }

    @Test
    void newInsightIsNotDismissed() {
        assertThat(new ExpenseInsight().isDismissed()).isFalse();
    }

    @Test
    void newAlertRuleIsEnabled() {
        assertThat(new AlertRule().isEnabled()).isTrue();
    }

    @Test
    void newMonthlySummaryStartsAtZero() {
        MonthlySummary s = new MonthlySummary();
        assertThat(s.getTotalRevenue()).isEqualByComparingTo("0");
        assertThat(s.getTotalExpenses()).isEqualByComparingTo("0");
        assertThat(s.getCashInflows()).isEqualByComparingTo("0");
        assertThat(s.getRevenueTransactionCount()).isZero();
    }
}
