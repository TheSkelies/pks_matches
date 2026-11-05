package ru.mirea.matchaggregator.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.mirea.matchaggregator.domain.Match;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID>, JpaSpecificationExecutor<Match> {
    
    @EntityGraph(attributePaths = {"tournament", "tags"})
    List<Match> findAll(); 

    // ДОБАВЛЯЕМ ЭТОТ МЕТОД:
    // Он говорит Hibernate: "Когда ищешь один матч по ID, сразу подгрузи турнир и теги"
    @Override
    @EntityGraph(attributePaths = {"tournament", "tags"})
    Optional<Match> findById(UUID id);
}
