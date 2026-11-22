package com.finsight.security;

import com.finsight.model.User;
import com.finsight.repository.UserRepository;
import com.finsight.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Call requirePro(email) at the start of any Pro-only endpoint. */
@Component
public class ProAccess {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;
    private final boolean enabled;

    public ProAccess(SubscriptionService subscriptionService,
                     UserRepository userRepository,
                     @Value("${clearside.pro-locks.enabled:false}") boolean enabled) {
        this.subscriptionService = subscriptionService;
        this.userRepository = userRepository;
        this.enabled = enabled;
    }

    public void requirePro(String email) {
        if (!enabled) return;
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!subscriptionService.isPro(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pro plan required");
        }
    }
}