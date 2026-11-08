package com.fintrack.api.category.dto;

import com.fintrack.api.transaction.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 80, message = "Category name must not exceed 80 characters")
        String name,

        @NotNull(message = "Category type is required")
        TransactionType type,

        @NotBlank(message = "Category color is required")
        @Pattern(
                regexp = "^#[0-9A-Fa-f]{6}$",
                message = "Color must be a valid hexadecimal value"
        )
        String color

) {}
