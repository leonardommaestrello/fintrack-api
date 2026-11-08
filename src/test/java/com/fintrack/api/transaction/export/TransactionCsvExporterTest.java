
package com.fintrack.api.transaction.export;

import com.fintrack.api.transaction.dto.TransactionResponse;
import com.fintrack.api.transaction.enums.TransactionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionCsvExporterTest {

    private static final String HEADER =
            "Date,Description,Type,Category,Amount\r\n";

    private TransactionCsvExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new TransactionCsvExporter();
    }

    private TransactionResponse createTransaction(
            String description,
            String amount,
            TransactionType type,
            String categoryName) {

        return new TransactionResponse(
                1L,
                description,
                new BigDecimal(amount),
                type,
                LocalDate.of(2026, 11, 7),
                7L,
                categoryName,
                "#F97316"
        );
    }

    private String exportToString(
            List<TransactionResponse> transactions) {

        byte[] bytes = exporter.export(transactions);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    void shouldExportHeaderWhenListIsEmpty() {

        String csv = exportToString(List.of());

        assertEquals(HEADER, csv);
    }

    @Test
    void shouldExportSingleTransaction() {

        TransactionResponse transaction = createTransaction(
                "Groceries",
                "250.90",
                TransactionType.EXPENSE,
                "Food"
        );

        String csv = exportToString(List.of(transaction));

        String expected = HEADER
                + "2026-11-07,Groceries,EXPENSE,Food,250.90\r\n";

        assertEquals(expected, csv);
    }

    @Test
    void shouldExportMultipleTransactions() {

        TransactionResponse expense = createTransaction(
                "Groceries",
                "250.90",
                TransactionType.EXPENSE,
                "Food"
        );

        TransactionResponse income = createTransaction(
                "Salary",
                "10000.00",
                TransactionType.INCOME,
                "Salary"
        );

        String csv = exportToString(
                List.of(expense, income)
        );

        String expected = HEADER
                + "2026-11-07,Groceries,EXPENSE,Food,250.90\r\n"
                + "2026-11-07,Salary,INCOME,Salary,10000.00\r\n";

        assertEquals(expected, csv);
    }

    @Test
    void shouldEscapeCommaInDescription() {

        TransactionResponse transaction = createTransaction(
                "Food, drinks and snacks",
                "150.00",
                TransactionType.EXPENSE,
                "Food"
        );

        String csv = exportToString(List.of(transaction));

        assertTrue(csv.contains(
                "\"Food, drinks and snacks\""
        ));
    }

    @Test
    void shouldEscapeDoubleQuotesInDescription() {

        TransactionResponse transaction = createTransaction(
                "Dinner at \"Restaurant\"",
                "200.00",
                TransactionType.EXPENSE,
                "Food"
        );

        String csv = exportToString(List.of(transaction));

        assertTrue(csv.contains(
                "\"Dinner at \"\"Restaurant\"\"\""
        ));
    }

    @Test
    void shouldEscapeLineBreaksInDescription() {

        TransactionResponse transaction = createTransaction(
                "First line\nSecond line",
                "100.00",
                TransactionType.EXPENSE,
                "Food"
        );

        String csv = exportToString(List.of(transaction));

        assertTrue(csv.contains(
                "\"First line\nSecond line\""
        ));
    }

    @Test
    void shouldPreserveUtf8Characters() {

        TransactionResponse transaction = createTransaction(
                "Alimentação e café",
                "35.50",
                TransactionType.EXPENSE,
                "Saúde"
        );

        byte[] bytes = exporter.export(List.of(transaction));

        String csv = new String(bytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("Alimentação e café"));
        assertTrue(csv.contains("Saúde"));

        assertArrayEquals(
                csv.getBytes(StandardCharsets.UTF_8),
                bytes
        );
    }

    @Test
    void shouldPreventFormulaInjectionStartingWithEquals() {

        TransactionResponse transaction = createTransaction(
                "=SUM(1+1)",
                "100.00",
                TransactionType.EXPENSE,
                "Food"
        );

        String csv = exportToString(List.of(transaction));

        assertTrue(csv.contains(
                "'=SUM(1+1)"
        ));

        assertFalse(csv.contains(
                ",=SUM(1+1),"
        ));
    }

    @Test
    void shouldPreventFormulaInjectionStartingWithPlus() {

        TransactionResponse transaction = createTransaction(
                "+123456",
                "100.00",
                TransactionType.EXPENSE,
                "Food"
        );

        String csv = exportToString(List.of(transaction));

        assertTrue(csv.contains("'+123456"));
    }

    @Test
    void shouldPreventFormulaInjectionStartingWithAt() {

        TransactionResponse transaction = createTransaction(
                "@SUM(1+1)",
                "100.00",
                TransactionType.EXPENSE,
                "Food"
        );

        String csv = exportToString(List.of(transaction));

        assertTrue(csv.contains("'@SUM(1+1)"));
    }

    @Test
    void shouldPreventFormulaInjectionWithLeadingWhitespace() {

        TransactionResponse transaction = createTransaction(
                "   =SUM(1+1)",
                "100.00",
                TransactionType.EXPENSE,
                "Food"
        );

        String csv = exportToString(List.of(transaction));

        assertTrue(csv.contains(
                "'   =SUM(1+1)"
        ));
    }

    @Test
    void shouldHandleNullDescriptionAndCategory() {

        TransactionResponse transaction = createTransaction(
                null,
                "100.00",
                TransactionType.EXPENSE,
                null
        );

        String csv = exportToString(List.of(transaction));

        String expected = HEADER
                + "2026-11-07,,EXPENSE,,100.00\r\n";

        assertEquals(expected, csv);
    }
}
