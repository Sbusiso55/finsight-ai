package com.finsight.repository;

import com.finsight.model.PaymentProviderAccount;
import com.finsight.model.enums.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Database access for connected payment provider accounts. */
public interface PaymentProviderAccountRepository extends JpaRepository<PaymentProviderAccount, Long> {

    List<PaymentProviderAccount> findByBusinessId(Long businessId);

    Optional<PaymentProviderAccount> findByBusinessIdAndProvider(Long businessId, PaymentProvider provider);

    boolean existsByBusinessIdAndProvider(Long businessId, PaymentProvider provider);
}
