package com.utility.software.dto;

import com.utility.software.entity.Meter;
import com.utility.software.enums.MeterStatus;

import java.time.LocalDateTime;

/** Row of "Real Time Data View" table (also used to fill the Edit Meter dialog). */
public record MeterView(
        Long id,
        String meterCode,
        Integer meterTypeId,
        String ipAddress,
        MeterStatus status,
        Double voltage,
        Double current,
        Double power,
        Double reading,
        LocalDateTime lastUpdated
) {
    public static MeterView from(Meter m) {
        return new MeterView(m.getId(), m.getMeterCode(), m.getMeterTypeId(), m.getIpAddress(),
                m.getStatus(), m.getVoltage(), m.getCurrent(), m.getPower(), m.getReading(),
                m.getLastUpdated());
    }
}
