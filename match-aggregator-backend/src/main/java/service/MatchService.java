package ru.mirea.matchaggregator.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.matchaggregator.domain.Match;
import ru.mirea.matchaggregator.domain.Tag;
import ru.mirea.matchaggregator.domain.Tournament;
import ru.mirea.matchaggregator.dto.match.MatchCreateRequest;
import ru.mirea.matchaggregator.dto.match.MatchFilter;
import ru.mirea.matchaggregator.dto.match.MatchUpdateRequest;
import ru.mirea.matchaggregator.error.ConflictException;
import ru.mirea.matchaggregator.error.NotFoundException;
import ru.mirea.matchaggregator.filter.MatchSpecifications;
import ru.mirea.matchaggregator.repository.MatchRepository;
import ru.mirea.matchaggregator.repository.TagRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final TagRepository tagRepository;
    private final TournamentService tournamentService;
    private final AuditService auditService;

    public MatchService(MatchRepository matchRepository,
                        TagRepository tagRepository,
                        TournamentService tournamentService,
                        AuditService auditService) {
        this.matchRepository = matchRepository;
        this.tagRepository = tagRepository;
        this.tournamentService = tournamentService;
        this.auditService = auditService;
    }

    public Page<Match> findAll(MatchFilter filter, Pageable pageable) {
        Specification<Match> spec = MatchSpecifications.byFilter(filter);
        return matchRepository.findAll(spec, pageable);
    }

    public Match getById(UUID id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Матч не найден: " + id));
    }

    @Transactional
    public Match create(MatchCreateRequest req) {
        if (req.team1().equalsIgnoreCase(req.team2())) {
            throw new IllegalArgumentException("Команда не может играть сама с собой");
        }
        Tournament tournament = tournamentService.getById(req.tournamentId());

        Match m = new Match();
        m.setExternalCode(req.externalCode());
        m.setTournament(tournament);
        m.setTeam1(req.team1());
        m.setTeam2(req.team2());
        m.setMatchDate(req.matchDate());
        m.setStatus(req.status());
        m.setStage(req.stage());
        m.setScore1(req.score1());
        m.setScore2(req.score2());
        m.setPriority(req.priority());
        m.setImportant(req.isImportant());

        if (req.tagIds() != null && !req.tagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(req.tagIds()));
            m.setTags(tags);
        }

        Match saved = matchRepository.save(m);
        auditService.log("CREATE", "Match", saved.getId(),
                "Создан матч " + saved.getTeam1() + " — " + saved.getTeam2());
        return saved;
    }

    @Transactional
    public Match update(UUID id, MatchUpdateRequest req) {
        Match m = getById(id);

        if (m.getVersion() != req.version()) {
            throw new ConflictException("Версия записи устарела. Обновите данные и попробуйте снова.");
        }
        if (req.team1().equalsIgnoreCase(req.team2())) {
            throw new IllegalArgumentException("Команда не может играть сама с собой");
        }

        Tournament tournament = tournamentService.getById(req.tournamentId());

        m.setExternalCode(req.externalCode());
        m.setTournament(tournament);
        m.setTeam1(req.team1());
        m.setTeam2(req.team2());
        m.setMatchDate(req.matchDate());
        m.setStatus(req.status());
        m.setStage(req.stage());
        m.setScore1(req.score1());
        m.setScore2(req.score2());
        m.setPriority(req.priority());
        m.setImportant(req.isImportant());

        if (req.tagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(req.tagIds()));
            m.setTags(tags);
        }

        Match saved = matchRepository.save(m);
        auditService.log("UPDATE", "Match", saved.getId(),
                "Обновлён матч " + saved.getTeam1() + " — " + saved.getTeam2());
        return saved;
    }

    @Transactional
    public void archive(UUID id) {
        Match m = getById(id);
        m.setArchived(true);
        matchRepository.save(m);
        auditService.log("ARCHIVE", "Match", id,
                "Матч " + m.getTeam1() + " — " + m.getTeam2() + " перемещён в архив");
    }

    @Transactional
    public void restore(UUID id) {
        Match m = getById(id);
        m.setArchived(false);
        matchRepository.save(m);
        auditService.log("RESTORE", "Match", id,
                "Матч " + m.getTeam1() + " — " + m.getTeam2() + " восстановлен из архива");
    }

    @Transactional
    public void permanentDelete(UUID id) {
        Match m = getById(id);
        matchRepository.deleteById(id);
        auditService.log("PERMANENT_DELETE", "Match", id,
                "Матч " + m.getTeam1() + " — " + m.getTeam2() + " окончательно удалён");
    }
}
