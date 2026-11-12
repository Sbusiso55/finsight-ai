package com.finsight.controller;

import com.finsight.dto.BusinessDTO;
import com.finsight.model.Business;
import com.finsight.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<BusinessDTO>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(
                businessService.getBusinessesByOwner(ownerId).stream()
                        .map(BusinessDTO::from)
                        .toList());
    }

    @GetMapping("/owner")
    public ResponseEntity<List<Business>> getMyBusinesses(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<Business> businesses =
                businessService.getBusinessesByOwnerEmail(
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(businesses);
    }

    @GetMapping("/accessible/{userId}")
    public ResponseEntity<List<BusinessDTO>> getAccessible(@PathVariable Long userId) {
        return ResponseEntity.ok(
                businessService.getAccessibleBusinesses(userId).stream()
                        .map(BusinessDTO::from)
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessDTO> getById(@PathVariable Long id) {
        return businessService.getBusinessById(id)
                .map(BusinessDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<BusinessDTO> create(
            @RequestBody Business business,
            @AuthenticationPrincipal UserDetails userDetails) {

        Business savedBusiness = businessService.saveBusiness(business, userDetails.getUsername());
        return ResponseEntity.ok(BusinessDTO.from(savedBusiness));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessDTO> update(@PathVariable Long id, @RequestBody Business business) {
        return businessService.getBusinessById(id)
                .map(existing -> {
                    business.setId(id);
                    Business saved = businessService.saveBusiness(business, business.getOwner().getEmail());
                    return ResponseEntity.ok(BusinessDTO.from(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }
}

