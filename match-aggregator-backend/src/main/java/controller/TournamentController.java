package ru.mirea.matchaggregator.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mirea.matchaggregator.domain.Tournament;
import ru.mirea.matchaggregator.service.TournamentService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/references/tournaments")
public class TournamentController {

    private final TournamentService service;

    public TournamentController(TournamentService service) {
        this.service = service;
    }

    public record CreateTournamentRequest(
            @NotBlank String name,
            @NotNull LocalDate startDate,
            LocalDate endDate
    ) {}

    public record UpdateTournamentRequest(
            @NotBlank String name,
            @NotNull LocalDate startDate,
            LocalDate endDate,
            boolean active
    ) {}

    public record TournamentDto(UUID id, String name, LocalDate startDate, LocalDate endDate, boolean active) {
        static TournamentDto from(Tournament t) {
            return new TournamentDto(t.getId(), t.getName(), t.getStartDate(), t.getEndDate(), t.isActive());
        }
    }

    @GetMapping
    public List<TournamentDto> findAll() {
        return service.findAll().stream().map(TournamentDto::from).toList();
    }

    @PostMapping
    public ResponseEntity<TournamentDto> create(@Valid @RequestBody CreateTournamentRequest req) {
        Tournament t = service.create(req.name(), req.startDate(), req.endDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(TournamentDto.from(t));
    }

    @PutMapping("/{id}")
    public TournamentDto update(@PathVariable UUID id, @Valid @RequestBody UpdateTournamentRequest req) {
        Tournament t = service.update(id, req.name(), req.startDate(), req.endDate(), req.active());
        return TournamentDto.from(t);
    }
}
