package ru.mirea.matchaggregator.dto.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteCreateRequest(
        @NotBlank @Size(max = 2000) String content,
        boolean pinned
) {}
