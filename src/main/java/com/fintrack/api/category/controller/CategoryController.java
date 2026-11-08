
package com.fintrack.api.category.controller;

import com.fintrack.api.category.dto.CategoryRequest;
import com.fintrack.api.category.dto.CategoryResponse;
import com.fintrack.api.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.ok(
                categoryService.findAll(userId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                categoryService.findById(userId, id)
        );
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse response =
                categoryService.create(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        return ResponseEntity.ok(
                categoryService.update(userId, id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        categoryService.delete(userId, id);

        return ResponseEntity.noContent().build();
    }
}
