package ru.mirea.matchaggregator.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.matchaggregator.domain.Tournament;
import ru.mirea.matchaggregator.error.ConflictException;
import ru.mirea.matchaggregator.error.NotFoundException;
import ru.mirea.matchaggregator.repository.TournamentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TournamentService {

    private final TournamentRepository repository;

    public TournamentService(TournamentRepository repository) {
        this.repository = repository;
    }

    public List<Tournament> findAll() {
        return repository.findAll();
    }

    public Tournament getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Турнир не найден: " + id));
    }

    @Transactional
    public Tournament create(String name, LocalDate start, LocalDate end) {
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Турнир с таким названием уже существует");
        }
        if (end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("Дата окончания не может быть раньше даты начала");
        }
        Tournament t = new Tournament();
        t.setName(name);
        t.setStartDate(start);
        t.setEndDate(end);
        return repository.save(t);
    }

    @Transactional
    public Tournament update(UUID id, String name, LocalDate start, LocalDate end, boolean active) {
        Tournament t = getById(id);
        if (end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("Дата окончания не может быть раньше даты начала");
        }
        t.setName(name);
        t.setStartDate(start);
        t.setEndDate(end);
        t.setActive(active);
        return repository.save(t);
    }
}
