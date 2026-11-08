
package com.fintrack.api.transaction.service;

import com.fintrack.api.transaction.dto.TransactionRequest;
import com.fintrack.api.transaction.dto.TransactionResponse;
import com.fintrack.api.transaction.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    List<TransactionResponse> findAll(Long userId);

    TransactionResponse findById(
            Long userId,
            Long transactionId
    );

    TransactionResponse create(
            Long userId,
            TransactionRequest request
    );

    TransactionResponse update(
            Long userId,
            Long transactionId,
            TransactionRequest request
    );

    void delete(Long userId, Long transactionId);

    List<TransactionResponse> findAllFiltered(
            Long userId,
            TransactionType type,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate
    );

    byte[] exportCsv(
            Long userId,
            TransactionType type,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate
    );
}
