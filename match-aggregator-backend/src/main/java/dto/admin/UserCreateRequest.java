package ru.mirea.matchaggregator.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.mirea.matchaggregator.domain.enums.UserRole;

public record UserCreateRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull UserRole role
) {}
