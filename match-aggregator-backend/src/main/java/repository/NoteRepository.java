package ru.mirea.matchaggregator.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.matchaggregator.domain.Note;
import java.util.List;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {
    
    // Добавляем загрузку автора
    @EntityGraph(attributePaths = {"author"})
    List<Note> findByMatchIdOrderByPinnedDescCreatedAtAsc(UUID matchId);
}
