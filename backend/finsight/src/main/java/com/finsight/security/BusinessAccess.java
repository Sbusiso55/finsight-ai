package com.finsight.security;

import com.finsight.model.Business;
import com.finsight.service.BusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class BusinessAccess {

    private final BusinessService businessService;

    public BusinessAccess(BusinessService businessService) {
        this.businessService = businessService;
    }

    public Business requireOwned(Long businessId, String email) {
        Business business = businessService.getBusinessById(businessId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found"));
        if (!business.getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found");
        }
        return business;
    }
}