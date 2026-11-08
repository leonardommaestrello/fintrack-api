
package com.fintrack.api.transaction.service.impl;

import com.fintrack.api.category.entity.Category;
import com.fintrack.api.category.repository.CategoryRepository;
import com.fintrack.api.common.exception.TransactionNotFoundException;
import com.fintrack.api.transaction.dto.TransactionRequest;
import com.fintrack.api.transaction.dto.TransactionResponse;
import com.fintrack.api.transaction.entity.Transaction;
import com.fintrack.api.transaction.enums.TransactionType;
import com.fintrack.api.transaction.export.TransactionCsvExporter;
import com.fintrack.api.transaction.mapper.TransactionMapper;
import com.fintrack.api.transaction.repository.TransactionRepository;
import com.fintrack.api.user.entity.User;
import com.fintrack.api.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private TransactionCsvExporter transactionCsvExporter;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    private TransactionRequest createRequest() {
        return new TransactionRequest(
                "Groceries",
                new BigDecimal("250.90"),
                TransactionType.EXPENSE,
                7L,
                LocalDate.of(2026, 11, 7)
        );
    }

    private TransactionResponse createResponse() {
        return new TransactionResponse(
                1L,
                "Groceries",
                new BigDecimal("250.90"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 11, 7),
                7L,
                "Food",
                "#F97316"
        );
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoTransactions() {

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of());

        List<TransactionResponse> result =
                transactionService.findAll(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(transactionRepository).findByUserId(userId);
        verifyNoInteractions(transactionMapper);
    }

    @Test
    void shouldReturnTransactionsBelongingToUser() {

        Transaction transaction = new Transaction();
        TransactionResponse response = createResponse();

        when(transactionRepository.findByUserId(userId))
                .thenReturn(List.of(transaction));

        when(transactionMapper.toResponse(transaction))
                .thenReturn(response);

        List<TransactionResponse> result =
                transactionService.findAll(userId);

        assertEquals(1, result.size());
        assertEquals("Groceries", result.get(0).description());
        assertEquals(
                0,
                new BigDecimal("250.90")
                        .compareTo(result.get(0).amount())
        );

        verify(transactionRepository).findByUserId(userId);
        verify(transactionMapper).toResponse(transaction);
    }

    @Test
    void shouldRejectInvalidDateRange() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transactionService.findAllFiltered(
                                userId,
                                null,
                                null,
                                LocalDate.of(2026, 12, 1),
                                LocalDate.of(2026, 11, 1)
                        )
                );

        assertEquals(
                "Start date cannot be after end date",
                exception.getMessage()
        );

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldFindTransactionById() {

        Long transactionId = 1L;
        Transaction transaction = new Transaction();
        TransactionResponse response = createResponse();

        when(transactionRepository.findByIdAndUserId(
                transactionId, userId
        )).thenReturn(Optional.of(transaction));

        when(transactionMapper.toResponse(transaction))
                .thenReturn(response);

        TransactionResponse result =
                transactionService.findById(userId, transactionId);

        assertNotNull(result);
        assertEquals(transactionId, result.id());
        assertEquals("Groceries", result.description());

        verify(transactionRepository)
                .findByIdAndUserId(transactionId, userId);
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {

        Long transactionId = 999L;

        when(transactionRepository.findByIdAndUserId(
                transactionId, userId
        )).thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.findById(
                        userId, transactionId
                )
        );

        verifyNoInteractions(transactionMapper);
    }

    @Test
    void shouldDeleteTransactionBelongingToUser() {

        Long transactionId = 1L;
        Transaction transaction = new Transaction();

        when(transactionRepository.findByIdAndUserId(
                transactionId, userId
        )).thenReturn(Optional.of(transaction));

        transactionService.delete(userId, transactionId);

        verify(transactionRepository)
                .findByIdAndUserId(transactionId, userId);

        verify(transactionRepository).delete(transaction);
    }

    @Test
    void shouldNotDeleteTransactionFromAnotherUser() {

        Long transactionId = 10L;

        when(transactionRepository.findByIdAndUserId(
                transactionId, userId
        )).thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.delete(
                        userId, transactionId
                )
        );

        verify(transactionRepository, never())
                .delete(any(Transaction.class));
    }

    @Test
    void shouldCreateTransactionWithValidCategory() {

        TransactionRequest request = createRequest();
        TransactionResponse response = createResponse();

        Category category = new Category();
        category.setType(TransactionType.EXPENSE);

        User user = new User();
        Transaction transaction = new Transaction();

        when(categoryRepository.findByIdAndUserId(7L, userId))
                .thenReturn(Optional.of(category));

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        when(transactionMapper.toResponse(transaction))
                .thenReturn(response);

        TransactionResponse result =
                transactionService.create(userId, request);

        assertNotNull(result);
        assertEquals("Groceries", result.description());

        verify(transactionRepository).save(
                argThat(saved ->
                        saved.getUser() == user
                                && saved.getCategory() == category
                                && saved.getType() == TransactionType.EXPENSE
                                && saved.getAmount().compareTo(
                                new BigDecimal("250.90")
                        ) == 0
                )
        );
    }

    @Test
    void shouldRejectTransactionWithDifferentCategoryType() {

        TransactionRequest request = createRequest();

        Category category = new Category();
        category.setType(TransactionType.INCOME);

        when(categoryRepository.findByIdAndUserId(7L, userId))
                .thenReturn(Optional.of(category));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transactionService.create(userId, request)
                );

        assertEquals(
                "Transaction type must match category type",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldUpdateExistingTransaction() {

        Long transactionId = 1L;
        TransactionRequest request = createRequest();
        TransactionResponse response = createResponse();

        Transaction transaction = new Transaction();

        Category category = new Category();
        category.setType(TransactionType.EXPENSE);

        when(transactionRepository.findByIdAndUserId(
                transactionId, userId
        )).thenReturn(Optional.of(transaction));

        when(categoryRepository.findByIdAndUserId(7L, userId))
                .thenReturn(Optional.of(category));

        when(transactionMapper.toResponse(transaction))
                .thenReturn(response);

        TransactionResponse result =
                transactionService.update(userId, transactionId, request);

        assertNotNull(result);
        assertEquals("Groceries", result.description());

        assertEquals("Groceries", transaction.getDescription());
        assertEquals(TransactionType.EXPENSE, transaction.getType());
        assertSame(category, transaction.getCategory());

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldNotUpdateTransactionFromAnotherUser() {

        Long transactionId = 10L;
        TransactionRequest request = createRequest();

        when(transactionRepository.findByIdAndUserId(
                transactionId, userId
        )).thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.update(
                        userId,
                        transactionId,
                        request
                )
        );

        verifyNoInteractions(categoryRepository);
        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldApplyTransactionFilters() {

        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        Transaction transaction = new Transaction();
        TransactionResponse response = createResponse();

        when(transactionRepository.findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<Transaction>>any()
        )).thenReturn(List.of(transaction));

        when(transactionMapper.toResponse(transaction))
                .thenReturn(response);

        List<TransactionResponse> result =
                transactionService.findAllFiltered(
                        userId,
                        TransactionType.EXPENSE,
                        7L,
                        startDate,
                        endDate
                );

        assertEquals(1, result.size());
        assertEquals(TransactionType.EXPENSE, result.get(0).type());
        assertEquals(7L, result.get(0).categoryId());

        verify(transactionRepository).findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<Transaction>>any()
        );
    }

    @Test
    void shouldExportFilteredTransactionsToCsv() {

        Transaction transaction = new Transaction();
        TransactionResponse response = createResponse();

        byte[] expectedCsv = (
                "Date,Description,Type,Category,Amount\r\n"
                        + "2026-11-07,Groceries,EXPENSE,Food,250.90\r\n"
        ).getBytes(StandardCharsets.UTF_8);

        when(transactionRepository.findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<Transaction>>any()
        )).thenReturn(List.of(transaction));

        when(transactionMapper.toResponse(transaction))
                .thenReturn(response);

        when(transactionCsvExporter.export(List.of(response)))
                .thenReturn(expectedCsv);

        byte[] result = transactionService.exportCsv(
                userId,
                TransactionType.EXPENSE,
                7L,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );

        assertArrayEquals(expectedCsv, result);

        verify(transactionCsvExporter)
                .export(List.of(response));
    }

    @Test
    void shouldRejectCsvExportWithInvalidDateRange() {

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.exportCsv(
                        userId,
                        null,
                        null,
                        LocalDate.of(2026, 12, 1),
                        LocalDate.of(2026, 11, 1)
                )
        );

        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transactionCsvExporter);
    }
}
