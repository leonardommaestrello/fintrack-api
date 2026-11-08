package com.fintrack.api.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal balance,
        long transactionCount
) {}
