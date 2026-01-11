package com.traffic.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectorReadingsDto {

    private String controllerId;
    private List<DetectorDto> detectors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetectorDto {
        private Integer id;
        private String name;
        private Integer vehicleCount;
        private BigDecimal occupancy;
        private Instant timestamp;
    }
}
