package com.finsight.dto;

import com.finsight.model.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SupplierDTO {
    private Long id;
    private Long businessId;
    private String name;
    private String phone;
    private String email;

    public static SupplierDTO from(Supplier s) {
        return new SupplierDTO(s.getId(), s.getBusinessId(), s.getName(), s.getPhone(), s.getEmail());
    }
}
