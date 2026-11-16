package com.finsight.testsupport;

import com.finsight.model.*;
import com.finsight.model.enums.*;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Builds and saves valid entities so each test only spells out the fields it cares about. */
public class TestData {

    private final TestEntityManager em;

    public TestData(TestEntityManager em) {
        this.em = em;
    }

    public User user(String email) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash("hash");
        u.setFullName("Test User");
        return em.persistAndFlush(u);
    }

    public Business business(User owner, String name) {
        Business b = new Business();
        b.setName(name);
        b.setOwner(owner);
        return em.persistAndFlush(b);
    }

    public BusinessMember member(Business business, User user, Role role) {
        BusinessMember m = new BusinessMember();
        m.setBusiness(business);
        m.setUser(user);
        m.setRole(role);
        return em.persistAndFlush(m);
    }

    public Category income(Business b, String name) {
        return category(b, name, TransactionType.INCOME, null, null);
    }

    public Category expense(Business b, String name, CostBehavior behavior, CostClassification classification) {
        return category(b, name, TransactionType.EXPENSE, behavior, classification);
    }

    public Category category(Business b, String name, TransactionType type,
                             CostBehavior behavior, CostClassification classification) {
        Category c = new Category();
        c.setBusiness(b);
        c.setName(name);
        c.setType(type);
        c.setCostBehavior(behavior);
        c.setCostClassification(classification);
        return em.persistAndFlush(c);
    }

    public Customer customer(Business b, String name) {
        Customer c = new Customer();
        c.setBusiness(b);
        c.setName(name);
        return em.persistAndFlush(c);
    }

    public Transaction transaction(Business b, Category c, LocalDate date, String amount, TransactionType type) {
        return transaction(b, c, null, date, amount, type, null);
    }

    public Transaction transaction(Business b, Category c, Customer customer, LocalDate date,
                                   String amount, TransactionType type, String description) {
        Transaction t = new Transaction();
        t.setBusiness(b);
        t.setCategory(c);
        t.setCustomer(customer);
        t.setTransactionDate(date);
        t.setAmount(new BigDecimal(amount));
        t.setType(type);
        t.setDescription(description);
        return em.persistAndFlush(t);
    }

    public MonthlySummary summary(Business b, LocalDate periodStart) {
        MonthlySummary s = new MonthlySummary();
        s.setBusiness(b);
        s.setPeriodStart(periodStart);
        return em.persistAndFlush(s);
    }

    public Forecast forecast(Business b, LocalDate month, String revenue) {
        Forecast f = new Forecast();
        f.setBusiness(b);
        f.setForecastMonth(month);
        f.setProjectedRevenue(new BigDecimal(revenue));
        f.setProjectedExpenses(new BigDecimal("1000.00"));
        f.setProjectedProfit(new BigDecimal(revenue).subtract(new BigDecimal("1000.00")));
        f.setMonthsOfHistory(3);
        f.setConfidence(ForecastConfidence.MEDIUM);
        return em.persistAndFlush(f);
    }

    public ExpenseInsight insight(Business b, LocalDate periodStart, InsightType type,
                                  Category category, boolean dismissed) {
        ExpenseInsight i = new ExpenseInsight();
        i.setBusiness(b);
        i.setPeriodStart(periodStart);
        i.setType(type);
        i.setSeverity(InsightSeverity.WARNING);
        i.setCategory(category);
        i.setMessage("message");
        i.setDismissed(dismissed);
        return em.persistAndFlush(i);
    }

    public AlertRule rule(Business b, InsightType type, boolean enabled) {
        AlertRule r = new AlertRule();
        r.setBusiness(b);
        r.setRuleType(type);
        r.setThresholdPercent(new BigDecimal("40.00"));
        r.setEnabled(enabled);
        return em.persistAndFlush(r);
    }
}
