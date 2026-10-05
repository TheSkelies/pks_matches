package pksmatches.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pksmatches.model.Tournament;
import pksmatches.repository.TournamentRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TournamentServiceTest {

    private TournamentService tournamentService;
    private InMemoryTournamentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTournamentRepository();
        tournamentService = new TournamentService(repository);
    }

    @Test
    @DisplayName("Валидация порядка дат турнира (endDate >= startDate)")
    void testInvalidDatesRejected() {
        Tournament t = new Tournament("Тест", LocalDate.of(2025, 6, 10), LocalDate.of(2025, 6, 1));
        assertThrows(IllegalArgumentException.class, () -> tournamentService.add(t));
    }

    @Test
    @DisplayName("Запрет дублирующегося названия турнира")
    void testDuplicateNameRejected() {
        Tournament t1 = new Tournament("Кубок", LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 10));
        tournamentService.add(t1);

        Tournament t2 = new Tournament("кубок", LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 10));
        assertThrows(IllegalArgumentException.class, () -> tournamentService.add(t2));
    }

    static class InMemoryTournamentRepository implements TournamentRepository {
        private final Map<Integer, Tournament> store = new HashMap<>();
        private int idGen = 1;

        @Override
        public List<Tournament> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public Optional<Tournament> findById(int id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Tournament save(Tournament tournament) {
            tournament.setId(idGen++);
            store.put(tournament.getId(), tournament);
            return tournament;
        }

        @Override
        public void update(Tournament tournament) {
            store.put(tournament.getId(), tournament);
        }

        @Override
        public void deleteById(int id) {
            store.remove(id);
        }
    }
}
