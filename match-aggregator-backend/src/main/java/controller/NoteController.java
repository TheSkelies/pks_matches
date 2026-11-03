package ru.mirea.matchaggregator.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mirea.matchaggregator.dto.note.NoteCreateRequest;
import ru.mirea.matchaggregator.dto.note.NoteDto;
import ru.mirea.matchaggregator.dto.note.NoteUpdateRequest;
import ru.mirea.matchaggregator.service.NoteService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items/{matchId}/notes")
public class NoteController {

    private final NoteService service;

    public NoteController(NoteService service) {
        this.service = service;
    }

    @GetMapping
    public List<NoteDto> findAll(@PathVariable UUID matchId) {
        return service.findByMatch(matchId).stream().map(NoteDto::from).toList();
    }

    @PostMapping
    public ResponseEntity<NoteDto> create(@PathVariable UUID matchId,
                                          @Valid @RequestBody NoteCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NoteDto.from(service.create(matchId, req)));
    }

    @PutMapping("/{noteId}")
    public NoteDto update(@PathVariable UUID matchId,
                          @PathVariable UUID noteId,
                          @Valid @RequestBody NoteUpdateRequest req) {
        return NoteDto.from(service.update(noteId, req));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> delete(@PathVariable UUID matchId, @PathVariable UUID noteId) {
        service.delete(noteId);
        return ResponseEntity.noContent().build();
    }
}
