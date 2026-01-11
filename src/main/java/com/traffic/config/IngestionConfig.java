package com.traffic.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ingestion")
@Data
public class IngestionConfig {

    private long pollingIntervalMs = 30000;
}
