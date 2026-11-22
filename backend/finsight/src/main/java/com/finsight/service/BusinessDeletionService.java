package com.finsight.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessDeletionService {

    private static final Logger log = LoggerFactory.getLogger(BusinessDeletionService.class);

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void deleteBusinessAndData(Long businessId) {
        int moves = em.createQuery("DELETE FROM StockMovement m WHERE m.productId IN "
                        + "(SELECT p.id FROM Product p WHERE p.businessId = :id)")
                .setParameter("id", businessId)
                .executeUpdate();
        int alerts = em.createQuery("DELETE FROM StockAlert a WHERE a.productId IN "
                        + "(SELECT p.id FROM Product p WHERE p.businessId = :id)")
                .setParameter("id", businessId)
                .executeUpdate();
        int products = deleteByBusinessId("Product", businessId);
        int suppliers = deleteByBusinessId("Supplier", businessId);
        int pos = deleteByBusinessId("PosTransaction", businessId);
        int accounts = deleteByBusinessId("PaymentProviderAccount", businessId);

        int tx = deleteByBusinessRelation("Transaction", businessId);
        int cust = deleteByBusinessRelation("Customer", businessId);
        int cat = deleteByBusinessRelation("Category", businessId);

        em.createQuery("DELETE FROM Business b WHERE b.id = :id")
                .setParameter("id", businessId)
                .executeUpdate();

        log.info("Deleted business {} (movements={}, alerts={}, products={}, suppliers={}, posPayments={}, "
                        + "providerAccounts={}, transactions={}, customers={}, categories={})",
                businessId, moves, alerts, products, suppliers, pos, accounts, tx, cust, cat);
    }

    
    private int deleteByBusinessId(String entity, Long businessId) {
        return em.createQuery("DELETE FROM " + entity + " e WHERE e.businessId = :id")
                .setParameter("id", businessId)
                .executeUpdate();
    }

    
    private int deleteByBusinessRelation(String entity, Long businessId) {
        return em.createQuery("DELETE FROM " + entity + " e WHERE e.business.id = :id")
                .setParameter("id", businessId)
                .executeUpdate();
    }
}