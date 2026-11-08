package com.fintrack.api.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
