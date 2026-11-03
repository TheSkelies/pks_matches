package ru.mirea.matchaggregator.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.mirea.matchaggregator.domain.AuditEvent;
import ru.mirea.matchaggregator.domain.UserAccount;
import ru.mirea.matchaggregator.repository.AuditEventRepository;
import ru.mirea.matchaggregator.repository.UserAccountRepository;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditEventRepository auditRepository;
    private final UserAccountRepository userRepository;

    public AuditService(AuditEventRepository auditRepository, UserAccountRepository userRepository) {
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
    }

    public void log(String eventType, String entityType, UUID entityId, String description) {
        AuditEvent event = new AuditEvent();
        event.setEventType(eventType);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setDescription(description);
        event.setCorrelationId(UUID.randomUUID().toString());

        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            userRepository.findByEmailIgnoreCase(email).ifPresent(event::setUser);
        } catch (Exception ignored) {
        }

        auditRepository.save(event);
    }
}
