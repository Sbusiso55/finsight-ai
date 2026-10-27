package com.finsight.dto;

import com.finsight.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerDTO {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;

    public static CustomerDTO from(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhoneNumber());
    }
}
