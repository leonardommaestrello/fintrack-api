
package com.fintrack.api.category.service.impl;

import com.fintrack.api.category.dto.CategoryRequest;
import com.fintrack.api.category.dto.CategoryResponse;
import com.fintrack.api.category.entity.Category;
import com.fintrack.api.category.mapper.CategoryMapper;
import com.fintrack.api.category.repository.CategoryRepository;
import com.fintrack.api.category.service.CategoryService;
import com.fintrack.api.common.exception.CategoryAlreadyExistsException;
import com.fintrack.api.common.exception.CategoryInUseException;
import com.fintrack.api.common.exception.CategoryNotFoundException;
import com.fintrack.api.transaction.enums.TransactionType;
import com.fintrack.api.transaction.repository.TransactionRepository;
import com.fintrack.api.user.entity.User;
import com.fintrack.api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll(Long userId) {

        return categoryRepository.findByUserId(userId)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long userId, Long categoryId) {

        Category category = findUserCategory(userId, categoryId);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {

        String name = request.name().trim();

        validateDuplicate(userId, request.type(), name, null);

        User user = userRepository.getReferenceById(userId);

        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category.setType(request.type());
        category.setColor(request.color());

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse update(
            Long userId,
            Long categoryId,
            CategoryRequest request) {

        Category category = findUserCategory(userId, categoryId);

        if (category.getType() != request.type()) {
            throw new IllegalArgumentException(
                    "Category type cannot be changed"
            );
        }

        String name = request.name().trim();

        validateDuplicate(
                userId,
                category.getType(),
                name,
                categoryId
        );

        category.setName(name);
        category.setColor(request.color());

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long categoryId) {

        Category category = findUserCategory(userId, categoryId);

        if (transactionRepository.existsByUserIdAndCategoryId(
                userId, categoryId)) {

            throw new CategoryInUseException();
        }

        categoryRepository.delete(category);
        categoryRepository.flush();
    }

    private Category findUserCategory(Long userId, Long categoryId) {

        return categoryRepository
                .findByIdAndUserId(categoryId, userId)
                .orElseThrow(CategoryNotFoundException::new);
    }

    private void validateDuplicate(
            Long userId,
            TransactionType type,
            String name,
            Long categoryId) {

        boolean exists;

        if (categoryId == null) {
            exists = categoryRepository
                    .existsByUserIdAndTypeAndNameIgnoreCase(
                            userId, type, name
                    );
        } else {
            exists = categoryRepository
                    .existsByUserIdAndTypeAndNameIgnoreCaseAndIdNot(
                            userId, type, name, categoryId
                    );
        }

        if (exists) {
            throw new CategoryAlreadyExistsException();
        }
    }
}
