package com.utility.software.config;

import com.utility.software.entity.Meter;
import com.utility.software.enums.MeterStatus;
import com.utility.software.repository.MeterRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Optional demo data. Disabled by default; enable with app.seed-sample-data=true
 * (inserts 5 sample meters only when the table is empty).
 */
@Component
@ConditionalOnProperty(name = "app.seed-sample-data", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final MeterRepository repo;

    public DataSeeder(MeterRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        LocalDateTime now = LocalDateTime.now();
        repo.saveAll(List.of(
                meter("MTR-001", 14, "192.168.1.100:100", MeterStatus.ONLINE, 230.5, 5.2, 1196.2, 1256.48, now),
                meter("MTR-002", 14, "192.168.1.101:100", MeterStatus.ONLINE, 228.9, 4.8, 1098.6, 983.21, now),
                meter("MTR-003", 14, "192.168.1.102:100", MeterStatus.OFFLINE, null, null, null, null, now),
                meter("MTR-004", 14, "192.168.1.103:100", MeterStatus.ONLINE, 231.1, 6.1, 1408.3, 1620.75, now),
                meter("MTR-005", 14, "192.168.1.104:100", MeterStatus.ONLINE, 229.8, 5.6, 1288.4, 1102.33, now)
        ));
    }

    private Meter meter(String code, int type, String ip, MeterStatus status,
                        Double v, Double c, Double p, Double r, LocalDateTime t) {
        Meter m = new Meter();
        m.setMeterCode(code);
        m.setMeterTypeId(type);
        m.setIpAddress(ip);
        m.setStatus(status);
        m.setVoltage(v);
        m.setCurrent(c);
        m.setPower(p);
        m.setReading(r);
        m.setLastUpdated(t);
        return m;
    }
}
