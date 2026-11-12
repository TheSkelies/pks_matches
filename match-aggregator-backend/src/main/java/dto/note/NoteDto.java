package ru.mirea.matchaggregator.dto.note;

import ru.mirea.matchaggregator.domain.Note;
import java.time.Instant;
import java.util.UUID;

public record NoteDto(
        UUID id,
        UUID matchId,
        String authorEmail,
        String authorName,
        String content,
        boolean pinned,
        Instant createdAt,
        Instant updatedAt
) {
    public static NoteDto from(Note n) {
        return new NoteDto(
                n.getId(),
                n.getMatch().getId(),
                n.getAuthor().getEmail(),
                n.getAuthor().getFullName(),
                n.getContent(),
                n.isPinned(),
                n.getCreatedAt(),
                n.getUpdatedAt()
        );
    }
}
