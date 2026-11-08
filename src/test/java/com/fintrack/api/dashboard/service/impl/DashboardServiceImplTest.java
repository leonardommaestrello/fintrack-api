package com.fintrack.api.dashboard.service.impl;

import com.fintrack.api.category.entity.Category;
import com.fintrack.api.dashboard.dto.DashboardSummaryResponse;
import com.fintrack.api.dashboard.dto.ExpenseByCategoryResponse;
import com.fintrack.api.dashboard.dto.MonthlySummaryResponse;
import com.fintrack.api.transaction.entity.Transaction;
import com.fintrack.api.transaction.enums.TransactionType;
import com.fintrack.api.transaction.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    private Transaction createTransaction(
            TransactionType type,
            String amount,
            LocalDate date,
            Category category) {

        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setTransactionDate(date);
        transaction.setCategory(category);

        return transaction;
    }

    private Category createCategory(
            Long id,
            String name,
            String color) {

        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setColor(color);

        return category;
    }

    @Test
    void shouldReturnZeroSummaryWhenNoTransactions() {

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of());

        DashboardSummaryResponse result =
                dashboardService.getSummary(userId);

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(result.totalIncome())
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(result.totalExpenses())
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(result.balance())
        );

        assertEquals(0, result.transactionCount());

        verify(transactionRepository).findByUserId(userId);
    }

    @Test
    void shouldCalculateIncomeExpensesAndBalance() {

        Transaction income = createTransaction(
                TransactionType.INCOME,
                "10000.00",
                LocalDate.of(2026, 11, 7),
                null
        );

        Transaction expense1 = createTransaction(
                TransactionType.EXPENSE,
                "250.90",
                LocalDate.of(2026, 11, 7),
                null
        );

        Transaction expense2 = createTransaction(
                TransactionType.EXPENSE,
                "1500.00",
                LocalDate.of(2026, 11, 7),
                null
        );

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of(income, expense1, expense2));

        DashboardSummaryResponse result =
                dashboardService.getSummary(userId);

        assertEquals(
                0,
                new BigDecimal("10000.00")
                        .compareTo(result.totalIncome())
        );

        assertEquals(
                0,
                new BigDecimal("1750.90")
                        .compareTo(result.totalExpenses())
        );

        assertEquals(
                0,
                new BigDecimal("8249.10")
                        .compareTo(result.balance())
        );

        assertEquals(3, result.transactionCount());
    }

    @Test
    void shouldCalculateNegativeBalance() {

        Transaction income = createTransaction(
                TransactionType.INCOME,
                "1000.00",
                LocalDate.of(2026, 11, 7),
                null
        );

        Transaction expense = createTransaction(
                TransactionType.EXPENSE,
                "1500.00",
                LocalDate.of(2026, 11, 7),
                null
        );

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of(income, expense));

        DashboardSummaryResponse result =
                dashboardService.getSummary(userId);

        assertEquals(
                0,
                new BigDecimal("-500.00")
                        .compareTo(result.balance())
        );
    }

    @Test
    void shouldReturnEmptyMonthlySummaryWhenNoTransactions() {

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of());

        List<MonthlySummaryResponse> result =
                dashboardService.getMonthlySummary(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGroupTransactionsByMonth() {

        Transaction novemberIncome = createTransaction(
                TransactionType.INCOME,
                "5000.00",
                LocalDate.of(2026, 11, 5),
                null
        );

        Transaction novemberExpense = createTransaction(
                TransactionType.EXPENSE,
                "1200.00",
                LocalDate.of(2026, 11, 10),
                null
        );

        Transaction decemberIncome = createTransaction(
                TransactionType.INCOME,
                "6000.00",
                LocalDate.of(2026, 12, 5),
                null
        );

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of(
                        novemberIncome,
                        novemberExpense,
                        decemberIncome
                ));

        List<MonthlySummaryResponse> result =
                dashboardService.getMonthlySummary(userId);

        assertEquals(2, result.size());

        MonthlySummaryResponse november = result.get(0);

        assertEquals(2026, november.year());
        assertEquals(11, november.month());

        assertEquals(
                0,
                new BigDecimal("5000.00")
                        .compareTo(november.totalIncome())
        );

        assertEquals(
                0,
                new BigDecimal("1200.00")
                        .compareTo(november.totalExpenses())
        );

        assertEquals(
                0,
                new BigDecimal("3800.00")
                        .compareTo(november.balance())
        );

        MonthlySummaryResponse december = result.get(1);

        assertEquals(2026, december.year());
        assertEquals(12, december.month());

        assertEquals(
                0,
                new BigDecimal("6000.00")
                        .compareTo(december.balance())
        );
    }

    @Test
    void shouldSortMonthlySummaryChronologically() {

        Transaction december = createTransaction(
                TransactionType.INCOME,
                "1000.00",
                LocalDate.of(2026, 12, 10),
                null
        );

        Transaction january = createTransaction(
                TransactionType.INCOME,
                "2000.00",
                LocalDate.of(2026, 1, 10),
                null
        );

        Transaction november = createTransaction(
                TransactionType.INCOME,
                "3000.00",
                LocalDate.of(2026, 11, 10),
                null
        );

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of(december, january, november));

        List<MonthlySummaryResponse> result =
                dashboardService.getMonthlySummary(userId);

        assertEquals(3, result.size());

        assertEquals(1, result.get(0).month());
        assertEquals(11, result.get(1).month());
        assertEquals(12, result.get(2).month());
    }

    @Test
    void shouldReturnEmptyExpensesWhenNoTransactions() {

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of());

        List<ExpenseByCategoryResponse> result =
                dashboardService.getExpensesByCategory(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIgnoreIncomeInExpensesByCategory() {

        Category salary = createCategory(
                1L,
                "Salary",
                "#10B981"
        );

        Transaction income = createTransaction(
                TransactionType.INCOME,
                "10000.00",
                LocalDate.of(2026, 11, 7),
                salary
        );

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of(income));

        List<ExpenseByCategoryResponse> result =
                dashboardService.getExpensesByCategory(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGroupExpensesByCategory() {

        Category food = createCategory(
                7L,
                "Food",
                "#F97316"
        );

        Transaction expense1 = createTransaction(
                TransactionType.EXPENSE,
                "250.90",
                LocalDate.of(2026, 11, 7),
                food
        );

        Transaction expense2 = createTransaction(
                TransactionType.EXPENSE,
                "1500.00",
                LocalDate.of(2026, 11, 8),
                food
        );

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of(expense1, expense2));

        List<ExpenseByCategoryResponse> result =
                dashboardService.getExpensesByCategory(userId);

        assertEquals(1, result.size());

        ExpenseByCategoryResponse foodResult = result.get(0);

        assertEquals(7L, foodResult.categoryId());
        assertEquals("Food", foodResult.categoryName());
        assertEquals("#F97316", foodResult.categoryColor());

        assertEquals(
                0,
                new BigDecimal("1750.90")
                        .compareTo(foodResult.totalAmount())
        );

        assertEquals(2, foodResult.transactionCount());
    }

    @Test
    void shouldSortExpenseCategoriesByHighestAmount() {

        Category food = createCategory(
                7L,
                "Food",
                "#F97316"
        );

        Category housing = createCategory(
                6L,
                "Housing",
                "#EF4444"
        );

        Transaction foodExpense = createTransaction(
                TransactionType.EXPENSE,
                "500.00",
                LocalDate.of(2026, 11, 7),
                food
        );

        Transaction housingExpense = createTransaction(
                TransactionType.EXPENSE,
                "2000.00",
                LocalDate.of(2026, 11, 7),
                housing
        );

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of(foodExpense, housingExpense));

        List<ExpenseByCategoryResponse> result =
                dashboardService.getExpensesByCategory(userId);

        assertEquals(2, result.size());

        assertEquals("Housing", result.get(0).categoryName());
        assertEquals("Food", result.get(1).categoryName());
    }
}
