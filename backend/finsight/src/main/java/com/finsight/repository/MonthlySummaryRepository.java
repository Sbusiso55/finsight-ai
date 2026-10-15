package com.finsight.repository;

import com.finsight.model.MonthlySummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Database access for the pre-calculated monthly totals. */
public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, Long> {

    Optional<MonthlySummary> findByBusinessIdAndPeriodStart(Long businessId, LocalDate periodStart);

    /** Full history, oldest first. Used for trends and forecasting. */
    List<MonthlySummary> findByBusinessIdOrderByPeriodStartAsc(Long businessId);

    List<MonthlySummary> findByBusinessIdAndPeriodStartBetweenOrderByPeriodStartAsc(
            Long businessId, LocalDate from, LocalDate to);

    /**
     * The most recent months up to and including a date, newest first.
     * Pass PageRequest.of(0, 3) to get the last three months for a basic trend forecast.
     */
    List<MonthlySummary> findByBusinessIdAndPeriodStartLessThanEqualOrderByPeriodStartDesc(
            Long businessId, LocalDate upTo, Pageable pageable);

    /** How many months of history exist (3 is the minimum for a forecast). */
    long countByBusinessId(Long businessId);
}
