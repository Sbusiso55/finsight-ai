package com.finsight.controller;

import com.finsight.dto.CheckoutRequest;
import com.finsight.dto.SubscriptionDTO;
import com.finsight.model.User;
import com.finsight.repository.UserRepository;
import com.finsight.service.BillingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;
    private final UserRepository userRepository;

    public BillingController(BillingService billingService, UserRepository userRepository) {
        this.billingService = billingService;
        this.userRepository = userRepository;
    }

    private User currentUser(UserDetails principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> checkout(@RequestBody CheckoutRequest request,
                                                        @AuthenticationPrincipal UserDetails principal) {
        String url = billingService.startCheckout(currentUser(principal), request.months());
        return ResponseEntity.ok(Map.of("authorizationUrl", url));
    }

    @GetMapping("/confirm")
    public ResponseEntity<SubscriptionDTO> confirm(@RequestParam String reference,
                                                   @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(SubscriptionDTO.from(billingService.confirm(currentUser(principal), reference)));
    }
}