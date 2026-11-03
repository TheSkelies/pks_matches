package ru.mirea.matchaggregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.matchaggregator.domain.AuditEvent;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
}
