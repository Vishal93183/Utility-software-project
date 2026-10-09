package com.utility.software.dto;

import com.utility.software.entity.RequestHistory;
import com.utility.software.enums.OperationType;

import java.time.LocalDateTime;

/** Row of "Request History" panel. */
public record HistoryItem(
        Long id,
        String name,
        OperationType opType,
        boolean success,
        LocalDateTime createdAt,
        String requestJson,
        String responseJson
) {
    public static HistoryItem from(RequestHistory h) {
        return new HistoryItem(h.getId(), h.getName(), h.getOpType(), h.isSuccess(),
                h.getCreatedAt(), h.getRequestJson(), h.getResponseJson());
    }
}
