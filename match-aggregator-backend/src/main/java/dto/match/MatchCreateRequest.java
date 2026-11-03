package ru.mirea.matchaggregator.dto.match;

import jakarta.validation.constraints.*;
import ru.mirea.matchaggregator.domain.enums.MatchStage;
import ru.mirea.matchaggregator.domain.enums.MatchStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record MatchCreateRequest(
        @Size(max = 100) String externalCode,
        @NotNull UUID tournamentId,
        @NotBlank @Size(max = 255) String team1,
        @NotBlank @Size(max = 255) String team2,
        @NotNull Instant matchDate,
        @NotNull MatchStatus status,
        @NotNull MatchStage stage,
        @Min(0) int score1,
        @Min(0) int score2,
        @Min(0) @Max(100) int priority,
        boolean isImportant,
        Set<UUID> tagIds
) {}
