package com.finsight.repository;

import com.finsight.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Database access for suppliers. */
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByBusinessIdOrderByName(Long businessId);

    Optional<Supplier> findByIdAndBusinessId(Long id, Long businessId);
}
