package ru.mirea.matchaggregator.dto.match;

import java.util.List;

public record CsvPreviewResponse(
        int totalRows,
        int validRows,
        int invalidRows,
        List<RowError> errors,
        List<MatchCsvRow> validData
) {
    public record RowError(int rowNumber, String message) {}
    public record MatchCsvRow(
            String externalCode, String tournamentName,
            String team1, String team2, String matchDate,
            String status, String stage, int score1, int score2, int priority
    ) {}
}
