package ru.mirea.matchaggregator.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.mirea.matchaggregator.dto.match.BulkRequest;
import ru.mirea.matchaggregator.dto.match.BulkResponse;
import ru.mirea.matchaggregator.service.BulkService;

@RestController
@RequestMapping("/api/items")
public class BulkController {

    private final BulkService service;

    public BulkController(BulkService service) {
        this.service = service;
    }

    @PostMapping("/bulk")
    public BulkResponse bulk(@Valid @RequestBody BulkRequest req) {
        return service.execute(req);
    }
}
