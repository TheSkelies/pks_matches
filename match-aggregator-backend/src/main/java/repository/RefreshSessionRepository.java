package ru.mirea.matchaggregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.matchaggregator.domain.RefreshSession;

import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, UUID> {
    Optional<RefreshSession> findByTokenHash(String tokenHash);
}
