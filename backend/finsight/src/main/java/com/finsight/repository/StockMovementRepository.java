package com.finsight.repository;

import com.finsight.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Database access for stock scans (stock in / stock out). */
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByScannedAtDesc(Long productId);

    @Query("""
            select m from StockMovement m
            where m.productId in (select p.id from Product p where p.businessId = :businessId)
            order by m.scannedAt desc
            """)
    Page<StockMovement> findByBusinessId(@Param("businessId") Long businessId, Pageable pageable);

    @Query("""
            select coalesce(sum(case when m.type = com.finsight.model.enums.StockMovementType.IN
                                     then m.quantity else -m.quantity end), 0L)
            from StockMovement m where m.productId = :productId
            """)
    Long netQuantityByProductId(@Param("productId") Long productId);
}
