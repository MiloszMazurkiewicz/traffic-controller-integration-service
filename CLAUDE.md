# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Traffic Controller Integration Service - a Spring Boot 4.0.1 service (Java 25) that integrates with remote traffic controllers, normalizes data from different protocols, and exposes monitoring REST APIs.

## Build Commands

```bash
# Build
mvn clean package

# Run tests (requires Docker for TestContainers)
mvn test

# Run application (requires PostgreSQL on localhost:5432)
mvn spring-boot:run

# Run with Docker Compose (recommended)
docker-compose up
```

## Project Structure

```
src/main/java/com/traffic/
├── adapter/                 # Protocol adapters (ProtocolAdapter interface)
│   ├── dto/                 # Protocol-specific DTOs
│   └── MockProtocolAdapter  # Simulated device responses
├── config/                  # Spring configuration (Flyway, JPA, OpenAPI)
├── controller/              # REST controllers (MonitoringController)
├── domain/                  # JPA entities (Controller, ControllerStatus, DetectorReading, CommandExecution)
├── repository/              # Spring Data JPA repositories
└── service/                 # Business logic (IngestionService, ControllerService)
```

## Architecture

- **Adapter Pattern**: `ProtocolAdapter` interface abstracts device communication
- **Scheduled Polling**: `IngestionService` polls controllers every 30s (configurable)
- **Append-only History**: Status and readings stored with timestamps for historical queries
- **Virtual Threads**: Enabled for high-concurrency request handling

## Key Configuration Files

- `src/main/resources/application.yml` - Main config (DB, Flyway, polling interval)
- `src/main/resources/controller-ids.yml` - List of monitored controller IDs
- `src/main/resources/db/migration/V1__init.sql` - Database schema

## API Endpoints

- `GET /api/controllers/{id}/status` - Current controller status
- `GET /api/controllers/{id}/detectors` - Current detector readings
- `GET /api/controllers/{id}/detectors/history?from=&to=&page=&size=` - Historical readings
- `POST /api/controllers/{id}/commands` - Send command to controller
- `GET /api/controllers/{id}/commands/history` - Command history
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## How Claude should work

- Always propose a step-by-step plan and wait for my approval before editing any files.
- Prefer small, incremental changes with clear diffs.
- Commit each feature separately with descriptive messages.
- When unsure, ask a clarifying question instead of guessing.

## Spring Boot Guidelines

- Follow standard Spring Boot conventions, similar to Baeldung's recommendations.
- Prefer constructor-based dependency injection, avoid field injection.
- Keep configuration in `application.yml`
- Write focused `@Service`, `@Repository`, and `@RestController` classes with clear responsibilities.
- When unsure about an approach, choose the idiomatic Spring Boot way as commonly described on Baeldung.
- Use Lombok for boilerplate code (`@Data`, `@Builder`, `@Slf4j`, `@RequiredArgsConstructor`)
- Use Spring Data JPA for database access
- Use PostgreSQL for database
- Use Flyway for database migrations
- Use TestContainers for integration tests (with Ryuk disabled for Colima compatibility)
- Use Maven for build
