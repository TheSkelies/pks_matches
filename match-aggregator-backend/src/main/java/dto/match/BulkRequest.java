package ru.mirea.matchaggregator.dto.match;

import ru.mirea.matchaggregator.domain.enums.MatchStatus;

import java.util.List;
import java.util.UUID;

public record BulkRequest(
        List<UUID> ids,
        Action action,
        MatchStatus newStatus,
        String tagName
) {
    public enum Action {
        ARCHIVE,
        RESTORE,
        CHANGE_STATUS,
        ADD_TAG,
        REMOVE_TAG
    }
}
