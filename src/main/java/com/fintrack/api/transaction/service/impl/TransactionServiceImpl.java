
package com.fintrack.api.transaction.service.impl;

import com.fintrack.api.category.entity.Category;
import com.fintrack.api.category.repository.CategoryRepository;
import com.fintrack.api.common.exception.CategoryNotFoundException;
import com.fintrack.api.common.exception.TransactionNotFoundException;
import com.fintrack.api.transaction.dto.TransactionRequest;
import com.fintrack.api.transaction.dto.TransactionResponse;
import com.fintrack.api.transaction.entity.Transaction;
import com.fintrack.api.transaction.enums.TransactionType;
import com.fintrack.api.transaction.export.TransactionCsvExporter;
import com.fintrack.api.transaction.mapper.TransactionMapper;
import com.fintrack.api.transaction.repository.TransactionRepository;
import com.fintrack.api.transaction.service.TransactionService;
import com.fintrack.api.transaction.specification.TransactionSpecification;
import com.fintrack.api.user.entity.User;
import com.fintrack.api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionCsvExporter transactionCsvExporter;

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll(Long userId) {

        return transactionRepository.findByUserId(userId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> findAllFiltered(
            Long userId,
            TransactionType type,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate != null
                && endDate != null
                && startDate.isAfter(endDate)) {

            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }

        Specification<Transaction> specification =
                TransactionSpecification.belongsToUser(userId)
                        .and(TransactionSpecification.hasType(type))
                        .and(TransactionSpecification.hasCategory(categoryId))
                        .and(TransactionSpecification.dateFrom(startDate))
                        .and(TransactionSpecification.dateTo(endDate));

        return transactionRepository.findAll(specification)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv(
            Long userId,
            TransactionType type,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate) {

        List<TransactionResponse> transactions =
                findAllFiltered(
                        userId,
                        type,
                        categoryId,
                        startDate,
                        endDate
                );

        return transactionCsvExporter.export(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse findById(
            Long userId,
            Long transactionId) {

        return transactionMapper.toResponse(
                findUserTransaction(userId, transactionId)
        );
    }

    @Override
    @Transactional
    public TransactionResponse create(
            Long userId,
            TransactionRequest request) {

        Category category = validateCategory(
                userId,
                request.categoryId(),
                request.type()
        );

        User user = userRepository.getReferenceById(userId);

        Transaction transaction = new Transaction();

        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setDescription(request.description().trim());
        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setTransactionDate(request.transactionDate());

        return transactionMapper.toResponse(
                transactionRepository.save(transaction)
        );
    }

    @Override
    @Transactional
    public TransactionResponse update(
            Long userId,
            Long transactionId,
            TransactionRequest request) {

        Transaction transaction =
                findUserTransaction(userId, transactionId);

        Category category = validateCategory(
                userId,
                request.categoryId(),
                request.type()
        );

        transaction.setCategory(category);
        transaction.setDescription(request.description().trim());
        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setTransactionDate(request.transactionDate());

        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public void delete(
            Long userId,
            Long transactionId) {

        transactionRepository.delete(
                findUserTransaction(userId, transactionId)
        );
    }

    private Transaction findUserTransaction(
            Long userId,
            Long transactionId) {

        return transactionRepository
                .findByIdAndUserId(transactionId, userId)
                .orElseThrow(TransactionNotFoundException::new);
    }

    private Category validateCategory(
            Long userId,
            Long categoryId,
            TransactionType type) {

        Category category = categoryRepository
                .findByIdAndUserId(categoryId, userId)
                .orElseThrow(CategoryNotFoundException::new);

        if (category.getType() != type) {
            throw new IllegalArgumentException(
                    "Transaction type must match category type"
            );
        }

        return category;
    }
}
