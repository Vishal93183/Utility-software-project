package com.utility.software.dto;

import com.utility.software.enums.OperationType;

import java.util.List;

public record MeterResponse(
        String messageId,
        OperationType opType,
        String ipAddress,
        boolean ipStatus,
        List<MeterItem> meters
) {
}
