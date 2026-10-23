package com.finsight.dto;

import com.finsight.model.Transaction;
import com.finsight.model.enums.TransactionSource;
import com.finsight.model.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class TransactionDTO {

    private Long id;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private TransactionSource source;
    private CategoryDTO category;
    private CustomerDTO customer;


    public static TransactionDTO from(Transaction transaction) {
        CustomerDTO customer = null;
        if (transaction.getCustomer() != null) {
            customer = CustomerDTO.from(transaction.getCustomer());
        }
        return new TransactionDTO(
                transaction.getId(),
                transaction.getTransactionDate(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getDescription(),
                transaction.getSource(),
                CategoryDTO.from(transaction.getCategory()),
                customer);
    }
}
