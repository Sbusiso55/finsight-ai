package com.finsight.repository;

import com.finsight.model.BusinessMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Database access for who belongs to which business, and with what role. */
public interface BusinessMemberRepository extends JpaRepository<BusinessMember, Long> {

    @EntityGraph(attributePaths = "business")
    List<BusinessMember> findByUserId(Long userId);

    @EntityGraph(attributePaths = "user")
    List<BusinessMember> findByBusinessId(Long businessId);

    /** Used for permission checks: does this user have a role in this business? */
    Optional<BusinessMember> findByBusinessIdAndUserId(Long businessId, Long userId);

    boolean existsByBusinessIdAndUserId(Long businessId, Long userId);
}
