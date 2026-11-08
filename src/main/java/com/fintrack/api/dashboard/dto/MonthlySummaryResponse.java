package com.fintrack.api.dashboard.dto;

import java.math.BigDecimal;

public record MonthlySummaryResponse(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal balance
) {}
