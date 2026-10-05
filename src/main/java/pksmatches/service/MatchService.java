package pksmatches.service;

import pksmatches.model.Match;
import pksmatches.model.Tournament;
import pksmatches.model.enums.MatchStatus;
import pksmatches.repository.MatchRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MatchService {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public void addMatch(Match match) {
        if (match == null) {
            throw new IllegalArgumentException("Матч не может быть null");
        }
        Tournament tournament = match.getTournament();
        if (tournament == null) {
            throw new IllegalArgumentException("Турнир обязателен для создания матча");
        }
        if (match.getTeam1() == null || match.getTeam1().isBlank() ||
            match.getTeam2() == null || match.getTeam2().isBlank()) {
            throw new IllegalArgumentException("Названия команд не могут быть пустыми");
        }
        if (match.getTeam1().trim().equalsIgnoreCase(match.getTeam2().trim())) {
            throw new IllegalArgumentException("Команда не может играть сама с собой (" + match.getTeam1() + ")");
        }
        if (match.getMatchDate() == null) {
            throw new IllegalArgumentException("Дата и время матча обязательны");
        }

        LocalDate matchDate = match.getMatchDate().toLocalDate();

        if (matchDate.isBefore(tournament.getStartDate()) ||
            (tournament.getEndDate() != null && matchDate.isAfter(tournament.getEndDate()))) {
            throw new IllegalArgumentException("Дата матча (" + matchDate + ") должна быть в пределах турнира ("
                    + tournament.getStartDate() + " — " + tournament.getEndDate() + ")");
        }

        if (tournament.getEndDate() != null && tournament.getEndDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Нельзя добавить матч: турнир уже завершился (" + tournament.getEndDate() + ")");
        }

        checkScheduleCollision(match);

        matchRepository.save(match);
    }

    private void checkScheduleCollision(Match match) {
        List<Match> all = matchRepository.findAll();
        for (Match other : all) {
            if (other.getStatus() == MatchStatus.CANCELLED) continue;
            if (other.getId() == match.getId()) continue;

            boolean hasTeam1 = match.getTeam1().equalsIgnoreCase(other.getTeam1()) ||
                               match.getTeam1().equalsIgnoreCase(other.getTeam2());
            boolean hasTeam2 = match.getTeam2().equalsIgnoreCase(other.getTeam1()) ||
                               match.getTeam2().equalsIgnoreCase(other.getTeam2());

            if (hasTeam1 || hasTeam2) {
                long minutes = Math.abs(Duration.between(match.getMatchDate(), other.getMatchDate()).toMinutes());
                if (minutes < 180) {
                    String teamName = hasTeam1 ? match.getTeam1() : match.getTeam2();
                    throw new IllegalStateException("Коллизия расписания: команда \"" + teamName +
                            "\" уже участвует в матче #" + other.getId() + " (" +
                            other.getMatchDate().format(DT_FORMAT) + "). Интервал между играми команды должен быть не менее 3 часов");
                }
            }
        }
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public Optional<Match> findById(int id) {
        return matchRepository.findById(id);
    }

    public Match getById(int id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("Матч с ID=" + id + " не найден"));
    }

    public List<Match> getMatchesByTournament(Tournament tournament) {
        if (tournament == null) return List.of();
        return matchRepository.findByTournamentId(tournament.getId());
    }

    public List<Match> getLiveMatches() {
        return matchRepository.findByStatus(MatchStatus.LIVE);
    }

    public List<Match> getMatchesByStatus(MatchStatus status) {
        return matchRepository.findByStatus(status);
    }

    public void startLive(int id) {
        Match m = getById(id);
        if (m.getStatus() == MatchStatus.LIVE) {
            throw new IllegalStateException("Матч уже идёт в прямом эфире");
        }
        if (m.getStatus() == MatchStatus.FINISHED) {
            throw new IllegalStateException("Нельзя начать уже завершённый матч");
        }
        if (m.getStatus() == MatchStatus.CANCELLED) {
            throw new IllegalStateException("Нельзя начать отменённый матч");
        }
        m.startLive();
        matchRepository.update(m);
    }

    public void finish(int id) {
        Match m = getById(id);
        if (m.getStatus() == MatchStatus.FINISHED) {
            throw new IllegalStateException("Матч уже завершён");
        }
        if (m.getStatus() == MatchStatus.CANCELLED) {
            throw new IllegalStateException("Нельзя завершить отменённый матч");
        }
        m.finish();
        matchRepository.update(m);
    }

    public void cancel(int id) {
        Match m = getById(id);
        if (m.getStatus() == MatchStatus.FINISHED) {
            throw new IllegalStateException("Нельзя отменить завершённый матч");
        }
        if (m.getStatus() == MatchStatus.CANCELLED) {
            throw new IllegalStateException("Матч уже отменён");
        }
        m.setStatus(MatchStatus.CANCELLED);
        matchRepository.update(m);
    }

    public void updateScore(int id, int score1, int score2) {
        if (score1 < 0 || score2 < 0) {
            throw new IllegalArgumentException("Счёт матча не может быть отрицательным");
        }
        Match m = getById(id);
        if (m.getStatus() == MatchStatus.FINISHED) {
            throw new IllegalStateException("Нельзя менять счёт завершённого матча");
        }
        if (m.getStatus() == MatchStatus.CANCELLED) {
            throw new IllegalStateException("Нельзя менять счёт отменённого матча");
        }
        if (m.getStatus() == MatchStatus.SCHEDULED) {
            throw new IllegalStateException("Нельзя менять счёт запланированного матча. Сначала начните матч");
        }
        m.setScore1(score1);
        m.setScore2(score2);
        matchRepository.update(m);
    }

    public boolean removeMatch(int id) {
        if (matchRepository.findById(id).isEmpty()) return false;
        matchRepository.deleteById(id);
        return true;
    }
}
