
package com.fintrack.api.category.service.impl;

import com.fintrack.api.category.dto.CategoryRequest;
import com.fintrack.api.category.dto.CategoryResponse;
import com.fintrack.api.category.entity.Category;
import com.fintrack.api.category.mapper.CategoryMapper;
import com.fintrack.api.category.repository.CategoryRepository;
import com.fintrack.api.common.exception.CategoryAlreadyExistsException;
import com.fintrack.api.common.exception.CategoryInUseException;
import com.fintrack.api.common.exception.CategoryNotFoundException;
import com.fintrack.api.transaction.enums.TransactionType;
import com.fintrack.api.transaction.repository.TransactionRepository;
import com.fintrack.api.user.entity.User;
import com.fintrack.api.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Long userId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        categoryId = 7L;
    }

    private CategoryRequest createRequest() {
        return new CategoryRequest(
                "Food",
                TransactionType.EXPENSE,
                "#F97316"
        );
    }

    private CategoryResponse createResponse() {
        return new CategoryResponse(
                categoryId,
                "Food",
                TransactionType.EXPENSE,
                "#F97316"
        );
    }

    private Category createCategory() {
        Category category = new Category();
        category.setName("Food");
        category.setType(TransactionType.EXPENSE);
        category.setColor("#F97316");
        return category;
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoCategories() {

        when(categoryRepository.findByUserId(userId))
                .thenReturn(List.of());

        List<CategoryResponse> result =
                categoryService.findAll(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryRepository).findByUserId(userId);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    void shouldReturnCategoriesBelongingToUser() {

        Category category = createCategory();
        CategoryResponse response = createResponse();

        when(categoryRepository.findByUserId(userId))
                .thenReturn(List.of(category));

        when(categoryMapper.toResponse(category))
                .thenReturn(response);

        List<CategoryResponse> result =
                categoryService.findAll(userId);

        assertEquals(1, result.size());
        assertEquals("Food", result.get(0).name());

        verify(categoryRepository).findByUserId(userId);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void shouldFindCategoryById() {

        Category category = createCategory();
        CategoryResponse response = createResponse();

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        when(categoryMapper.toResponse(category))
                .thenReturn(response);

        CategoryResponse result =
                categoryService.findById(userId, categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.id());
        assertEquals("Food", result.name());
    }

    @Test
    void shouldRejectCategoryFromAnotherUser() {

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.findById(userId, categoryId)
        );

        verifyNoInteractions(categoryMapper);
    }

    @Test
    void shouldCreateCategorySuccessfully() {

        CategoryRequest request = createRequest();
        CategoryResponse response = createResponse();
        User user = new User();

        when(categoryRepository.existsByUserIdAndTypeAndNameIgnoreCase(
                userId,
                TransactionType.EXPENSE,
                "Food"
        )).thenReturn(false);

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(categoryMapper.toResponse(any(Category.class)))
                .thenReturn(response);

        CategoryResponse result =
                categoryService.create(userId, request);

        assertNotNull(result);
        assertEquals("Food", result.name());

        verify(categoryRepository).save(
                argThat(category ->
                        category.getUser() == user
                                && category.getName().equals("Food")
                                && category.getType() == TransactionType.EXPENSE
                                && category.getColor().equals("#F97316")
                )
        );
    }

    @Test
    void shouldRejectDuplicateCategory() {

        CategoryRequest request = createRequest();

        when(categoryRepository.existsByUserIdAndTypeAndNameIgnoreCase(
                userId,
                TransactionType.EXPENSE,
                "Food"
        )).thenReturn(true);

        assertThrows(
                CategoryAlreadyExistsException.class,
                () -> categoryService.create(userId, request)
        );

        verify(categoryRepository, never())
                .save(any(Category.class));

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldTrimCategoryNameWhenCreating() {

        CategoryRequest request = new CategoryRequest(
                "  Food  ",
                TransactionType.EXPENSE,
                "#F97316"
        );

        User user = new User();

        when(userRepository.getReferenceById(userId))
                .thenReturn(user);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.create(userId, request);

        verify(categoryRepository).existsByUserIdAndTypeAndNameIgnoreCase(
                userId,
                TransactionType.EXPENSE,
                "Food"
        );

        verify(categoryRepository).save(
                argThat(category -> category.getName().equals("Food"))
        );
    }

    @Test
    void shouldUpdateCategorySuccessfully() {

        Category category = createCategory();
        CategoryResponse response = createResponse();

        CategoryRequest request = new CategoryRequest(
                "Groceries",
                TransactionType.EXPENSE,
                "#22C55E"
        );

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        when(categoryMapper.toResponse(category))
                .thenReturn(response);

        CategoryResponse result =
                categoryService.update(userId, categoryId, request);

        assertNotNull(result);
        assertEquals("Groceries", category.getName());
        assertEquals("#22C55E", category.getColor());

        verify(categoryRepository)
                .existsByUserIdAndTypeAndNameIgnoreCaseAndIdNot(
                        userId,
                        TransactionType.EXPENSE,
                        "Groceries",
                        categoryId
                );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void shouldRejectChangingCategoryType() {

        Category category = createCategory();

        CategoryRequest request = new CategoryRequest(
                "Food",
                TransactionType.INCOME,
                "#F97316"
        );

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> categoryService.update(
                                userId,
                                categoryId,
                                request
                        )
                );

        assertEquals(
                "Category type cannot be changed",
                exception.getMessage()
        );

        verifyNoInteractions(categoryMapper);
    }

    @Test
    void shouldRejectDuplicateCategoryOnUpdate() {

        Category category = createCategory();

        CategoryRequest request = new CategoryRequest(
                "Housing",
                TransactionType.EXPENSE,
                "#F97316"
        );

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        when(categoryRepository
                .existsByUserIdAndTypeAndNameIgnoreCaseAndIdNot(
                        userId,
                        TransactionType.EXPENSE,
                        "Housing",
                        categoryId
                )).thenReturn(true);

        assertThrows(
                CategoryAlreadyExistsException.class,
                () -> categoryService.update(
                        userId,
                        categoryId,
                        request
                )
        );

        assertEquals("Food", category.getName());
        verifyNoInteractions(categoryMapper);
    }

    @Test
    void shouldDeleteCategoryWhenNotInUse() {

        Category category = createCategory();

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        categoryService.delete(userId, categoryId);

        verify(transactionRepository)
                .existsByUserIdAndCategoryId(userId, categoryId);

        verify(categoryRepository).delete(category);
        verify(categoryRepository).flush();
    }

    @Test
    void shouldRejectDeletingCategoryInUse() {

        Category category = createCategory();

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        when(transactionRepository.existsByUserIdAndCategoryId(
                userId, categoryId
        )).thenReturn(true);

        assertThrows(
                CategoryInUseException.class,
                () -> categoryService.delete(userId, categoryId)
        );

        verify(categoryRepository, never())
                .delete(any(Category.class));

        verify(categoryRepository, never()).flush();
    }
}
