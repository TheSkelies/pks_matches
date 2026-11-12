package ru.mirea.matchaggregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.matchaggregator.domain.Tournament;
import java.util.UUID;

public interface TournamentRepository extends JpaRepository<Tournament, UUID> {
    boolean existsByNameIgnoreCase(String name);
}
