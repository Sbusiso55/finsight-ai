package com.finsight.repository;

import com.finsight.model.*;
import com.finsight.model.enums.*;
import com.finsight.testsupport.JpaTest;
import com.finsight.testsupport.TestData;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static com.finsight.model.enums.TransactionType.EXPENSE;
import static com.finsight.model.enums.TransactionType.INCOME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the custom queries against hand-calculated numbers.
 *
 * January 2026, business "Shop":
 *   INCOME : Sales 10000 (5 Jan) + 5000 (20 Jan), Consulting 2500 (31 Jan)           = 17500
 *   EXPENSE: Salaries 9000 (FIXED, opex), Rent 3000 (FIXED, opex),
 *            Advertising 1500 + 500 (VARIABLE, opex), Stock 6000 (VARIABLE, COGS)   = 20000
 *   Outside the month (must never be counted): Sales 999 on 1 Feb, expense 777 on 31 Dec.
 * A second business has income of 12345 in January (must never leak into Shop's numbers).
 */
@JpaTest
class TransactionRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired TransactionRepository transactions;

    TestData data;
    Business shop, other;
    Category sales, consulting, salaries, rent, advertising, stock;
    Customer anita;

    static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    static final LocalDate TO = LocalDate.of(2026, 1, 31);

    static LocalDate jan(int day) { return LocalDate.of(2026, 1, day); }

    @BeforeEach
    void setUp() {
        data = new TestData(em);
        User u = data.user("u@example.com");
        shop = data.business(u, "Shop");
        other = data.business(u, "Other");

        sales = data.income(shop, "Sales");
        consulting = data.income(shop, "Consulting");
        salaries = data.expense(shop, "Salaries", CostBehavior.FIXED, CostClassification.OPERATING_EXPENSE);
        rent = data.expense(shop, "Rent", CostBehavior.FIXED, CostClassification.OPERATING_EXPENSE);
        advertising = data.expense(shop, "Advertising", CostBehavior.VARIABLE, CostClassification.OPERATING_EXPENSE);
        stock = data.expense(shop, "Stock", CostBehavior.VARIABLE, CostClassification.COST_OF_GOODS_SOLD);
        anita = data.customer(shop, "Anita's Retail");

        data.transaction(shop, sales, anita, jan(5), "10000.00", INCOME, "January sales");
        data.transaction(shop, sales, jan(20), "5000.00", INCOME);
        data.transaction(shop, consulting, jan(31), "2500.00", INCOME);          // last day: must be included
        data.transaction(shop, sales, LocalDate.of(2026, 2, 1), "999.00", INCOME); // outside

        data.transaction(shop, salaries, jan(1), "9000.00", EXPENSE);            // first day: must be included
        data.transaction(shop, rent, jan(1), "3000.00", EXPENSE);
        data.transaction(shop, advertising, jan(10), "1500.00", EXPENSE);
        data.transaction(shop, advertising, jan(25), "500.00", EXPENSE);
        data.transaction(shop, stock, jan(15), "6000.00", EXPENSE);
        data.transaction(shop, rent, LocalDate.of(2025, 12, 31), "777.00", EXPENSE); // outside

        Category otherSales = data.income(other, "Sales");
        data.transaction(other, otherSales, jan(5), "12345.00", INCOME);
        em.clear();
    }

    // ---------------------------------------------------------------- sumByType

    @Test
    void sumByTypeAddsUpRevenueForTheMonth() {
        assertThat(transactions.sumByType(shop.getId(), INCOME, FROM, TO)).isEqualByComparingTo("17500.00");
    }

    @Test
    void sumByTypeAddsUpExpensesForTheMonth() {
        assertThat(transactions.sumByType(shop.getId(), EXPENSE, FROM, TO)).isEqualByComparingTo("20000.00");
    }

    @Test
    void sumByTypeIncludesTheFirstAndLastDay() {
        assertThat(transactions.sumByType(shop.getId(), INCOME, jan(31), jan(31))).isEqualByComparingTo("2500.00");
        assertThat(transactions.sumByType(shop.getId(), EXPENSE, jan(1), jan(1))).isEqualByComparingTo("12000.00");
    }

    @Test
    void sumByTypeDoesNotMixInOtherBusinesses() {
        assertThat(transactions.sumByType(other.getId(), INCOME, FROM, TO)).isEqualByComparingTo("12345.00");
        assertThat(transactions.sumByType(other.getId(), EXPENSE, FROM, TO)).isEqualByComparingTo("0");
    }

    @Test
    void sumByTypeReturnsZeroNotNullWhenThereAreNoRows() {
        assertThat(transactions.sumByType(shop.getId(), INCOME, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 31)))
                .isNotNull()
                .isEqualByComparingTo("0");
    }

    // ----------------------------------------------------------- totalsByCategory

    @Test
    void totalsByCategoryAreGroupedAndLargestFirst() {
        List<TransactionRepository.CategoryTotal> result =
                transactions.totalsByCategory(shop.getId(), EXPENSE, FROM, TO);

        assertThat(result).extracting(TransactionRepository.CategoryTotal::getCategoryName)
                .containsExactly("Salaries", "Stock", "Rent", "Advertising");
        assertThat(result).extracting(TransactionRepository.CategoryTotal::getTotal)
                .usingElementComparator(java.math.BigDecimal::compareTo)
                .containsExactly(new java.math.BigDecimal("9000"), new java.math.BigDecimal("6000"),
                        new java.math.BigDecimal("3000"), new java.math.BigDecimal("2000"));
    }

    @Test
    void totalsByCategoryCarriesTheCategoryId() {
        TransactionRepository.CategoryTotal top =
                transactions.totalsByCategory(shop.getId(), EXPENSE, FROM, TO).get(0);
        assertThat(top.getCategoryId()).isEqualTo(salaries.getId());
    }

    @Test
    void totalsByCategoryForIncomeListsRevenueSources() {
        assertThat(transactions.totalsByCategory(shop.getId(), INCOME, FROM, TO))
                .extracting(TransactionRepository.CategoryTotal::getCategoryName)
                .containsExactly("Sales", "Consulting");
    }

    @Test
    void totalsByCategoryAddUpToTheTypeTotal() {
        java.math.BigDecimal sum = transactions.totalsByCategory(shop.getId(), EXPENSE, FROM, TO).stream()
                .map(TransactionRepository.CategoryTotal::getTotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        assertThat(sum).isEqualByComparingTo(transactions.sumByType(shop.getId(), EXPENSE, FROM, TO));
    }

    @Test
    void totalsByCategoryIsEmptyWhenNothingMatches() {
        assertThat(transactions.totalsByCategory(shop.getId(), EXPENSE,
                LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 31))).isEmpty();
    }

    // ------------------------------------------------------------- sumByCategory

    @Test
    void sumByCategoryAddsAllRowsOfThatCategoryInThePeriod() {
        assertThat(transactions.sumByCategory(shop.getId(), advertising.getId(), FROM, TO))
                .isEqualByComparingTo("2000.00");
    }

    @Test
    void sumByCategoryRespectsTheDateRange() {
        assertThat(transactions.sumByCategory(shop.getId(), advertising.getId(), jan(1), jan(15)))
                .isEqualByComparingTo("1500.00");
    }

    @Test
    void sumByCategoryIsZeroWhenCategoryHasNoRows() {
        assertThat(transactions.sumByCategory(shop.getId(), advertising.getId(),
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31))).isEqualByComparingTo("0");
    }

    // --------------------------------------------------------- sumByCostBehavior

    @Test
    void fixedCostsAreSalariesPlusRent() {
        assertThat(transactions.sumByCostBehavior(shop.getId(), CostBehavior.FIXED, FROM, TO))
                .isEqualByComparingTo("12000.00");
    }

    @Test
    void variableCostsAreAdvertisingPlusStock() {
        assertThat(transactions.sumByCostBehavior(shop.getId(), CostBehavior.VARIABLE, FROM, TO))
                .isEqualByComparingTo("8000.00");
    }

    @Test
    void fixedPlusVariableEqualsTotalExpenses() {
        var fixed = transactions.sumByCostBehavior(shop.getId(), CostBehavior.FIXED, FROM, TO);
        var variable = transactions.sumByCostBehavior(shop.getId(), CostBehavior.VARIABLE, FROM, TO);
        assertThat(fixed.add(variable)).isEqualByComparingTo(transactions.sumByType(shop.getId(), EXPENSE, FROM, TO));
    }

    @Test
    void costBehaviorIgnoresIncomeRowsEvenIfTheCategoryWasTaggedFixed() {
        Category mistagged = data.category(shop, "Mistagged income", INCOME, CostBehavior.FIXED, null);
        data.transaction(shop, mistagged, jan(12), "100.00", INCOME);
        assertThat(transactions.sumByCostBehavior(shop.getId(), CostBehavior.FIXED, FROM, TO))
                .isEqualByComparingTo("12000.00");
    }

    @Test
    void costBehaviorIsZeroForABusinessWithNoExpenses() {
        assertThat(transactions.sumByCostBehavior(other.getId(), CostBehavior.FIXED, FROM, TO))
                .isEqualByComparingTo("0");
    }

    // ----------------------------------------------------- sumByCostClassification

    @Test
    void costOfGoodsSoldIsJustStock() {
        assertThat(transactions.sumByCostClassification(shop.getId(), CostClassification.COST_OF_GOODS_SOLD, FROM, TO))
                .isEqualByComparingTo("6000.00");
    }

    @Test
    void operatingExpensesAreEverythingElse() {
        assertThat(transactions.sumByCostClassification(shop.getId(), CostClassification.OPERATING_EXPENSE, FROM, TO))
                .isEqualByComparingTo("14000.00");
    }

    @Test
    void cogsPlusOperatingEqualsTotalExpenses() {
        var cogs = transactions.sumByCostClassification(shop.getId(), CostClassification.COST_OF_GOODS_SOLD, FROM, TO);
        var opex = transactions.sumByCostClassification(shop.getId(), CostClassification.OPERATING_EXPENSE, FROM, TO);
        assertThat(cogs.add(opex)).isEqualByComparingTo(transactions.sumByType(shop.getId(), EXPENSE, FROM, TO));
    }

    // ------------------------------------------------------------------ listing

    @Test
    void pagedListIsScopedToTheBusinessAndLoadsCategoryAndCustomer() {
        Page<Transaction> page = transactions.findByBusinessId(shop.getId(), PageRequest.of(0, 50));
        assertThat(page.getTotalElements()).isEqualTo(10);
        assertThat(page.getContent()).allSatisfy(t -> {
            assertThat(Hibernate.isInitialized(t.getCategory())).isTrue();
            assertThat(Hibernate.isInitialized(t.getCustomer())).isTrue();
        });
    }

    @Test
    void pagingSplitsTheResults() {
        Page<Transaction> first = transactions.findByBusinessId(shop.getId(), PageRequest.of(0, 4));
        Page<Transaction> last = transactions.findByBusinessId(shop.getId(), PageRequest.of(2, 4));
        assertThat(first.getContent()).hasSize(4);
        assertThat(first.getTotalPages()).isEqualTo(3);
        assertThat(last.getContent()).hasSize(2);
    }

    @Test
    void pagedListByDateRangeIsInclusive() {
        Page<Transaction> page = transactions.findByBusinessIdAndTransactionDateBetween(
                shop.getId(), jan(1), jan(31), PageRequest.of(0, 50));
        assertThat(page.getTotalElements()).isEqualTo(8);   // 3 income + 5 expense in January
    }

    @Test
    void rangeListIsOldestFirstAndLoadsRelations() {
        List<Transaction> list = transactions
                .findByBusinessIdAndTransactionDateBetweenOrderByTransactionDateAsc(shop.getId(), FROM, TO);
        assertThat(list).hasSize(8);
        assertThat(list).extracting(Transaction::getTransactionDate).isSorted();
        assertThat(list).allSatisfy(t -> assertThat(Hibernate.isInitialized(t.getCategory())).isTrue());
    }

    // --------------------------------------------------------------- lookups

    @Test
    void findByIdAndBusinessIdFindsOwnTransaction() {
        Transaction any = transactions.findByBusinessId(shop.getId(), PageRequest.of(0, 1)).getContent().get(0);
        assertThat(transactions.findByIdAndBusinessId(any.getId(), shop.getId())).isPresent();
    }

    @Test
    void findByIdAndBusinessIdHidesOtherBusinessesTransactions() {
        Transaction any = transactions.findByBusinessId(shop.getId(), PageRequest.of(0, 1)).getContent().get(0);
        assertThat(transactions.findByIdAndBusinessId(any.getId(), other.getId())).isEmpty();
    }

    @Test
    void duplicateDetectionMatchesDateAmountAndDescription() {
        assertThat(transactions.existsByBusinessIdAndTransactionDateAndAmountAndDescription(
                shop.getId(), jan(5), new java.math.BigDecimal("10000.00"), "January sales")).isTrue();
        assertThat(transactions.existsByBusinessIdAndTransactionDateAndAmountAndDescription(
                shop.getId(), jan(5), new java.math.BigDecimal("10000.00"), "Something else")).isFalse();
        assertThat(transactions.existsByBusinessIdAndTransactionDateAndAmountAndDescription(
                other.getId(), jan(5), new java.math.BigDecimal("10000.00"), "January sales")).isFalse();
    }

    @Test
    void countByTypeAndDateRange() {
        assertThat(transactions.countByBusinessIdAndTypeAndTransactionDateBetween(shop.getId(), INCOME, FROM, TO))
                .isEqualTo(3);
        assertThat(transactions.countByBusinessIdAndTypeAndTransactionDateBetween(shop.getId(), EXPENSE, FROM, TO))
                .isEqualTo(5);
    }
}
