package com.fintrack.api.dashboard.service;

import com.fintrack.api.dashboard.dto.DashboardSummaryResponse;
import com.fintrack.api.dashboard.dto.MonthlySummaryResponse;
import com.fintrack.api.dashboard.dto.ExpenseByCategoryResponse;

import java.util.List;

public interface DashboardService {

    DashboardSummaryResponse getSummary(Long userId);

    List<MonthlySummaryResponse> getMonthlySummary(Long userId);

    List<ExpenseByCategoryResponse> getExpensesByCategory(Long userId);
}
