package com.finsight.controller;

import com.finsight.dto.CustomerDTO;
import com.finsight.model.Business;
import com.finsight.model.Customer;
import com.finsight.security.BusinessAccess;
import com.finsight.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final BusinessAccess businessAccess;

    public CustomerController(CustomerService customerService, BusinessAccess businessAccess) {
        this.customerService = customerService;
        this.businessAccess = businessAccess;
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<CustomerDTO>> getByBusiness(@PathVariable Long businessId,
                                                           @AuthenticationPrincipal UserDetails user) {
        businessAccess.requireOwned(businessId, user.getUsername());
        return ResponseEntity.ok(
                customerService.getCustomersByBusiness(businessId).stream()
                        .map(CustomerDTO::from)
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getById(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(CustomerDTO.from(findOwned(id, user)));
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> create(@RequestBody Customer customer,
                                              @AuthenticationPrincipal UserDetails user) {
        if (customer.getBusiness() == null || customer.getBusiness().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business is required");
        }
        Business business = businessAccess.requireOwned(customer.getBusiness().getId(), user.getUsername());
        customer.setId(null);
        customer.setBusiness(business);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomerDTO.from(customerService.saveCustomer(customer)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> update(@PathVariable Long id,
                                              @RequestBody Customer customer,
                                              @AuthenticationPrincipal UserDetails user) {
        Customer existing = findOwned(id, user);
        customer.setId(id);
        // Keep the customer on its original business, so it can't be moved to someone else's
        customer.setBusiness(existing.getBusiness());
        return ResponseEntity.ok(CustomerDTO.from(customerService.saveCustomer(customer)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails user) {
        findOwned(id, user);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    private Customer findOwned(Long id, UserDetails user) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        businessAccess.requireOwned(customer.getBusiness().getId(), user.getUsername());
        return customer;
    }
}