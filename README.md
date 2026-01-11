# Traffic Controller Integration Service

A Spring Boot service that integrates with remote traffic controllers, normalizes data from different protocols, and exposes monitoring APIs.

## Quick Start

### Prerequisites
- Java 25+
- Maven 3.8+
- Docker (for PostgreSQL)

### Run PostgreSQL
```bash
docker run -d --name traffic-postgres \
  -e POSTGRES_DB=traffic_controller \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

### Run the Application
```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

## Data Flow

```
┌─────────────────┐     ┌──────────────┐     ┌────────────┐     ┌──────────┐
│ ProtocolAdapter │────>│ Normalizer   │────>│ PostgreSQL │────>│ REST API │
│ (Mock Device)   │     │ (DTO→Entity) │     │ (Flyway)   │     │ /api/*   │
└─────────────────┘     └──────────────┘     └────────────┘     └──────────┘
        ^                                           │
        │           @Scheduled polling              │
        └───────────────────────────────────────────┘
```

**Flow Description:**
1. `IngestionService` polls controllers at configurable intervals (default: 30s)
2. `MockProtocolAdapter` returns simulated device data
3. Data is normalized from protocol-specific DTOs to domain entities
4. Normalized data is persisted to PostgreSQL via Spring Data JPA
5. REST APIs expose current state and historical data

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/controllers/{id}/status` | Current controller status |
| GET | `/api/controllers/{id}/detectors` | Current detector readings |
| GET | `/api/controllers/{id}/detectors/history?from=&to=&page=&size=` | Historical readings |
| POST | `/api/controllers/{id}/commands` | Send command to controller |
| GET | `/api/controllers/{id}/commands/history?page=&size=` | Command execution history |

### Example Requests

```bash
# Get controller status
curl http://localhost:8080/api/controllers/fd132.z1.highway.a21.loc/status

# Get current detector readings
curl http://localhost:8080/api/controllers/fd11.z1.downtown.loc/detectors

# Get historical readings with time range
curl "http://localhost:8080/api/controllers/fd11.z1.downtown.loc/detectors/history?from=2024-01-01T00:00:00Z&to=2024-01-02T00:00:00Z&page=0&size=20"

# Send command
curl -X POST http://localhost:8080/api/controllers/fd35.z1.suburb12.loc/commands \
  -H "Content-Type: application/json" \
  -d '{"command": "CHANGE_PROGRAM", "value": "SP2"}'

# Get command history
curl "http://localhost:8080/api/controllers/fd35.z1.suburb12.loc/commands/history?page=0&size=10"
```

## Configuration

### Controller IDs
Edit `src/main/resources/controller-ids.yml` to configure monitored controllers:
```yaml
controllers:
  ids:
    - fd132.z1.highway.a21.loc
    - fd11.z1.downtown.loc
    - fd35.z1.suburb12.loc
    - fd88.z2.industrial.loc
    - fd99.z3.airport.loc
```

### Polling Interval
Edit `src/main/resources/application.yml`:
```yaml
ingestion:
  polling-interval-ms: 30000  # 30 seconds
```

## Project Structure

```
src/main/java/com/traffic/
├── adapter/                 # Protocol adapters
│   ├── ProtocolAdapter.java # Interface for device communication
│   ├── MockProtocolAdapter  # Simulated device responses
│   └── dto/                 # Protocol-specific data structures
├── domain/                  # JPA entities (normalized model)
├── repository/              # Spring Data JPA repositories
├── service/                 # Business logic
│   ├── IngestionService     # Scheduled data polling
│   └── ControllerService    # API business logic
├── controller/              # REST controllers
└── config/                  # Configuration classes
```

## Design Decisions

- **Adapter Pattern**: `ProtocolAdapter` interface allows easy addition of new protocols
- **Append-only History**: Status and readings are stored with timestamps for historical queries
- **JSONB for Errors**: Flexible storage for varying error structures
- **Indexed Queries**: `controller_id + timestamp` indexes for efficient time-range queries

## Running Tests

Integration tests use TestContainers to spin up a PostgreSQL container automatically.

```bash
# Run all tests (requires Docker)
mvn test
```

### Colima / Non-Docker Desktop Setup

If using Colima or another Docker alternative, configure TestContainers:

```bash
# Create ~/.testcontainers.properties
cat > ~/.testcontainers.properties << 'EOF'
docker.host=unix://${HOME}/.colima/default/docker.sock
ryuk.disabled=true
EOF

# Run tests with Ryuk disabled
TESTCONTAINERS_RYUK_DISABLED=true mvn test
```

### Test Coverage

| Test Class | Tests | Description |
|------------|-------|-------------|
| `MonitoringControllerIntegrationTest` | 4 | REST API endpoint tests |
| `IngestionServiceTest` | 3 | Data polling and persistence tests |
