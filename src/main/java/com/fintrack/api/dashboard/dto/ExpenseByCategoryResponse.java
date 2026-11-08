package com.fintrack.api.dashboard.dto;

import java.math.BigDecimal;

public record ExpenseByCategoryResponse(
        Long categoryId,
        String categoryName,
        String categoryColor,
        BigDecimal totalAmount,
        long transactionCount
) {
}
