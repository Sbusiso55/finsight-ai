package com.finsight.service;

import com.finsight.model.Payment;
import com.finsight.model.Subscription;
import com.finsight.model.User;
import com.finsight.model.enums.PaymentStatus;
import com.finsight.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class BillingService {

    public static final Map<Integer, Long> PRICES_CENTS = Map.of(
            1, 12900L,
            3, 34900L,
            6, 65900L,
            12, 119900L
    );

    private final PaymentRepository paymentRepository;
    private final PaystackService paystackService;
    private final SubscriptionService subscriptionService;
    private final String frontendUrl;

    public BillingService(PaymentRepository paymentRepository,
                          PaystackService paystackService,
                          SubscriptionService subscriptionService,
                          @Value("${CLEARSIDE_FRONTEND_URL:http://localhost:3000}") String frontendUrl) {
        this.paymentRepository = paymentRepository;
        this.paystackService = paystackService;
        this.subscriptionService = subscriptionService;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public String startCheckout(User user, Integer months) {
        Long amount = months == null ? null : PRICES_CENTS.get(months);
        if (amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose 1, 3, 6 or 12 months");
        }
        String reference = "CSP-" + UUID.randomUUID().toString().replace("-", "");

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setReference(reference);
        payment.setMonths(months);
        payment.setAmountCents(amount);
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        return paystackService.initialize(user.getEmail(), amount, reference,
                frontendUrl + "/billing/callback", months, user.getId());
    }

    @Transactional
    public Subscription confirm(User user, String reference) {
        Payment payment = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return subscriptionService.getCurrent(user);
        }

        Map<String, Object> data = paystackService.verify(reference);
        boolean success = "success".equals(data.get("status"));
        Object paidAmount = data.get("amount");
        boolean amountOk = paidAmount instanceof Number n && n.longValue() == payment.getAmountCents();
        boolean currencyOk = payment.getCurrency().equalsIgnoreCase(String.valueOf(data.get("currency")));

        if (!success || !amountOk || !currencyOk) {
            if (!"ongoing".equals(data.get("status")) && !"pending".equals(data.get("status"))) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Payment was not completed");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
        return subscriptionService.activatePro(user, payment.getMonths());
    }
}