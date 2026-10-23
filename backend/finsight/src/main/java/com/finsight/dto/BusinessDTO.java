package com.finsight.dto;

import com.finsight.model.Business;
import com.finsight.model.enums.Role;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessDTO {

    private Long id;
    private String name;
    private String industry;
    private String currency;
    private BigDecimal openingBalance;
    private LocalDate openingBalanceDate;
    private boolean seasonal;
    private Role role;

    public static BusinessDTO from(Business business) {
        return from(business, null);
    }

    public static BusinessDTO from(Business business, Role role) {
        return new BusinessDTO(
                business.getId(),
                business.getName(),
                business.getIndustry(),
                business.getCurrency(),
                business.getOpeningBalance(),
                business.getOpeningBalanceDate(),
                business.isSeasonal(),
                role);
    }
}
