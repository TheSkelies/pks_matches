package ru.mirea.matchaggregator.dto.match;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.mirea.matchaggregator.domain.Match;
import ru.mirea.matchaggregator.domain.enums.MatchStage;
import ru.mirea.matchaggregator.domain.enums.MatchStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record MatchDto(
        UUID id,
        String externalCode,
        UUID tournamentId,
        String tournamentName,
        String team1,
        String team2,
        Instant matchDate,
        MatchStatus status,
        MatchStage stage,
        int score1,
        int score2,
        int priority,
        @JsonProperty("isImportant") boolean isImportant, // <--- Явно указываем имя для JSON
        boolean archived,
        long version,
        Instant createdAt,
        Instant updatedAt,
        Set<String> tags
) {
    public static MatchDto from(Match m) {
        return new MatchDto(
                m.getId(),
                m.getExternalCode(),
                m.getTournament() != null ? m.getTournament().getId() : null,
                m.getTournament() != null ? m.getTournament().getName() : "Без турнира",
                m.getTeam1(),
                m.getTeam2(),
                m.getMatchDate(),
                m.getStatus(),
                m.getStage(),
                m.getScore1(),
                m.getScore2(),
                m.getPriority(),
                m.isImportant(),
                m.isArchived(),
                m.getVersion(),
                m.getCreatedAt(),
                m.getUpdatedAt(),
                m.getTags() != null ? m.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet()) : Set.of()
        );
    }
}
