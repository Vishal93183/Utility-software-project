package com.utility.software.entity;

import com.utility.software.enums.MeterStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "meters")
@Getter
@Setter
@NoArgsConstructor
public class Meter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String meterCode;           // MTR-001

    private Integer meterTypeId;

    @Column(length = 50)
    private String ipAddress;           // 192.168.1.100:100

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MeterStatus status = MeterStatus.ONLINE;

    private Double voltage;
    private Double current;
    private Double power;
    private Double reading;

    private LocalDateTime lastUpdated;
}
