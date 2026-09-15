package pksmatches.service;

import pksmatches.model.Tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TournamentService {

    private final List<Tournament> tournaments = new ArrayList<>();
    private int nextId = 1;

    public void add(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Турнир не может быть null");
        }
        if (tournament.getId() <= 0) {
            tournament.setId(nextId++);
        } else if (tournament.getId() >= nextId) {
            nextId = tournament.getId() + 1;
        }
        tournaments.add(tournament);
    }

    public List<Tournament> getAll() {
        return new ArrayList<>(tournaments);
    }

    public Optional<Tournament> findById(int id) {
        return tournaments.stream().filter(t -> t.getId() == id).findFirst();
    }

    public Tournament getById(int id) {
        return findById(id).orElseThrow(
                () -> new IllegalArgumentException("Турнир с ID=" + id + " не найден"));
    }

    public boolean remove(int id) {
        return tournaments.removeIf(t -> t.getId() == id);
    }
}
