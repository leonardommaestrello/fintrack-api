package com.fintrack.api.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.fintrack.api.common.exception.TransactionNotFoundException;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");

        return buildError(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                "Data conflicts with an existing record",
                request
        );
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }


    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                request
        );
    }


    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiError> handleCategoryNotFound(
            CategoryNotFoundException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }


    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleCategoryAlreadyExists(
            CategoryAlreadyExistsException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }


    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<ApiError> handleCategoryInUse(
            CategoryInUseException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }


    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ApiError> handleTransactionNotFound(
            TransactionNotFoundException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }



}
