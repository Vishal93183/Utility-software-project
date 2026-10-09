package com.utility.software.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utility.software.dto.HistoryItem;
import com.utility.software.dto.MeterRequest;
import com.utility.software.dto.MeterResponse;
import com.utility.software.entity.RequestHistory;
import com.utility.software.enums.OperationType;
import com.utility.software.exception.ResourceNotFoundException;
import com.utility.software.repository.RequestHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoryService {

    private final RequestHistoryRepository repo;
    private final ObjectMapper mapper;

    public HistoryService(RequestHistoryRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    /**
     * Runs in its own transaction so the history row is kept even when
     * RequestService throws (e.g. 404 for an unknown meter).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(MeterRequest req, MeterResponse res, boolean success) {
        RequestHistory h = new RequestHistory();
        h.setName(req.opType() == OperationType.READ ? "Get Meter Data" : "Update Config");
        h.setOpType(req.opType());
        h.setMessageId(req.messageId());
        h.setMeterId(req.meterId());
        h.setIpAddress(req.ipAddress());
        h.setSuccess(success);
        h.setRequestJson(toJson(req));
        h.setResponseJson(res == null ? null : toJson(res));
        repo.save(h);
    }

    /** Last 10 requests (newest first). */
    @Transactional(readOnly = true)
    public List<HistoryItem> latest() {
        return repo.findTop10ByOrderByCreatedAtDesc().stream().map(HistoryItem::from).toList();
    }

    @Transactional(readOnly = true)
    public HistoryItem get(Long id) {
        return repo.findById(id).map(HistoryItem::from)
                .orElseThrow(() -> new ResourceNotFoundException("History not found: " + id));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("History not found: " + id);
        repo.deleteById(id);
    }

    @Transactional
    public void clearAll() {
        repo.deleteAll();
    }

    private String toJson(Object o) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
