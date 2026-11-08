package com.fintrack.api.transaction.dto;

import com.fintrack.api.transaction.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(

        @NotBlank(message = "Description is required")
        @Size(max = 200, message = "Description must not exceed 200 characters")
        String description,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        @Digits(
                integer = 13,
                fraction = 2,
                message = "Amount must have at most 13 integer digits and 2 decimal places"
        )
        BigDecimal amount,

        @NotNull(message = "Transaction type is required")
        TransactionType type,

        @NotNull(message = "Category is required")
        Long categoryId,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate

) {}
