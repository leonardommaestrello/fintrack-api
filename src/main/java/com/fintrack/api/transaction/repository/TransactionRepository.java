
package com.fintrack.api.transaction.repository;

import com.fintrack.api.transaction.entity.Transaction;
import com.fintrack.api.transaction.enums.TransactionType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndType(
            Long userId,
            TransactionType type
    );

    List<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Transaction> findByUserIdAndCategoryId(
            Long userId,
            Long categoryId
    );

    boolean existsByUserIdAndCategoryId(
            Long userId,
            Long categoryId
    );

    Optional<Transaction> findByIdAndUserId(
            Long id,
            Long userId
    );
}
