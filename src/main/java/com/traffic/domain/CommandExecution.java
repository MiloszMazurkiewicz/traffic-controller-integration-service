package com.traffic.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "command_executions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "controller_id")
    private String controllerId;

    private String command;

    private boolean success;

    private String value;

    private Instant executedAt;
}
