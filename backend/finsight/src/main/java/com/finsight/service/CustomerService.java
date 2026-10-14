package com.finsight.service;

import com.finsight.model.Customer;
import com.finsight.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public Customer addCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public List<Customer> getAllCustomersByBusiness(Long businessId) {
        return customerRepository.findByBusinessId(businessId);
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        return customerRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedCustomer.getName());
                    existing.setEmail(updatedCustomer.getEmail());
                    existing.setPhone(updatedCustomer.getPhone());
                    return customerRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
