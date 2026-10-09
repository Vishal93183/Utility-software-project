package com.utility.software.controller;

import com.utility.software.dto.MeterInput;
import com.utility.software.dto.MeterView;
import com.utility.software.service.MeterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meters")
public class MeterController {

    private final MeterService service;

    public MeterController(MeterService service) {
        this.service = service;
    }

    /** Real Time Data View table */
    @GetMapping
    public List<MeterView> getAll() {
        return service.getAll();
    }

    /** Add Meter */
    @PostMapping
    public ResponseEntity<MeterView> create(@Valid @RequestBody MeterInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(input));
    }

    /** Edit Meter */
    @PutMapping("/{id}")
    public MeterView update(@PathVariable Long id, @Valid @RequestBody MeterInput input) {
        return service.update(id, input);
    }

    /** Delete Meter */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Optional demo endpoint (simulated values). Not used by the UI. */
    @PostMapping("/refresh")
    public List<MeterView> refresh() {
        return service.refresh();
    }
}
