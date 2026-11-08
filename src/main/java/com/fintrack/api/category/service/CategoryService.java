
package com.fintrack.api.category.service;

import com.fintrack.api.category.dto.CategoryRequest;
import com.fintrack.api.category.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> findAll(Long userId);

    CategoryResponse findById(Long userId, Long categoryId);

    CategoryResponse create(Long userId, CategoryRequest request);

    CategoryResponse update(
            Long userId,
            Long categoryId,
            CategoryRequest request
    );

    void delete(Long userId, Long categoryId);
}
