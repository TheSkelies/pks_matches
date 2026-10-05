package pksmatches.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pksmatches.model.Match;
import pksmatches.model.Tournament;
import pksmatches.model.enums.MatchStatus;
import pksmatches.model.enums.TournamentStage;
import pksmatches.repository.MatchRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MatchServiceTest {

    private MatchService matchService;
    private InMemoryMatchRepository matchRepository;
    private Tournament activeTournament;

    @BeforeEach
    void setUp() {
        matchRepository = new InMemoryMatchRepository();
        matchService = new MatchService(matchRepository);

        activeTournament = new Tournament(
                1,
                "Кубок Мира",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(20)
        );
    }

    @Test
    @DisplayName("Запрет матча команды с самой собой")
    void testSameTeamRejected() {
        Match match = new Match(
                activeTournament,
                "Спартак",
                "спартак",
                LocalDateTime.now().plusDays(1),
                TournamentStage.GROUP
        );
        assertThrows(IllegalArgumentException.class, () -> matchService.addMatch(match));
    }

    @Test
    @DisplayName("Контроль коллизии расписания (< 3 часов)")
    void testScheduleCollisionRejected() {
        LocalDateTime baseTime = LocalDateTime.now().plusDays(2).withHour(18).withMinute(0);

        Match m1 = new Match(activeTournament, "Спартак", "Зенит", baseTime, TournamentStage.GROUP);
        matchService.addMatch(m1);

        // Другой матч со Спартаком через 1.5 часа (менее 180 мин)
        Match m2 = new Match(activeTournament, "Спартак", "Динамо", baseTime.plusMinutes(90), TournamentStage.GROUP);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> matchService.addMatch(m2));
        assertTrue(ex.getMessage().contains("Коллизия расписания"));
    }

    @Test
    @DisplayName("Матч разрешен если интервал >= 3 часов")
    void testScheduleCollisionAllowedAfterThreeHours() {
        LocalDateTime baseTime = LocalDateTime.now().plusDays(2).withHour(12).withMinute(0);

        Match m1 = new Match(activeTournament, "Спартак", "Зенит", baseTime, TournamentStage.GROUP);
        matchService.addMatch(m1);

        // Матч через 3.5 часа (210 мин)
        Match m2 = new Match(activeTournament, "Спартак", "Динамо", baseTime.plusMinutes(210), TournamentStage.GROUP);
        assertDoesNotThrow(() -> matchService.addMatch(m2));
    }

    @Test
    @DisplayName("Запрет изменения счета завершенного или отмененного матча")
    void testScoreModificationInvariants() {
        Match m = new Match(activeTournament, "ЦСКА", "Локомотив", LocalDateTime.now().plusDays(1), TournamentStage.FINAL);
        matchService.addMatch(m);

        // Нельзя менять счет запланированного матча
        assertThrows(IllegalStateException.class, () -> matchService.updateScore(m.getId(), 1, 0));

        // Начинаем матч -> LIVE
        matchService.startLive(m.getId());
        assertDoesNotThrow(() -> matchService.updateScore(m.getId(), 2, 1));

        // Завершаем матч -> FINISHED
        matchService.finish(m.getId());
        assertThrows(IllegalStateException.class, () -> matchService.updateScore(m.getId(), 3, 1));
        assertThrows(IllegalStateException.class, () -> matchService.cancel(m.getId()));
    }

    static class InMemoryMatchRepository implements MatchRepository {
        private final Map<Integer, Match> store = new HashMap<>();
        private int idGen = 1;

        @Override
        public List<Match> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public Optional<Match> findById(int id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Match> findByTournamentId(int tournamentId) {
            return store.values().stream().filter(m -> m.getTournament().getId() == tournamentId).toList();
        }

        @Override
        public List<Match> findByStatus(MatchStatus status) {
            return store.values().stream().filter(m -> m.getStatus() == status).toList();
        }

        @Override
        public Match save(Match match) {
            match.setId(idGen++);
            store.put(match.getId(), match);
            return match;
        }

        @Override
        public void update(Match match) {
            store.put(match.getId(), match);
        }

        @Override
        public void deleteById(int id) {
            store.remove(id);
        }
    }
}
