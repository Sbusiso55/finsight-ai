package com.finsight.dto;

import com.finsight.model.StockAlert;
import com.finsight.model.enums.StockAlertStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StockAlertDTO {
    private Long id;
    private Long productId;
    private Integer currentQuantity;
    private Integer minStockLevel;
    private StockAlertStatus status;
    private LocalDateTime createdAt;

    public static StockAlertDTO from(StockAlert a) {
        return new StockAlertDTO(a.getId(), a.getProductId(), a.getCurrentQuantity(),
                a.getMinStockLevel(), a.getStatus(), a.getCreatedAt());
    }
}
