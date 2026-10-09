package com.utility.software.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utility.software.dto.MeterData;
import com.utility.software.dto.MeterItem;
import com.utility.software.dto.MeterRequest;
import com.utility.software.dto.MeterResponse;
import com.utility.software.entity.Meter;
import com.utility.software.enums.MeterStatus;
import com.utility.software.enums.OperationType;
import com.utility.software.exception.DeviceCommunicationException;
import com.utility.software.exception.ResourceNotFoundException;
import com.utility.software.mqtt.MqttGateway;
import com.utility.software.repository.MeterRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RequestService {

    private final MeterRepository meterRepo;
    private final HistoryService historyService;
    private final ObjectProvider<MqttGateway> mqttProvider;
    private final ObjectMapper mapper;

    public RequestService(MeterRepository meterRepo,
                          HistoryService historyService,
                          ObjectProvider<MqttGateway> mqttProvider,
                          ObjectMapper mapper) {
        this.meterRepo = meterRepo;
        this.historyService = historyService;
        this.mqttProvider = mqttProvider;
        this.mapper = mapper;
    }

    /** "Send Request" button. */
    @Transactional
    public MeterResponse process(MeterRequest req) {

        Meter meter = meterRepo.findById(req.meterId()).orElse(null);

        if (meter == null) {
            historyService.log(req, null, false);
            throw new ResourceNotFoundException("Meter not found with id: " + req.meterId());
        }

        MqttGateway gateway = mqttProvider.getIfAvailable();
        MeterResponse response;

        if (gateway == null) {
            response = processFromDatabase(meter, req);
        } else {
            try {
                response = processViaDevice(gateway, meter, req);
            } catch (DeviceCommunicationException e) {
                historyService.log(req, null, false);
                throw e;
            }
        }

        historyService.log(req, response, response.ipStatus());
        return response;
    }

    // ------------------------------------------------------------------
    // Mode 1: mqtt.enabled=false  -> answer from the database
    // ------------------------------------------------------------------
    private MeterResponse processFromDatabase(Meter meter, MeterRequest req) {
        if (meter.getStatus() != MeterStatus.ONLINE) {
            return new MeterResponse(req.messageId(), req.opType(), req.ipAddress(), false, List.of());
        }

        if (req.opType() == OperationType.WRITE) {
            applyWrite(meter, req);
        }
        meter.setLastUpdated(LocalDateTime.now());
        meterRepo.save(meter);

        return new MeterResponse(req.messageId(), req.opType(), req.ipAddress(), true,
                List.of(toItem(meter)));
    }

    // ------------------------------------------------------------------
    // Mode 2: mqtt.enabled=true  -> ask the device, use its reply
    // ------------------------------------------------------------------
    private MeterResponse processViaDevice(MqttGateway gateway, Meter meter, MeterRequest req) {
        Optional<String> raw = gateway.request(req.messageId(), toJson(req));

        // no reply in time -> device is treated as offline
        if (raw.isEmpty()) {
            meter.setStatus(MeterStatus.OFFLINE);
            meter.setLastUpdated(LocalDateTime.now());
            meterRepo.save(meter);
            return new MeterResponse(req.messageId(), req.opType(), req.ipAddress(), false, List.of());
        }

        MeterResponse res = parse(raw.get());
        applyDeviceReply(meter, res);
        return res;
    }

    /** Stores what the device reported, so the Real Time Data View shows it. */
    private void applyDeviceReply(Meter meter, MeterResponse res) {
        List<MeterItem> items = res.meters() == null ? List.of() : res.meters();
        boolean hasData = !items.isEmpty();

        meter.setStatus(res.ipStatus() || hasData ? MeterStatus.ONLINE : MeterStatus.OFFLINE);

        if (hasData) {
            MeterItem item = items.stream()
                    .filter(i -> meter.getId().equals(i.meterId()))
                    .findFirst()
                    .orElse(items.get(0));

            if (item.meterTypeId() != null) meter.setMeterTypeId(item.meterTypeId());

            MeterData d = item.data();
            if (d != null) {
                if (d.reading() != null) meter.setReading(d.reading());
                if (d.voltage() != null) meter.setVoltage(d.voltage());
                if (d.current() != null) meter.setCurrent(d.current());
                if (d.power() != null) meter.setPower(d.power());
            }
        }
        meter.setLastUpdated(LocalDateTime.now());
        meterRepo.save(meter);
    }

    // ------------------------------------------------------------------

    private MeterItem toItem(Meter m) {
        return new MeterItem(m.getId(), m.getMeterTypeId(),
                new MeterData(m.getReading(), m.getVoltage(), m.getCurrent(), m.getPower()));
    }

    private void applyWrite(Meter meter, MeterRequest req) {
        meter.setIpAddress(req.ipAddress());
        if (req.meterTypeId() != null) {
            meter.setMeterTypeId(req.meterTypeId());
        }
        MeterData data = req.data();
        if (data != null) {
            if (data.reading() != null) meter.setReading(data.reading());
            if (data.voltage() != null) meter.setVoltage(data.voltage());
            if (data.current() != null) meter.setCurrent(data.current());
            if (data.power() != null) meter.setPower(data.power());
        }
    }

    private String toJson(MeterRequest req) {
        try {
            return mapper.writeValueAsString(req);
        } catch (JsonProcessingException e) {
            throw new DeviceCommunicationException("Could not build the request: " + e.getMessage());
        }
    }

    private MeterResponse parse(String raw) {
        try {
            return mapper.readValue(raw, MeterResponse.class);
        } catch (JsonProcessingException e) {
            String shown = raw.length() > 200 ? raw.substring(0, 200) + "..." : raw;
            throw new DeviceCommunicationException("Device sent a reply that cannot be read: " + shown);
        }
    }
}
