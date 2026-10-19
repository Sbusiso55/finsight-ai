package com.finsight.controller;

import com.finsight.model.Business;
import com.finsight.service.BusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping
    public ResponseEntity<List<Business>> getAllBusinesses() {
        return ResponseEntity.ok(businessService.getAllBusinesses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Business> getBusinessById(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.getBusinessById(id));
    }

    @PostMapping
    public ResponseEntity<Business> createBusiness(@RequestBody Business business) {
        return ResponseEntity.ok(businessService.saveBusiness(business));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Business> updateBusiness(
            @PathVariable Long id,
            @RequestBody Business business) {
        business.setId(id);
        return ResponseEntity.ok(businessService.saveBusiness(business));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusiness(@PathVariable Long id) {
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }
}
