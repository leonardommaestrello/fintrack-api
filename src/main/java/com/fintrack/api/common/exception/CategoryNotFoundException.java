package com.fintrack.api.common.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException() {
        super("Category not found");
    }
}
