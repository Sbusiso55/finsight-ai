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
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JpaTest
class ForecastRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired ForecastRepository forecasts;

    TestData data;
    Business shop, other;

    @BeforeEach
    void setUp() {
        data = new TestData(em);
        User u = data.user("u@example.com");
        shop = data.business(u, "Shop");
        other = data.business(u, "Other");
    }

    @Test
    void listIsNewestMonthFirstAndScoped() {
        data.forecast(shop, LocalDate.of(2026, 4, 1), "100.00");
        data.forecast(shop, LocalDate.of(2026, 6, 1), "100.00");
        data.forecast(shop, LocalDate.of(2026, 5, 1), "100.00");
        data.forecast(other, LocalDate.of(2026, 7, 1), "100.00");
        assertThat(forecasts.findByBusinessIdOrderByForecastMonthDesc(shop.getId()))
                .extracting(Forecast::getForecastMonth)
                .containsExactly(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 1));
    }

    @Test
    void latestForecastForAMonthIsTheMostRecentlyCreated() throws Exception {
        LocalDate april = LocalDate.of(2026, 4, 1);
        data.forecast(shop, april, "100.00");
        Thread.sleep(10);                                  // make createdAt differ
        Forecast newer = data.forecast(shop, april, "200.00");
        assertThat(forecasts.findFirstByBusinessIdAndForecastMonthOrderByCreatedAtDesc(shop.getId(), april))
                .get().extracting(Forecast::getId).isEqualTo(newer.getId());
    }

    @Test
    void latestForecastIsEmptyWhenNoneExistForThatMonth() {
        assertThat(forecasts.findFirstByBusinessIdAndForecastMonthOrderByCreatedAtDesc(
                shop.getId(), LocalDate.of(2026, 4, 1))).isEmpty();
    }
}
