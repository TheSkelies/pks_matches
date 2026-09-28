package pksmatches.service;

import pksmatches.model.enums.MatchStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Сервис статистики.
 * Собирает 7 показателей на основе данных из других сервисов.
 */
public class StatisticsService {

    private final MatchService matchService;
    private final TournamentService tournamentService;
    private final UserService userService;

    public StatisticsService(MatchService matchService,
                             TournamentService tournamentService,
                             UserService userService) {
        this.matchService = matchService;
        this.tournamentService = tournamentService;
        this.userService = userService;
    }

    /**
     * Возвращает карту "название показателя -> значение".
     * LinkedHashMap сохраняет порядок вставки, чтобы в консоли и Excel
     * показатели шли в нужном порядке.
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new LinkedHashMap<>();

        stats.put("Всего пользователей", userService.getAll().size());
        stats.put("Всего турниров", tournamentService.getAll().size());
        stats.put("Всего матчей", matchService.getAllMatches().size());
        stats.put("Активных матчей (LIVE)", matchService.getMatchesByStatus(MatchStatus.LIVE).size());
        stats.put("Завершённых матчей (FINISHED)", matchService.getMatchesByStatus(MatchStatus.FINISHED).size());
        stats.put("Отменённых матчей (CANCELLED)", matchService.getMatchesByStatus(MatchStatus.CANCELLED).size());
        stats.put("Предстоящих матчей (SCHEDULED)", matchService.getMatchesByStatus(MatchStatus.SCHEDULED).size());

        return stats;
    }

    /**
     * Красивый вывод статистики в консоль.
     */
    public void printStatistics() {
        Map<String, Integer> stats = getStatistics();
        System.out.println();
        System.out.println("=== СТАТИСТИКА ===");
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}