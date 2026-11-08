package com.fintrack.api.category.mapper;

import com.fintrack.api.category.dto.CategoryResponse;
import com.fintrack.api.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getColor()
        );
    }
}
