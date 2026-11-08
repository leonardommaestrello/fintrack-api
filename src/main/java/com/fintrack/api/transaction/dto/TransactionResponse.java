package com.fintrack.api.transaction.dto;

import com.fintrack.api.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        String description,
        BigDecimal amount,
        TransactionType type,
        LocalDate transactionDate,
        Long categoryId,
        String categoryName,
        String categoryColor
) {}
