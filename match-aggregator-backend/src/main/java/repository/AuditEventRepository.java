package ru.mirea.matchaggregator.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.matchaggregator.domain.AuditEvent;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    
    // Говорим Hibernate: "когда достаешь аудит, сразу подгрузи связанного пользователя"
    @Override
    @EntityGraph(attributePaths = {"user"})
    org.springframework.data.domain.Page<AuditEvent> findAll(org.springframework.data.domain.Pageable pageable);
}
