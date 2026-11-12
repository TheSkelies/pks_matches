package ru.mirea.matchaggregator.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mirea.matchaggregator.domain.Match;
import ru.mirea.matchaggregator.domain.enums.MatchStage;
import ru.mirea.matchaggregator.domain.enums.MatchStatus;
import ru.mirea.matchaggregator.dto.common.PageResponse;
import ru.mirea.matchaggregator.dto.match.MatchCreateRequest;
import ru.mirea.matchaggregator.dto.match.MatchDto;
import ru.mirea.matchaggregator.dto.match.MatchFilter;
import ru.mirea.matchaggregator.dto.match.MatchUpdateRequest;
import ru.mirea.matchaggregator.service.MatchService;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class MatchController {

    private final MatchService service;

    public MatchController(MatchService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<MatchDto> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID tournamentId,
            @RequestParam(required = false) MatchStatus status,
            @RequestParam(required = false) MatchStage stage,
            @RequestParam(required = false) Boolean important,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) Integer priorityMin,
            @RequestParam(required = false) Integer priorityMax,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Instant dateFrom,
            @RequestParam(required = false) Instant dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "matchDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        int safeSize = Math.min(size, 100);
        Sort sort = "asc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize, sort);

        MatchFilter filter = new MatchFilter(search, tournamentId, status, stage,
                important, archived, priorityMin, priorityMax, tag, dateFrom, dateTo);

        Page<Match> result = service.findAll(filter, pageable);
        return PageResponse.from(result.map(MatchDto::from));
    }

    @GetMapping("/{id}")
    public MatchDto getById(@PathVariable UUID id) {
        return MatchDto.from(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<MatchDto> create(@Valid @RequestBody MatchCreateRequest req) {
        Match m = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(MatchDto.from(m));
    }

    @PutMapping("/{id}")
    public MatchDto update(@PathVariable UUID id, @Valid @RequestBody MatchUpdateRequest req) {
        return MatchDto.from(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        service.archive(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        service.restore(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentDelete(@PathVariable UUID id) {
        service.permanentDelete(id);
        return ResponseEntity.noContent().build();
    }
}
