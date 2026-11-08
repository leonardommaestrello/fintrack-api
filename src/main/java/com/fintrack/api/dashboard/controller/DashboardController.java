
package com.fintrack.api.dashboard.controller;

import com.fintrack.api.dashboard.dto.DashboardSummaryResponse;
import com.fintrack.api.dashboard.service.DashboardService;
import com.fintrack.api.dashboard.service.impl.DashboardServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fintrack.api.dashboard.dto.MonthlySummaryResponse;
import com.fintrack.api.dashboard.dto.ExpenseByCategoryResponse;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(
                dashboardService.getSummary(userId)
        );
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlySummaryResponse>> getMonthlySummary(
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(
                dashboardService.getMonthlySummary(userId)
        );
    }


    @GetMapping("/expenses-by-category")
    public ResponseEntity<List<ExpenseByCategoryResponse>> getExpensesByCategory(
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(
                dashboardService.getExpensesByCategory(userId)
        );
    }



}
