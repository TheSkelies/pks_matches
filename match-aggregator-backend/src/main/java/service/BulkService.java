package ru.mirea.matchaggregator.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.matchaggregator.domain.Match;
import ru.mirea.matchaggregator.domain.Tag;
import ru.mirea.matchaggregator.dto.match.BulkRequest;
import ru.mirea.matchaggregator.dto.match.BulkResponse;
import ru.mirea.matchaggregator.repository.MatchRepository;
import ru.mirea.matchaggregator.repository.TagRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BulkService {

    private final MatchRepository matchRepository;
    private final TagRepository tagRepository;

    public BulkService(MatchRepository matchRepository, TagRepository tagRepository) {
        this.matchRepository = matchRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public BulkResponse execute(BulkRequest req) {
        List<BulkResponse.BulkError> errors = new ArrayList<>();
        int success = 0;

        for (UUID id : req.ids()) {
            try {
                Match match = matchRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Не найдено"));

                switch (req.action()) {
                    case ARCHIVE -> match.setArchived(true);
                    case RESTORE -> match.setArchived(false);
                    case CHANGE_STATUS -> {
                        if (req.newStatus() == null)
                            throw new IllegalArgumentException("newStatus не указан");
                        match.setStatus(req.newStatus());
                    }
                    case ADD_TAG -> {
                        if (req.tagName() == null || req.tagName().isBlank())
                            throw new IllegalArgumentException("tagName не указан");
                        Tag tag = tagRepository.findByNameIgnoreCase(req.tagName())
                                .orElseGet(() -> {
                                    Tag t = new Tag();
                                    t.setName(req.tagName());
                                    return tagRepository.save(t);
                                });
                        match.getTags().add(tag);
                    }
                    case REMOVE_TAG -> {
                        if (req.tagName() == null)
                            throw new IllegalArgumentException("tagName не указан");
                        match.getTags().removeIf(t -> t.getName().equalsIgnoreCase(req.tagName()));
                    }
                }
                matchRepository.save(match);
                success++;
            } catch (Exception e) {
                errors.add(new BulkResponse.BulkError(id, e.getMessage()));
            }
        }
        return new BulkResponse(success, errors.size(), errors);
    }
}
