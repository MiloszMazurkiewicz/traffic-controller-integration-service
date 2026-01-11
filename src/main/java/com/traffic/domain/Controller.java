package com.traffic.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "controllers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Controller {

    @Id
    private String id;

    private Instant registeredAt;
}
