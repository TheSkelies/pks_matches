package ru.mirea.matchaggregator.dto.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessExpiresInSeconds,
        long refreshExpiresInSeconds,
        String email,
        String role,
        boolean mustChangePassword
) {}
