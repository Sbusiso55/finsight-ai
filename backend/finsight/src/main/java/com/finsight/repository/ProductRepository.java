package com.finsight.repository;

import com.finsight.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Database access for stock items. */
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByBusinessIdOrderByName(Long businessId);

    Optional<Product> findByBusinessIdAndBarcode(Long businessId, String barcode);

    Optional<Product> findByBusinessIdAndQrCode(Long businessId, String qrCode);

    boolean existsByBusinessIdAndBarcode(Long businessId, String barcode);

    boolean existsByBusinessIdAndQrCode(Long businessId, String qrCode);

    Optional<Product> findByIdAndBusinessId(Long id, Long businessId);

    List<Product> findBySupplierId(Long supplierId);

    @Query("""
            select p from Product p
            where p.businessId = :businessId and p.quantity <= p.minStockLevel
            order by p.quantity asc, p.name asc
            """)
    List<Product> findLowStock(@Param("businessId") Long businessId);
}
