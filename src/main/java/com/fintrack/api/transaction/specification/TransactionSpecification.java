package com.fintrack.api.transaction.specification;

import com.fintrack.api.transaction.entity.Transaction;
import com.fintrack.api.transaction.enums.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class TransactionSpecification {

    private TransactionSpecification() {
    }

    public static Specification<Transaction> belongsToUser(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, cb) ->
                type == null
                        ? cb.conjunction()
                        : cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> dateFrom(LocalDate startDate) {
        return (root, query, cb) ->
                startDate == null
                        ? cb.conjunction()
                        : cb.greaterThanOrEqualTo(
                        root.get("transactionDate"),
                        startDate
                );
    }

    public static Specification<Transaction> dateTo(LocalDate endDate) {
        return (root, query, cb) ->
                endDate == null
                        ? cb.conjunction()
                        : cb.lessThanOrEqualTo(
                        root.get("transactionDate"),
                        endDate
                );
    }
}
