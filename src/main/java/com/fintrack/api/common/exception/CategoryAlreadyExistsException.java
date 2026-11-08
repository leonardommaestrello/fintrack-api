package com.fintrack.api.common.exception;

public class CategoryAlreadyExistsException
        extends RuntimeException {

    public CategoryAlreadyExistsException() {
        super("Category name already exists for this type");
    }
}
