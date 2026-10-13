package pksmatches.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import pksmatches.model.Match;
import pksmatches.model.enums.MatchStatus;
import pksmatches.repository.MatchRepository;
import pksmatches.repository.TournamentRepository;
import pksmatches.repository.impl.MatchRepositoryJdbc;
import pksmatches.repository.impl.TournamentRepositoryJdbc;
import pksmatches.service.MatchService;
import pksmatches.service.TournamentService;
import pksmatches.util.AlertUtil;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsController {

    @FXML private Label totalMatchesLabel;
    @FXML private Label finishedMatchesLabel;
    @FXML private Label liveMatchesLabel;
    @FXML private Label cancelledMatchesLabel;
    @FXML private Label totalTournamentsLabel;
    @FXML private Label avgScoreLabel;
    @FXML private Label topTeamLabel;
    @FXML private Label recentMatchesLabel;
    @FXML private Label scheduledMatchesLabel;

    private MatchService matchService;
    private TournamentService tournamentService;

    @FXML
    private void initialize() {
        MatchRepository matchRepository = new MatchRepositoryJdbc();
        TournamentRepository tournamentRepository = new TournamentRepositoryJdbc();
        matchService = new MatchService(matchRepository);
        tournamentService = new TournamentService(tournamentRepository);
        refresh();
    }

    @FXML
    private void onRefreshClick() {
        refresh();
    }

    private void refresh() {
        try {
            List<Match> matches = matchService.getAllMatches();
            int tournamentsCount = tournamentService.getAll().size();

            totalMatchesLabel.setText(String.valueOf(matches.size()));
            finishedMatchesLabel.setText(String.valueOf(countByStatus(matches, MatchStatus.FINISHED)));
            liveMatchesLabel.setText(String.valueOf(countByStatus(matches, MatchStatus.LIVE)));
            cancelledMatchesLabel.setText(String.valueOf(countByStatus(matches, MatchStatus.CANCELLED)));
            scheduledMatchesLabel.setText(String.valueOf(countByStatus(matches, MatchStatus.SCHEDULED)));
            totalTournamentsLabel.setText(String.valueOf(tournamentsCount));

            avgScoreLabel.setText(calculateAverageScore(matches));
            topTeamLabel.setText(findTopTeam(matches));
            recentMatchesLabel.setText(String.valueOf(countRecentMatches(matches, 7)));

        } catch (RuntimeException e) {
            AlertUtil.error("Ошибка расчёта статистики", e.getMessage());
        }
    }

    private long countByStatus(List<Match> matches, MatchStatus status) {
        return matches.stream().filter(m -> m.getStatus() == status).count();
    }

    private String calculateAverageScore(List<Match> matches) {
        List<Match> finished = matches.stream()
                .filter(m -> m.getStatus() == MatchStatus.FINISHED)
                .toList();
        if (finished.isEmpty()) return "—";

        double total1 = 0;
        double total2 = 0;
        for (Match m : finished) {
            total1 += m.getScore1();
            total2 += m.getScore2();
        }
        double avg1 = total1 / finished.size();
        double avg2 = total2 / finished.size();
        return String.format("%.2f : %.2f", avg1, avg2);
    }

    private String findTopTeam(List<Match> matches) {
        Map<String, Integer> wins = new HashMap<>();
        for (Match m : matches) {
            if (m.getStatus() != MatchStatus.FINISHED) continue;
            if (m.getScore1() > m.getScore2()) {
                wins.merge(m.getTeam1(), 1, Integer::sum);
            } else if (m.getScore2() > m.getScore1()) {
                wins.merge(m.getTeam2(), 1, Integer::sum);
            }
        }
        if (wins.isEmpty()) return "—";

        Map.Entry<String, Integer> best = wins.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        if (best == null) return "—";
        return best.getKey() + " (" + best.getValue() + " побед)";
    }

    private long countRecentMatches(List<Match> matches, int days) {
        LocalDate threshold = LocalDate.now().minusDays(days);
        return matches.stream()
                .filter(m -> !m.getMatchDate().toLocalDate().isBefore(threshold))
                .count();
    }
}
