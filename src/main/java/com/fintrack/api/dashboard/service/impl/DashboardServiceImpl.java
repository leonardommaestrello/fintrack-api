
package com.fintrack.api.dashboard.service.impl;

import com.fintrack.api.dashboard.dto.DashboardSummaryResponse;
import com.fintrack.api.dashboard.dto.ExpenseByCategoryResponse;
import com.fintrack.api.dashboard.dto.MonthlySummaryResponse;
import com.fintrack.api.dashboard.service.DashboardService;
import com.fintrack.api.transaction.entity.Transaction;
import com.fintrack.api.transaction.enums.TransactionType;
import com.fintrack.api.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(Long userId) {

        List<Transaction> transactions =
                transactionRepository.findByUserId(userId);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {

            if (transaction.getType() == TransactionType.INCOME) {

                totalIncome = totalIncome.add(
                        transaction.getAmount()
                );

            } else if (transaction.getType() == TransactionType.EXPENSE) {

                totalExpenses = totalExpenses.add(
                        transaction.getAmount()
                );
            }
        }

        BigDecimal balance = totalIncome.subtract(totalExpenses);

        return new DashboardSummaryResponse(
                totalIncome,
                totalExpenses,
                balance,
                transactions.size()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlySummaryResponse> getMonthlySummary(Long userId) {

        List<Transaction> transactions =
                transactionRepository.findByUserId(userId);

        Map<YearMonth, List<Transaction>> grouped =
                new TreeMap<>();

        for (Transaction transaction : transactions) {

            YearMonth month = YearMonth.from(
                    transaction.getTransactionDate()
            );

            grouped.computeIfAbsent(
                    month,
                    key -> new ArrayList<>()
            ).add(transaction);
        }

        return grouped.entrySet().stream()
                .map(entry -> {

                    BigDecimal income = BigDecimal.ZERO;
                    BigDecimal expenses = BigDecimal.ZERO;

                    for (Transaction transaction : entry.getValue()) {

                        if (transaction.getType() == TransactionType.INCOME) {
                            income = income.add(transaction.getAmount());
                        } else {
                            expenses = expenses.add(transaction.getAmount());
                        }
                    }

                    return new MonthlySummaryResponse(
                            entry.getKey().getYear(),
                            entry.getKey().getMonthValue(),
                            income,
                            expenses,
                            income.subtract(expenses)
                    );
                })
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<ExpenseByCategoryResponse> getExpensesByCategory(Long userId) {

        List<Transaction> transactions =
                transactionRepository.findByUserId(userId);

        Map<Long, List<Transaction>> grouped = transactions.stream()
                .filter(transaction ->
                        transaction.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        transaction -> transaction.getCategory().getId()
                ));

        return grouped.values().stream()
                .map(categoryTransactions -> {

                    Transaction first = categoryTransactions.get(0);

                    BigDecimal total = categoryTransactions.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new ExpenseByCategoryResponse(
                            first.getCategory().getId(),
                            first.getCategory().getName(),
                            first.getCategory().getColor(),
                            total,
                            categoryTransactions.size()
                    );
                })
                .sorted(Comparator.comparing(
                        ExpenseByCategoryResponse::totalAmount
                ).reversed())
                .toList();
    }

}
