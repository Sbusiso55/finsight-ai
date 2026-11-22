package com.finsight.dto;

import com.finsight.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private Long businessId;
    private String name;
    private String barcode;
    private String qrCode;
    private Integer quantity;
    private Integer minStockLevel;
    private BigDecimal unitPrice;
    private Long supplierId;

    public static ProductDTO from(Product p) {
        return new ProductDTO(p.getId(), p.getBusinessId(), p.getName(), p.getBarcode(), p.getQrCode(),
                p.getQuantity(), p.getMinStockLevel(), p.getUnitPrice(), p.getSupplierId());
    }
}
