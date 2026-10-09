package com.utility.software.entity;

import com.utility.software.enums.OperationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_history")
@Getter
@Setter
@NoArgsConstructor
public class RequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;                // Get Meter Data / Update Config

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private OperationType opType;

    private String messageId;
    private Long meterId;
    private String ipAddress;

    private boolean success;            // green / red dot

    @Column(columnDefinition = "TEXT")
    private String requestJson;

    @Column(columnDefinition = "TEXT")
    private String responseJson;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
