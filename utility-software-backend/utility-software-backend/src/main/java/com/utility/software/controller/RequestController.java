package com.utility.software.controller;

import com.utility.software.dto.MeterRequest;
import com.utility.software.dto.MeterResponse;
import com.utility.software.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/request")
public class RequestController {

    private final RequestService service;

    public RequestController(RequestService service) {
        this.service = service;
    }

    @PostMapping("/send")
    public MeterResponse send(@Valid @RequestBody MeterRequest request) {
        return service.process(request);
    }
}
