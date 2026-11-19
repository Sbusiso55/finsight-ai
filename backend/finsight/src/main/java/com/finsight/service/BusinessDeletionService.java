package com.finsight.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Permanently deletes a business and everything linked to it.
 * Children go first so no foreign key is broken. One transaction: all or nothing.
 * Callers MUST check ownership first (BusinessAccess.requireOwned).
 */
@Service
public class BusinessDeletionService {

    private static final Logger log = LoggerFactory.getLogger(BusinessDeletionService.class);

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void deleteBusinessAndData(Long businessId) {
        // Transactions point to categories, so they must go first.
        int tx = delete("Transaction", businessId);
        int cust = delete("Customer", businessId);
        int cat = delete("Category", businessId);

        em.createQuery("DELETE FROM Business b WHERE b.id = :id")
          .setParameter("id", businessId)
          .executeUpdate();

        log.info("Deleted business {} (transactions={}, customers={}, categories={})",
                businessId, tx, cust, cat);
    }

    private int delete(String entity, Long businessId) {
        return em.createQuery("DELETE FROM " + entity + " e WHERE e.business.id = :id")
                 .setParameter("id", businessId)
                 .executeUpdate();
    }
}