package com.traffic.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandResultDto {

    private String controllerId;
    private String command;
    private boolean success;
    private String value;
    private Instant timestamp;
}
