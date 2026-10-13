package com.finsight.repository;

import com.finsight.model.Category;
import com.finsight.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Database access for transaction categories. */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByBusinessIdOrderByName(Long businessId);

    List<Category> findByBusinessIdAndType(Long businessId, TransactionType type);

    /** Used by CSV/bank-statement import to match a row to an existing category. */
    Optional<Category> findByBusinessIdAndNameIgnoreCase(Long businessId, String name);

    boolean existsByBusinessIdAndNameIgnoreCase(Long businessId, String name);
}
