package pksmatches.service;

import pksmatches.model.Match;
import pksmatches.model.Tournament;
import pksmatches.model.enums.MatchStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatchService {

    private final List<Match> matches = new ArrayList<>();
    private int nextId = 1;

    public void addMatch(Match match) {
        if (match == null) {
            throw new IllegalArgumentException("Матч не может быть null");
        }
        if (match.getId() <= 0) {
            match.setId(nextId++);
        } else if (match.getId() >= nextId) {
            nextId = match.getId() + 1;
        }
        matches.add(match);
    }

    public List<Match> getAllMatches() {
        return new ArrayList<>(matches);
    }

    public Optional<Match> findById(int id) {
        return matches.stream().filter(m -> m.getId() == id).findFirst();
    }

    public Match getById(int id) {
        return findById(id).orElseThrow(
                () -> new IllegalArgumentException("Матч с ID=" + id + " не найден"));
    }

    public List<Match> getMatchesByTournament(Tournament tournament) {
        return matches.stream()
                .filter(m -> m.getTournament().equals(tournament))
                .collect(Collectors.toList());
    }

    public List<Match> getLiveMatches() {
        return matches.stream()
                .filter(m -> m.getStatus() == MatchStatus.LIVE)
                .collect(Collectors.toList());
    }

    public List<Match> getMatchesByStatus(MatchStatus status) {
        return matches.stream()
                .filter(m -> m.getStatus() == status)
                .collect(Collectors.toList());
    }

    public void startLive(int id) {
        Match m = getById(id);
        m.startLive();
    }

    public void finish(int id) {
        Match m = getById(id);
        m.finish();
    }

    public void cancel(int id) {
        Match m = getById(id);
        if (m.getStatus() == MatchStatus.FINISHED) {
            throw new IllegalStateException("Нельзя отменить завершённый матч");
        }
        m.setStatus(MatchStatus.CANCELLED);
    }

    public void updateScore(int id, int score1, int score2) {
        Match m = getById(id);
        if (m.getStatus() == MatchStatus.CANCELLED) {
            throw new IllegalStateException("Нельзя менять счёт отменённого матча");
        }
        m.setScore1(score1);
        m.setScore2(score2);
    }

    public boolean removeMatch(int id) {
        return matches.removeIf(m -> m.getId() == id);
    }
}
