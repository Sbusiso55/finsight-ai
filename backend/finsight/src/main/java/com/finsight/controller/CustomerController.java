
package com.finsight.controller;

import com.finsight.dto.CustomerDTO;
import com.finsight.model.Customer;
import com.finsight.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<CustomerDTO>> getByBusiness(@PathVariable Long businessId) {
        return ResponseEntity.ok(
                customerService.getCustomersByBusiness(businessId).stream()
                        .map(CustomerDTO::from)
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getById(@PathVariable Long id) {
        return customerService.getCustomerById(id)
                .map(CustomerDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> create(@RequestBody Customer customer) {
        return ResponseEntity.ok(CustomerDTO.from(customerService.saveCustomer(customer)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> update(@PathVariable Long id, @RequestBody Customer customer) {
        return customerService.getCustomerById(id)
                .map(existing -> {
                    customer.setId(id);
                    return ResponseEntity.ok(CustomerDTO.from(customerService.saveCustomer(customer)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}

