package ru.mirea.matchaggregator.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mirea.matchaggregator.domain.Match;
import ru.mirea.matchaggregator.domain.Tournament;
import ru.mirea.matchaggregator.domain.enums.MatchStage;
import ru.mirea.matchaggregator.domain.enums.MatchStatus;
import ru.mirea.matchaggregator.dto.match.CsvPreviewResponse;
import ru.mirea.matchaggregator.dto.match.MatchFilter;
import ru.mirea.matchaggregator.repository.MatchRepository;
import ru.mirea.matchaggregator.repository.TournamentRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CsvService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_INSTANT;
    private static final String[] HEADERS = {
            "externalCode", "tournamentName", "team1", "team2", "matchDate",
            "status", "stage", "score1", "score2", "priority", "important"
    };

    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final MatchService matchService;

    public CsvService(MatchRepository matchRepository,
                      TournamentRepository tournamentRepository,
                      MatchService matchService) {
        this.matchRepository = matchRepository;
        this.tournamentRepository = tournamentRepository;
        this.matchService = matchService;
    }

    public CsvPreviewResponse preview(MultipartFile file) {
        List<CsvPreviewResponse.RowError> errors = new ArrayList<>();
        List<CsvPreviewResponse.MatchCsvRow> valid = new ArrayList<>();
        int total = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader(HEADERS)
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                total++;
                int rowNum = (int) record.getRecordNumber();
                try {
                    CsvPreviewResponse.MatchCsvRow row = parseRow(record);
                    validateRow(row);
                    valid.add(row);
                } catch (Exception e) {
                    errors.add(new CsvPreviewResponse.RowError(rowNum, e.getMessage()));
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Не удалось прочитать CSV: " + e.getMessage());
        }

        return new CsvPreviewResponse(total, valid.size(), errors.size(), errors, valid);
    }

    @Transactional
    public int apply(MultipartFile file, boolean updateExisting) {
        CsvPreviewResponse preview = preview(file);

        if (!preview.errors().isEmpty()) {
            throw new IllegalArgumentException(
                    "CSV содержит ошибки (" + preview.errors().size() + "). Исправьте и повторите.");
        }

        int count = 0;
        for (CsvPreviewResponse.MatchCsvRow row : preview.validData()) {
            Tournament tournament = tournamentRepository.findAll().stream()
                    .filter(t -> t.getName().equalsIgnoreCase(row.tournamentName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Турнир не найден: " + row.tournamentName()));

            Optional<Match> existing = row.externalCode() == null ? Optional.empty()
                    : matchRepository.findAll().stream()
                    .filter(m -> row.externalCode().equals(m.getExternalCode()))
                    .findFirst();

            Match match;
            if (existing.isPresent() && updateExisting) {
                match = existing.get();
            } else if (existing.isPresent()) {
                continue; // пропускаем, если только новые
            } else {
                match = new Match();
            }

            match.setExternalCode(row.externalCode());
            match.setTournament(tournament);
            match.setTeam1(row.team1());
            match.setTeam2(row.team2());
            match.setMatchDate(Instant.parse(row.matchDate()));
            match.setStatus(MatchStatus.valueOf(row.status()));
            match.setStage(MatchStage.valueOf(row.stage()));
            match.setScore1(row.score1());
            match.setScore2(row.score2());
            match.setPriority(row.priority());

            matchRepository.save(match);
            count++;
        }
        return count;
    }

    public byte[] exportCsv(MatchFilter filter) {
        try (StringWriter sw = new StringWriter();
             CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder()
                     .setHeader(HEADERS)
                     .build())) {

            matchService.findAll(filter, org.springframework.data.domain.PageRequest.of(0, 10_000))
                    .forEach(m -> {
                        try {
                            printer.printRecord(
                                    m.getExternalCode(),
                                    m.getTournament().getName(),
                                    m.getTeam1(),
                                    m.getTeam2(),
                                    m.getMatchDate().toString(),
                                    m.getStatus().name(),
                                    m.getStage().name(),
                                    m.getScore1(),
                                    m.getScore2(),
                                    m.getPriority(),
                                    m.isImportant()
                            );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            return sw.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка экспорта CSV", e);
        }
    }

    private CsvPreviewResponse.MatchCsvRow parseRow(CSVRecord r) {
        return new CsvPreviewResponse.MatchCsvRow(
                emptyToNull(r.get("externalCode")),
                r.get("tournamentName"),
                r.get("team1"),
                r.get("team2"),
                r.get("matchDate"),
                r.get("status"),
                r.get("stage"),
                parseIntSafe(r.get("score1")),
                parseIntSafe(r.get("score2")),
                parseIntSafe(r.get("priority"))
        );
    }

    private void validateRow(CsvPreviewResponse.MatchCsvRow row) {
        if (row.team1() == null || row.team1().isBlank())
            throw new IllegalArgumentException("team1 обязательно");
        if (row.team2() == null || row.team2().isBlank())
            throw new IllegalArgumentException("team2 обязательно");
        if (row.team1().equalsIgnoreCase(row.team2()))
            throw new IllegalArgumentException("Команды должны различаться");
        if (row.matchDate() == null)
            throw new IllegalArgumentException("matchDate обязательно");
        try {
            Instant.parse(row.matchDate());
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверный формат matchDate (ожидается ISO-8601)");
        }
        try {
            MatchStatus.valueOf(row.status());
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверный status: " + row.status());
        }
        try {
            MatchStage.valueOf(row.stage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверный stage: " + row.stage());
        }
    }

    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private int parseIntSafe(String s) {
        if (s == null || s.isBlank()) return 0;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ожидалось число, получено: " + s);
        }
    }
}
