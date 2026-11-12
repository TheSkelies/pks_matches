package ru.mirea.matchaggregator.dto.match;

import java.util.List;
import java.util.UUID;

public record BulkResponse(
        int successCount,
        int errorCount,
        List<BulkError> errors
) {
    public record BulkError(UUID id, String message) {}
}
