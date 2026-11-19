package com.finsight.dto;

import com.finsight.model.StockMovement;
import com.finsight.model.enums.StockMovementType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StockMovementDTO {
    private Long id;
    private Long productId;
    private Integer quantity;
    private StockMovementType type;
    private Long scannedBy;
    private LocalDateTime scannedAt;

    public static StockMovementDTO from(StockMovement m) {
        return new StockMovementDTO(m.getId(), m.getProductId(), m.getQuantity(), m.getType(),
                m.getScannedBy(), m.getScannedAt());
    }
}
