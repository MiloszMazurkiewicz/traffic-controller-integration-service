package com.traffic.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandRequest {

    @NotBlank(message = "Command is required")
    private String command;

    private String value;
}
