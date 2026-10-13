package com.finsight.repository;

import com.finsight.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Database access for businesses. */
public interface BusinessRepository extends JpaRepository<Business, Long> {

    List<Business> findByOwnerId(Long ownerId);

    /** Every business the user owns or belongs to as a member. Used for the business switcher. */
    @Query("""
            select distinct b from Business b
            where b.owner.id = :userId
               or b.id in (select m.business.id from BusinessMember m where m.user.id = :userId)
            order by b.name
            """)
    List<Business> findAccessibleByUser(@Param("userId") Long userId);
}
