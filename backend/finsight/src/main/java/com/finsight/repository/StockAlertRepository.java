package com.finsight.repository;

import com.finsight.model.StockAlert;
import com.finsight.model.enums.StockAlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Database access for low stock alerts. */
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    List<StockAlert> findByProductIdAndStatus(Long productId, StockAlertStatus status);

    boolean existsByProductIdAndStatus(Long productId, StockAlertStatus status);

    @Query("""
            select a from StockAlert a
            where a.status = :status
              and a.productId in (select p.id from Product p where p.businessId = :businessId)
            order by a.createdAt desc
            """)
    List<StockAlert> findByBusinessIdAndStatus(@Param("businessId") Long businessId,
                                               @Param("status") StockAlertStatus status);
}
