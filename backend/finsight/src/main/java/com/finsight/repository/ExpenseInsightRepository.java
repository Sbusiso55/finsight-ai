package com.finsight.repository;

import com.finsight.model.ExpenseInsight;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Database access for generated expense advice. */
public interface ExpenseInsightRepository extends JpaRepository<ExpenseInsight, Long> {

    @EntityGraph(attributePaths = "category")
    List<ExpenseInsight> findByBusinessIdAndPeriodStart(Long businessId, LocalDate periodStart);

    /** Insights the user has not dismissed, newest month first. */
    @EntityGraph(attributePaths = "category")
    List<ExpenseInsight> findByBusinessIdAndDismissedFalseOrderByPeriodStartDesc(Long businessId);

    /** Clears a month's insights so they can be regenerated after transactions change. */
    @Transactional
    @Modifying
    @Query("delete from ExpenseInsight i where i.business.id = :businessId and i.periodStart = :periodStart")
    void deleteByBusinessAndPeriod(@Param("businessId") Long businessId,
                                   @Param("periodStart") LocalDate periodStart);
}
