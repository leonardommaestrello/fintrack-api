
package com.fintrack.api.transaction.controller;

import com.fintrack.api.transaction.dto.TransactionRequest;
import com.fintrack.api.transaction.dto.TransactionResponse;
import com.fintrack.api.transaction.enums.TransactionType;
import com.fintrack.api.transaction.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findAll(
            @AuthenticationPrincipal Long userId,

            @RequestParam(required = false)
            TransactionType type,

            @RequestParam(required = false)
            Long categoryId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return ResponseEntity.ok(
                transactionService.findAllFiltered(
                        userId,
                        type,
                        categoryId,
                        startDate,
                        endDate
                )
        );
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @AuthenticationPrincipal Long userId,

            @RequestParam(required = false)
            TransactionType type,

            @RequestParam(required = false)
            Long categoryId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        byte[] csv = transactionService.exportCsv(
                userId,
                type,
                categoryId,
                startDate,
                endDate
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"fintrack-transactions.csv\""
                )
                .contentType(
                        new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8)
                )
                .contentLength(csv.length)
                .body(csv);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                transactionService.findById(userId, id)
        );
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TransactionRequest request) {

        TransactionResponse response =
                transactionService.create(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {

        return ResponseEntity.ok(
                transactionService.update(userId, id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {

        transactionService.delete(userId, id);

        return ResponseEntity.noContent().build();
    }
}
