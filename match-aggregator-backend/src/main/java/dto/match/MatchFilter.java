package ru.mirea.matchaggregator.dto.match;

import ru.mirea.matchaggregator.domain.enums.MatchStage;
import ru.mirea.matchaggregator.domain.enums.MatchStatus;

import java.time.Instant;
import java.util.UUID;

public record MatchFilter(
        String search,
        UUID tournamentId,
        MatchStatus status,
        MatchStage stage,
        Boolean important,
        Boolean archived,
        Integer priorityMin,
        Integer priorityMax,
        String tag,
        Instant dateFrom,
        Instant dateTo
) {}
