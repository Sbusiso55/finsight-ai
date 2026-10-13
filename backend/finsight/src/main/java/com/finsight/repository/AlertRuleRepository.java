package com.finsight.repository;

import com.finsight.model.AlertRule;
import com.finsight.model.enums.InsightType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Database access for per-business advice thresholds. */
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {

    List<AlertRule> findByBusinessId(Long businessId);

    List<AlertRule> findByBusinessIdAndEnabledTrue(Long businessId);

    Optional<AlertRule> findByBusinessIdAndRuleType(Long businessId, InsightType ruleType);
}
