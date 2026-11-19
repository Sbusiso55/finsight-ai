package com.finsight.repository;

import com.finsight.model.PosTransaction;
import com.finsight.model.enums.PosTransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Database access for card machine payments.
 * Totals only count SUCCESS payments and return 0 (never null) when there are none.
 */
public interface PosTransactionRepository extends JpaRepository<PosTransaction, Long> {

    boolean existsByProviderTransactionId(String providerTransactionId);

    Optional<PosTransaction> findByProviderTransactionId(String providerTransactionId);

    Page<PosTransaction> findByBusinessIdOrderByCreatedAtDesc(Long businessId, Pageable pageable);

    long countByBusinessIdAndStatus(Long businessId, PosTransactionStatus status);

    @Query("""
            select coalesce(sum(t.amount), 0) from PosTransaction t
            where t.businessId = :businessId
              and t.status = com.finsight.model.enums.PosTransactionStatus.SUCCESS
            """)
    BigDecimal totalPaid(@Param("businessId") Long businessId);

    @Query("""
            select coalesce(sum(t.amount), 0) from PosTransaction t
            where t.businessId = :businessId
              and t.status = com.finsight.model.enums.PosTransactionStatus.SUCCESS
              and t.createdAt between :from and :to
            """)
    BigDecimal totalPaidBetween(@Param("businessId") Long businessId,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to);
}
