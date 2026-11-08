
package com.fintrack.api.category.repository;

import com.fintrack.api.category.entity.Category;
import com.fintrack.api.transaction.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdAndType(
            Long userId,
            TransactionType type
    );


    boolean existsByUserIdAndTypeAndNameIgnoreCase(
            Long userId,
            TransactionType type,
            String name
    );

    boolean existsByUserIdAndTypeAndNameIgnoreCaseAndIdNot(
            Long userId,
            TransactionType type,
            String name,
            Long id
    );

    Optional<Category> findByIdAndUserId(
            Long id,
            Long userId
    );


}
