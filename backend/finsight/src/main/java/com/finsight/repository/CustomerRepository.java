package com.finsight.repository;

import com.finsight.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Database access for customers. */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByBusinessIdOrderByName(Long businessId);

    Optional<Customer> findByBusinessIdAndNameIgnoreCase(Long businessId, String name);
}
