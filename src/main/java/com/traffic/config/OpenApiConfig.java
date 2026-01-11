package com.traffic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI trafficControllerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Traffic Controller Integration API")
                        .description("API for monitoring and controlling traffic controllers")
                        .version("1.0.0"));
    }
}
