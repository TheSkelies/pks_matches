package ru.mirea.matchaggregator.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mirea.matchaggregator.dto.match.CsvPreviewResponse;
import ru.mirea.matchaggregator.dto.match.MatchFilter;
import ru.mirea.matchaggregator.service.CsvService;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ImportExportController {

    private final CsvService csvService;

    public ImportExportController(CsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping(value = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CsvPreviewResponse preview(@RequestParam("file") MultipartFile file) {
        return csvService.preview(file);
    }

    @PostMapping(value = "/import/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> apply(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean updateExisting) {
        int count = csvService.apply(file, updateExisting);
        return ResponseEntity.ok("Обработано записей: " + count);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) UUID tournamentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean archived) {

        MatchFilter filter = new MatchFilter(null, tournamentId,
                status != null ? ru.mirea.matchaggregator.domain.enums.MatchStatus.valueOf(status) : null,
                null, null, archived, null, null, null, null, null);

        byte[] csv = csvService.exportCsv(filter);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=matches.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
