package com.fintrack.api.auth.dto;

public record RegisterResponse(
        Long id,
        String name,
        String email
) {}
