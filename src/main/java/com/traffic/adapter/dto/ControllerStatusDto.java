package com.traffic.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControllerStatusDto {

    private String controllerId;
    private String state;
    private String program;
    private Instant lastUpdated;
    private List<ErrorDto> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDto {
        private String code;
        private String message;
    }
}
