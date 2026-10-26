package com.finsight.controller;

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
    public ResponseEntity<List<Business>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(businessService.getBusinessesByOwner(ownerId));
    }

    @GetMapping("/accessible/{userId}")
    public ResponseEntity<List<Business>> getAccessible(@PathVariable Long userId) {
        return ResponseEntity.ok(businessService.getAccessibleBusinesses(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Business> getById(@PathVariable Long id) {
        return businessService.getBusinessById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

//    @PostMapping
//    public ResponseEntity<Business> create(@RequestBody Business business) {
//        return ResponseEntity.ok(businessService.saveBusiness(business));
//    }

    @PostMapping
    public ResponseEntity<Business> create(
            @RequestBody Business business,
            @AuthenticationPrincipal UserDetails userDetails) {

        Business savedBusiness = businessService.saveBusiness(
                business,
                userDetails.getUsername()
        );

        return ResponseEntity.ok(savedBusiness);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Business> update(@PathVariable Long id, @RequestBody Business business) {
        return businessService.getBusinessById(id)
                .map(existing -> {
                    business.setId(id);
                    return ResponseEntity.ok(businessService.saveBusiness(business, business.getOwner().getEmail()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }
}

