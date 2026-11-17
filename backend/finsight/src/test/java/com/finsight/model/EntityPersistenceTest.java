package com.finsight.model;

import com.finsight.model.enums.*;
import com.finsight.testsupport.JpaTest;
import com.finsight.testsupport.TestData;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Checks that the entity mappings really work against a database: constraints, defaults, enums, audit fields. */
@JpaTest
class EntityPersistenceTest {

    @Autowired TestEntityManager em;
    @Autowired JdbcTemplate jdbc;

    TestData data;
    User owner;
    Business business;

    @BeforeEach
    void setUp() {
        data = new TestData(em);
        owner = data.user("owner@example.com");
        business = data.business(owner, "Anita's Shop");
    }

    private void assertRejected(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfAny(PersistenceException.class, HibernateException.class, DataAccessException.class);
    }

    @Test
    void savingSetsIdAndAuditTimestamps() {
        assertThat(owner.getId()).isNotNull();
        assertThat(owner.getCreatedAt()).isNotNull();
    }

    @Test
    void updateChangesUpdatedAtButNotCreatedAt() throws Exception {
        var created = owner.getCreatedAt();
        Thread.sleep(5);
        owner.setFullName("Changed Name");
        em.flush();
        assertThat(owner.getCreatedAt()).isEqualTo(created);
        assertThat(owner.getUpdatedAt()).isAfterOrEqualTo(created);
    }

    @Test
    void businessDefaultsAreStored() {
        em.clear();
        Business loaded = em.find(Business.class, business.getId());
        assertThat(loaded.getCurrency()).isEqualTo("ZAR");
        assertThat(loaded.getOpeningBalance()).isEqualByComparingTo("0");
        assertThat(loaded.isSeasonal()).isFalse();
    }

    @Test
    void moneyKeepsTwoDecimalPlaces() {
        Category sales = data.income(business, "Sales");
        Transaction t = data.transaction(business, sales, LocalDate.of(2026, 1, 5), "1234.56", TransactionType.INCOME);
        em.clear();
        assertThat(em.find(Transaction.class, t.getId()).getAmount()).isEqualByComparingTo("1234.56");
    }

    @Test
    void enumsAreStoredAsText() {
        Category rent = data.expense(business, "Rent", CostBehavior.FIXED, CostClassification.OPERATING_EXPENSE);
        String behavior = jdbc.queryForObject("select cost_behavior from categories where id = ?", String.class, rent.getId());
        String type = jdbc.queryForObject("select type from categories where id = ?", String.class, rent.getId());
        assertThat(behavior).isEqualTo("FIXED");
        assertThat(type).isEqualTo("EXPENSE");
    }

    @Test
    void incomeCategoryMayHaveNoCostBehavior() {
        Category sales = data.income(business, "Sales");
        em.clear();
        Category loaded = em.find(Category.class, sales.getId());
        assertThat(loaded.getCostBehavior()).isNull();
        assertThat(loaded.getCostClassification()).isNull();
    }

    @Test
    void transactionSourceDefaultsToManual() {
        Category sales = data.income(business, "Sales");
        Transaction t = data.transaction(business, sales, LocalDate.of(2026, 1, 5), "10.00", TransactionType.INCOME);
        em.clear();
        assertThat(em.find(Transaction.class, t.getId()).getSource()).isEqualTo(TransactionSource.MANUAL);
    }

    @Test
    void transactionCustomerIsOptional() {
        Category sales = data.income(business, "Sales");
        Transaction t = data.transaction(business, sales, LocalDate.of(2026, 1, 5), "10.00", TransactionType.INCOME);
        em.clear();
        assertThat(em.find(Transaction.class, t.getId()).getCustomer()).isNull();
    }

    @Test
    void monthlySummaryComputedGettersAreNotColumns() {
        MonthlySummary s = data.summary(business, LocalDate.of(2026, 1, 1));
        Integer columns = jdbc.queryForObject(
                "select count(*) from information_schema.columns "
                        + "where lower(table_name) = 'monthly_summaries' "
                        + "and lower(column_name) in ('gross_profit','net_profit','net_cash_flow')",
                Integer.class);
        assertThat(columns).isZero();
        assertThat(s.getNetProfit()).isEqualByComparingTo("0");
    }

    // ---- constraints ----

    @Test
    void duplicateEmailIsRejected() {
        assertRejected(() -> data.user("owner@example.com"));
    }

    @Test
    void userWithoutEmailIsRejected() {
        User u = new User();
        u.setPasswordHash("x");
        u.setFullName("No Email");
        assertRejected(() -> em.persistAndFlush(u));
    }

    @Test
    void businessWithoutOwnerIsRejected() {
        Business b = new Business();
        b.setName("Orphan");
        assertRejected(() -> em.persistAndFlush(b));
    }

    @Test
    void duplicateCategoryNameInSameBusinessIsRejected() {
        data.income(business, "Sales");
        assertRejected(() -> data.income(business, "Sales"));
    }

    @Test
    void sameCategoryNameInDifferentBusinessIsAllowed() {
        Business other = data.business(owner, "Second Shop");
        data.income(business, "Sales");
        Category again = data.income(other, "Sales");
        assertThat(again.getId()).isNotNull();
    }

    @Test
    void sameUserCannotJoinSameBusinessTwice() {
        User accountant = data.user("acc@example.com");
        data.member(business, accountant, Role.ACCOUNTANT);
        assertRejected(() -> data.member(business, accountant, Role.EMPLOYEE));
    }

    @Test
    void onlyOneSummaryPerBusinessPerMonth() {
        data.summary(business, LocalDate.of(2026, 1, 1));
        assertRejected(() -> data.summary(business, LocalDate.of(2026, 1, 1)));
    }

    @Test
    void onlyOneAlertRulePerTypePerBusiness() {
        data.rule(business, InsightType.LARGEST_EXPENSE, true);
        assertRejected(() -> data.rule(business, InsightType.LARGEST_EXPENSE, false));
    }

    @Test
    void transactionWithoutAmountIsRejected() {
        Category sales = data.income(business, "Sales");
        Transaction t = new Transaction();
        t.setBusiness(business);
        t.setCategory(sales);
        t.setTransactionDate(LocalDate.of(2026, 1, 5));
        t.setType(TransactionType.INCOME);
        assertRejected(() -> em.persistAndFlush(t));
    }

    @Test
    void transactionAmountOverFifteenDigitsIsRejected() {
        Category sales = data.income(business, "Sales");
        assertRejected(() -> data.transaction(business, sales, LocalDate.of(2026, 1, 5),
                new BigDecimal("1").scaleByPowerOfTen(14).toPlainString() + ".00", TransactionType.INCOME));
    }

    @Test
    void currencyLongerThanThreeCharactersIsRejected() {
        Business b = new Business();
        b.setName("Bad currency");
        b.setOwner(owner);
        b.setCurrency("RAND");
        assertRejected(() -> em.persistAndFlush(b));
    }
}
