package com.utility.software.repository;

import com.utility.software.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeterRepository extends JpaRepository<Meter, Long> {
    List<Meter> findAllByOrderByIdAsc();

    boolean existsByMeterCode(String meterCode);

    boolean existsByMeterCodeAndIdNot(String meterCode, Long id);
}
