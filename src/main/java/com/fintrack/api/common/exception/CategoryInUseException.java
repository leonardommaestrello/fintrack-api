package com.fintrack.api.common.exception;

public class CategoryInUseException extends RuntimeException {

    public CategoryInUseException() {
        super("Category cannot be deleted because it has associated transactions");
    }
}
