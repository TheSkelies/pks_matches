package ru.mirea.matchaggregator.filter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.mirea.matchaggregator.domain.Match;
import ru.mirea.matchaggregator.dto.match.MatchFilter;

import java.util.ArrayList;
import java.util.List;

public class MatchSpecifications {

    public static Specification<Match> byFilter(MatchFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.search() != null && !filter.search().isBlank()) {
                String like = "%" + filter.search().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("team1")), like),
                        cb.like(cb.lower(root.get("team2")), like),
                        cb.like(cb.lower(root.get("tournament").get("name")), like)
                ));
            }
            if (filter.tournamentId() != null) {
                predicates.add(cb.equal(root.get("tournament").get("id"), filter.tournamentId()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.stage() != null) {
                predicates.add(cb.equal(root.get("stage"), filter.stage()));
            }
            if (filter.important() != null) {
                predicates.add(cb.equal(root.get("isImportant"), filter.important()));
            }
            if (filter.archived() != null) {
                predicates.add(cb.equal(root.get("archived"), filter.archived()));
            }
            if (filter.priorityMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("priority"), filter.priorityMin()));
            }
            if (filter.priorityMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("priority"), filter.priorityMax()));
            }
            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("matchDate"), filter.dateFrom()));
            }
            if (filter.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("matchDate"), filter.dateTo()));
            }
            if (filter.tag() != null && !filter.tag().isBlank()) {
                Join<Match, ru.mirea.matchaggregator.domain.Tag> tagJoin = root.join("tags");
                predicates.add(cb.equal(cb.lower(tagJoin.get("name")), filter.tag().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
