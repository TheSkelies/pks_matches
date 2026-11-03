package ru.mirea.matchaggregator.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.matchaggregator.domain.AuditEvent;
import ru.mirea.matchaggregator.dto.common.PageResponse;
import ru.mirea.matchaggregator.repository.AuditEventRepository;

@RestController
@RequestMapping("/api/admin/audit")
public class AdminAuditController {

    private final AuditEventRepository auditRepository;

    public AdminAuditController(AuditEventRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @GetMapping
    public PageResponse<AuditDto> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        Page<AuditEvent> events = auditRepository.findAll(pageable);
        return PageResponse.from(events.map(AuditDto::from));
    }

    public record AuditDto(
            String id,
            String userEmail,
            String eventType,
            String entityType,
            String entityId,
            String description,
            String correlationId,
            String createdAt
    ) {
        static AuditDto from(AuditEvent e) {
            return new AuditDto(
                    e.getId().toString(),
                    e.getUser() != null ? e.getUser().getEmail() : null,
                    e.getEventType(),
                    e.getEntityType(),
                    e.getEntityId() != null ? e.getEntityId().toString() : null,
                    e.getDescription(),
                    e.getCorrelationId(),
                    e.getCreatedAt().toString()
            );
        }
    }
}
