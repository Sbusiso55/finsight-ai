package com.finsight.controller;

import com.finsight.model.Business;
import com.finsight.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @GetMapping
    public ResponseEntity<List<Business>> getAllBusinesses() {
        return ResponseEntity.ok(businessService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Business> getBusinessById(@PathVariable Long id) {
        return businessService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Business> createBusiness(@RequestBody Business business) {
        return ResponseEntity.ok(businessService.save(business));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Business> updateBusiness(@PathVariable Long id, @RequestBody Business business) {
        return businessService.findById(id)
                .map(existing -> {
                    business.setId(existing.getId());
                    return ResponseEntity.ok(businessService.save(business));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusiness(@PathVariable Long id) {
        businessService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
