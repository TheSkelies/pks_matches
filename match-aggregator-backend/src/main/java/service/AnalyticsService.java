package ru.mirea.matchaggregator.service;

import org.springframework.stereotype.Service;
import ru.mirea.matchaggregator.domain.Match;
import ru.mirea.matchaggregator.dto.analytics.AnalyticsResponse;
import ru.mirea.matchaggregator.repository.MatchRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final MatchRepository matchRepository;

    public AnalyticsService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public AnalyticsResponse summary() {
        List<Match> all = matchRepository.findAll();

        long total = all.size();
        long active = all.stream().filter(m -> !m.isArchived()).count();
        long archived = all.stream().filter(Match::isArchived).count();
        long important = all.stream().filter(Match::isImportant).count();
        double avgPriority = all.stream().mapToInt(Match::getPriority).average().orElse(0.0);

        Map<String, Long> byTournament = all.stream()
                .collect(Collectors.groupingBy(m -> m.getTournament().getName(), Collectors.counting()));

        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(m -> m.getStatus().name(), Collectors.counting()));

        Map<String, Long> byStage = all.stream()
                .collect(Collectors.groupingBy(m -> m.getStage().name(), Collectors.counting()));

        return new AnalyticsResponse(total, active, archived, important,
                Math.round(avgPriority * 100.0) / 100.0,
                byTournament, byStatus, byStage);
    }
}
