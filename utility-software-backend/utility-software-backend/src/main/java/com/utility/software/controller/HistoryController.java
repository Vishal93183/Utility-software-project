package com.utility.software.controller;

import com.utility.software.dto.HistoryItem;
import com.utility.software.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService service;

    public HistoryController(HistoryService service) {
        this.service = service;
    }

    /** Request History panel (latest 10) */
    @GetMapping
    public List<HistoryItem> latest() {
        return service.latest();
    }

    /** Click on a history row -> load request / response again */
    @GetMapping("/{id}")
    public HistoryItem get(@PathVariable Long id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearAll() {
        service.clearAll();
        return ResponseEntity.noContent().build();
    }
}
