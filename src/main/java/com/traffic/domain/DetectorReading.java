package com.traffic.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "detector_readings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "controller_id")
    private String controllerId;

    private Integer detectorId;

    private String detectorName;

    private Integer vehicleCount;

    private BigDecimal occupancy;

    private Instant readingTimestamp;

    private Instant fetchedAt;
}
