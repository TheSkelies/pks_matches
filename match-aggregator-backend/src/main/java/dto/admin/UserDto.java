package ru.mirea.matchaggregator.dto.admin;

import ru.mirea.matchaggregator.domain.UserAccount;
import ru.mirea.matchaggregator.domain.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String fullName,
        UserRole role,
        boolean active,
        boolean mustChangePassword,
        Instant createdAt,
        Instant lastLoginAt
) {
    public static UserDto from(UserAccount u) {
        return new UserDto(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getRole(),
                u.isActive(),
                u.isMustChangePassword(),
                u.getCreatedAt(),
                null // поле lastLoginAt в сущности пока нет, можно добавить позже
        );
    }
}
