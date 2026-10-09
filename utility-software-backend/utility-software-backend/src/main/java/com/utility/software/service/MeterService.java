package com.utility.software.service;

import com.utility.software.dto.MeterInput;
import com.utility.software.dto.MeterView;
import com.utility.software.entity.Meter;
import com.utility.software.enums.MeterStatus;
import com.utility.software.exception.DuplicateResourceException;
import com.utility.software.exception.ResourceNotFoundException;
import com.utility.software.repository.MeterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MeterService {

    private final MeterRepository repo;

    public MeterService(MeterRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<MeterView> getAll() {
        return repo.findAllByOrderByIdAsc().stream().map(MeterView::from).toList();
    }

    /** Add Meter */
    @Transactional
    public MeterView create(MeterInput in) {
        String code = in.meterCode().trim();
        if (repo.existsByMeterCode(code)) {
            throw new DuplicateResourceException("Meter code already exists: " + code);
        }
        Meter m = new Meter();
        apply(m, in, code);
        return MeterView.from(repo.save(m));
    }

    /** Edit Meter */
    @Transactional
    public MeterView update(Long id, MeterInput in) {
        Meter m = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with id: " + id));
        String code = in.meterCode().trim();
        if (repo.existsByMeterCodeAndIdNot(code, id)) {
            throw new DuplicateResourceException("Meter code already exists: " + code);
        }
        apply(m, in, code);
        return MeterView.from(repo.save(m));
    }

    /** Delete Meter */
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Meter not found with id: " + id);
        }
        repo.deleteById(id);
    }

    /**
     * Optional demo helper: simulates new live values for every ONLINE meter.
     * The React UI does not call this; real values come from the database.
     */
    @Transactional
    public List<MeterView> refresh() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<Meter> meters = repo.findAllByOrderByIdAsc();
        LocalDateTime now = LocalDateTime.now();

        for (Meter m : meters) {
            if (m.getStatus() == MeterStatus.ONLINE) {
                double voltage = round(rnd.nextDouble(225, 235));
                double current = round(rnd.nextDouble(4, 7));
                double power = round(voltage * current);
                double reading = round((m.getReading() == null ? 0 : m.getReading()) + power / 1000 / 60);
                m.setVoltage(voltage);
                m.setCurrent(current);
                m.setPower(power);
                m.setReading(reading);
            }
            m.setLastUpdated(now);
        }
        return repo.saveAll(meters).stream().map(MeterView::from).toList();
    }

    private void apply(Meter m, MeterInput in, String code) {
        MeterStatus status = in.status() == null ? MeterStatus.ONLINE : in.status();
        String ip = in.ipAddress() == null || in.ipAddress().isBlank() ? null : in.ipAddress().trim();

        m.setMeterCode(code);
        m.setMeterTypeId(in.meterTypeId());
        m.setIpAddress(ip);
        m.setStatus(status);

        if (status == MeterStatus.ONLINE) {
            m.setVoltage(in.voltage());
            m.setCurrent(in.current());
            m.setPower(in.power());
            m.setReading(in.reading());
        } else {
            m.setVoltage(null);
            m.setCurrent(null);
            m.setPower(null);
            m.setReading(null);
        }
        m.setLastUpdated(LocalDateTime.now());
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
