package pksmatches;

import pksmatches.model.Match;
import pksmatches.model.Tournament;
import pksmatches.model.enums.TournamentStage;
import pksmatches.service.MatchService;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

        Tournament championsLeague = new Tournament(
                1, "UEFA Champions League",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2026, 5, 30)
        );

        Match match = new Match(
                championsLeague,
                "Real Madrid",
                "Barcelona",
                LocalDateTime.of(2025, 10, 15, 21, 0),
                TournamentStage.GROUP
        );

        match.setScore1(2);
        match.setScore2(1);
        match.startLive();

        System.out.println(match);
        System.out.println("Турнир: " + match.getTournament().getName());
        System.out.println("Статус live: " + match.isLive());

        MatchService service = new MatchService();
        service.addMatch(match);

        System.out.println("Live-матчи: " + service.getLiveMatches());
        System.out.println("Матчи турнира: " +
                service.getMatchesByTournament(championsLeague));
    }
}
