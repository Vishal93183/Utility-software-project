package com.utility.software.dto;

import com.utility.software.enums.OperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request Builder payload.
 * For WRITE, meterTypeId / data are optional fields that get updated.
 */
public record MeterRequest(
        @NotBlank(message = "Message ID is required") String messageId,
        @NotNull(message = "Operation type is required") OperationType opType,
        @NotNull(message = "Meter ID is required") Long meterId,
        @NotBlank(message = "IP address is required") String ipAddress,
        Integer meterTypeId,
        MeterData data
) {
}
