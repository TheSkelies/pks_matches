package ru.mirea.matchaggregator.dto.analytics;

import java.util.Map;

public record AnalyticsResponse(
        long totalMatches,
        long activeMatches,
        long archivedMatches,
        long importantMatches,
        double avgPriority,
        Map<String, Long> byTournament,
        Map<String, Long> byStatus,
        Map<String, Long> byStage
) {}
