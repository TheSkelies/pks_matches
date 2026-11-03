package ru.mirea.matchaggregator.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.mirea.matchaggregator.domain.enums.UserRole;

public record UserUpdateRequest(
        @NotBlank @Email String email,
        @NotBlank String fullName,
        @NotNull UserRole role,
        boolean active
) {}
