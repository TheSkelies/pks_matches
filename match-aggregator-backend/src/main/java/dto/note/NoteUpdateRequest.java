package ru.mirea.matchaggregator.dto.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteUpdateRequest(
        @NotBlank @Size(max = 2000) String content,
        boolean pinned
) {}
