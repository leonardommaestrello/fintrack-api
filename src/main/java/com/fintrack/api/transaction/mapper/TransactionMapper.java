package com.fintrack.api.transaction.mapper;

import com.fintrack.api.transaction.dto.TransactionResponse;
import com.fintrack.api.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTransactionDate(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getCategory().getColor()
        );
    }
}
