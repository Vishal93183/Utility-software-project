package com.utility.software.dto;

import com.utility.software.enums.MeterStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Add / Edit meter payload. */
public record MeterInput(
        @NotBlank(message = "Meter code is required")
        @Size(max = 20, message = "Meter code can be at most 20 characters")
        String meterCode,
        Integer meterTypeId,
        String ipAddress,
        MeterStatus status,
        Double voltage,
        Double current,
        Double power,
        Double reading
) {
}
