package pksmatches.service;

import pksmatches.model.Match;
import pksmatches.model.Tournament;
import pksmatches.model.enums.MatchStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MatchService {

    private final List<Match> matches = new ArrayList<>();

    public void addMatch(Match match) {
        matches.add(match);
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
}