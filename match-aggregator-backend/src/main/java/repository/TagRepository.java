package ru.mirea.matchaggregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.matchaggregator.domain.Tag;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByNameIgnoreCase(String name);
}
