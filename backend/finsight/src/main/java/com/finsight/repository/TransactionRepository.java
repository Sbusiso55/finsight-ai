package com.finsight.repository;

import com.finsight.model.Transaction;
import com.finsight.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Database access and aggregate queries for transactions.
 * All date ranges are inclusive. Sums return 0 (never null) when there are no rows.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** One category's total, e.g. ("Sales revenue", 25500). */
    interface CategoryTotal {
        Long getCategoryId();
        String getCategoryName();
        BigDecimal getTotal();
    }

    // ---- Listing ----

    /** Paged list for the Transactions page. Loads category and customer to avoid lazy-loading errors. */
    @EntityGraph(attributePaths = {"category", "customer"})
    Page<Transaction> findByBusinessId(Long businessId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "customer"})
    Page<Transaction> findByBusinessIdAndTransactionDateBetween(
            Long businessId, LocalDate from, LocalDate to, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "customer"})
    List<Transaction> findByBusinessIdAndTransactionDateBetweenOrderByTransactionDateAsc(
            Long businessId, LocalDate from, LocalDate to);

    /** Security check: only fetch a transaction if it belongs to the given business. */
    java.util.Optional<Transaction> findByIdAndBusinessId(Long id, Long businessId);

    /** Duplicate detection when importing the same CSV or bank statement twice. */
    boolean existsByBusinessIdAndTransactionDateAndAmountAndDescription(
            Long businessId, LocalDate date, BigDecimal amount, String description);

    long countByBusinessIdAndTypeAndTransactionDateBetween(
            Long businessId, TransactionType type, LocalDate from, LocalDate to);

    // ---- Totals (revenue, expenses, cash inflows and outflows) ----

    @Query("""
            select coalesce(sum(t.amount), 0) from Transaction t
            where t.business.id = :businessId and t.type = :type
              and t.transactionDate between :from and :to
            """)
    BigDecimal sumByType(@Param("businessId") Long businessId,
                         @Param("type") TransactionType type,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to);

    /** Revenue or spend per category, largest first. Drives "largest expense" and revenue by category. */
    @Query("""
            select c.id as categoryId, c.name as categoryName, sum(t.amount) as total
            from Transaction t join t.category c
            where t.business.id = :businessId and t.type = :type
              and t.transactionDate between :from and :to
            group by c.id, c.name
            order by sum(t.amount) desc
            """)
    List<CategoryTotal> totalsByCategory(@Param("businessId") Long businessId,
                                         @Param("type") TransactionType type,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to);

    /** Total for one category in a period, e.g. advertising this month vs last month. */
    @Query("""
            select coalesce(sum(t.amount), 0) from Transaction t
            where t.business.id = :businessId and t.category.id = :categoryId
              and t.transactionDate between :from and :to
            """)
    BigDecimal sumByCategory(@Param("businessId") Long businessId,
                             @Param("categoryId") Long categoryId,
                             @Param("from") LocalDate from,
                             @Param("to") LocalDate to);

    // ---- Fixed/variable and COGS/operating splits (expense categories only) ----

    @Query("""
            select coalesce(sum(t.amount), 0) from Transaction t
            where t.business.id = :businessId and t.type = com.finsight.model.enums.TransactionType.EXPENSE
              and t.category.costBehavior = :behavior
              and t.transactionDate between :from and :to
            """)
    BigDecimal sumByCostBehavior(@Param("businessId") Long businessId,
                                 @Param("behavior") com.finsight.model.enums.CostBehavior behavior,
                                 @Param("from") LocalDate from,
                                 @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(t.amount), 0) from Transaction t
            where t.business.id = :businessId and t.type = com.finsight.model.enums.TransactionType.EXPENSE
              and t.category.costClassification = :classification
              and t.transactionDate between :from and :to
            """)
    BigDecimal sumByCostClassification(@Param("businessId") Long businessId,
                                       @Param("classification") com.finsight.model.enums.CostClassification classification,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to);
}
