package com.finsight.repository;

import com.finsight.model.Forecast;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Database access for forecasts. */
public interface ForecastRepository extends JpaRepository<Forecast, Long> {

    List<Forecast> findByBusinessIdOrderByForecastMonthDesc(Long businessId);

    /** The latest forecast generated for a given month. */
    Optional<Forecast> findFirstByBusinessIdAndForecastMonthOrderByCreatedAtDesc(
            Long businessId, LocalDate forecastMonth);
}
