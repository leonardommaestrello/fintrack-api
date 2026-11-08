package com.fintrack.api.category.dto;

import com.fintrack.api.transaction.enums.TransactionType;

public record CategoryResponse(
        Long id,
        String name,
        TransactionType type,
        String color
) {}
