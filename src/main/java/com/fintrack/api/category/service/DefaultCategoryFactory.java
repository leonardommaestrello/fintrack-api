package com.fintrack.api.category.service;

import com.fintrack.api.category.entity.Category;
import com.fintrack.api.category.repository.CategoryRepository;
import com.fintrack.api.transaction.enums.TransactionType;
import com.fintrack.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultCategoryFactory {

    private final CategoryRepository categoryRepository;

    public void createDefaultCategories(User user) {

        List<Category> categories = List.of(
                create(user, "Salary", TransactionType.INCOME, "#10B981"),
                create(user, "Freelance", TransactionType.INCOME, "#34D399"),
                create(user, "Investments", TransactionType.INCOME, "#059669"),
                create(user, "Gifts", TransactionType.INCOME, "#6EE7B7"),
                create(user, "Other Income", TransactionType.INCOME, "#047857"),

                create(user, "Housing", TransactionType.EXPENSE, "#EF4444"),
                create(user, "Food", TransactionType.EXPENSE, "#F97316"),
                create(user, "Transportation", TransactionType.EXPENSE, "#F59E0B"),
                create(user, "Health", TransactionType.EXPENSE, "#EC4899"),
                create(user, "Entertainment", TransactionType.EXPENSE, "#8B5CF6")
        );

        categoryRepository.saveAll(categories);
    }

    private Category create(
            User user,
            String name,
            TransactionType type,
            String color) {

        Category category = new Category();

        category.setUser(user);
        category.setName(name);
        category.setType(type);
        category.setColor(color);

        return category;
    }
}
