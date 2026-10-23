package com.finsight.dto;

import com.finsight.model.Category;
import com.finsight.model.enums.CostBehavior;
import com.finsight.model.enums.CostClassification;
import com.finsight.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private TransactionType type;
    private CostBehavior costBehavior;
    private CostClassification costClassification;

    public static CategoryDTO from(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getCostBehavior(),
                category.getCostClassification());
    }
}
