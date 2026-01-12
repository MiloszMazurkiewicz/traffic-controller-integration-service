package com.traffic.controller;

import com.traffic.AbstractIntegrationTest;
import com.traffic.controller.dto.CommandRequest;
import com.traffic.domain.Controller;
import com.traffic.domain.ControllerStatus;
import com.traffic.domain.DetectorReading;
import com.traffic.repository.CommandExecutionRepository;
import com.traffic.repository.ControllerRepository;
import com.traffic.repository.ControllerStatusRepository;
import com.traffic.repository.DetectorReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoringControllerIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @Autowired
    private ControllerRepository controllerRepository;

    @Autowired
    private ControllerStatusRepository controllerStatusRepository;

    @Autowired
    private DetectorReadingRepository detectorReadingRepository;

    @Autowired
    private CommandExecutionRepository commandExecutionRepository;

    private static final String TEST_CONTROLLER_ID = "test.controller.1";

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        commandExecutionRepository.deleteAll();
        detectorReadingRepository.deleteAll();
        controllerStatusRepository.deleteAll();
        controllerRepository.deleteAll();

        Controller controller = Controller.builder()
                .id(TEST_CONTROLLER_ID)
                .registeredAt(Instant.now())
                .build();
        controllerRepository.save(controller);
    }

    @Test
    void getStatus_whenStatusExists_returnsLatestStatus() {
        ControllerStatus status = ControllerStatus.builder()
                .controllerId(TEST_CONTROLLER_ID)
                .state("OPERATIONAL")
                .program("SP1")
                .fetchedAt(Instant.now())
                .errors(List.of())
                .build();
        controllerStatusRepository.save(status);

        ControllerStatus response = restClient.get()
                .uri("/api/controllers/{id}/status", TEST_CONTROLLER_ID)
                .retrieve()
                .body(ControllerStatus.class);

        assertThat(response).isNotNull();
        assertThat(response.getState()).isEqualTo("OPERATIONAL");
    }

    @Test
    void getDetectorReadings_returnsLatestReadings() {
        Instant now = Instant.now();
        DetectorReading reading1 = DetectorReading.builder()
                .controllerId(TEST_CONTROLLER_ID)
                .detectorId(1)
                .detectorName("D1")
                .vehicleCount(10)
                .occupancy(BigDecimal.valueOf(0.25))
                .readingTimestamp(now)
                .fetchedAt(now)
                .build();
        DetectorReading reading2 = DetectorReading.builder()
                .controllerId(TEST_CONTROLLER_ID)
                .detectorId(2)
                .detectorName("D2")
                .vehicleCount(15)
                .occupancy(BigDecimal.valueOf(0.35))
                .readingTimestamp(now)
                .fetchedAt(now)
                .build();
        detectorReadingRepository.saveAll(List.of(reading1, reading2));

        DetectorReading[] response = restClient.get()
                .uri("/api/controllers/{id}/detectors", TEST_CONTROLLER_ID)
                .retrieve()
                .body(DetectorReading[].class);

        assertThat(response).hasSize(2);
    }

    @Test
    void sendCommand_executesAndReturnsResult() {
        CommandRequest request = new CommandRequest("CHANGE_PROGRAM", "SP2");

        Map response = restClient.post()
                .uri("/api/controllers/{id}/commands", TEST_CONTROLLER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        assertThat(response).isNotNull();
        assertThat(response.get("command")).isEqualTo("CHANGE_PROGRAM");
        assertThat(response.get("value")).isEqualTo("SP2");
    }

    @Test
    void getDetectorReadingsHistory_withTimeRange_returnsFilteredResults() {
        Instant now = Instant.now();
        Instant hourAgo = now.minusSeconds(3600);
        Instant twoHoursAgo = now.minusSeconds(7200);

        DetectorReading oldReading = DetectorReading.builder()
                .controllerId(TEST_CONTROLLER_ID)
                .detectorId(1)
                .detectorName("D1")
                .vehicleCount(5)
                .occupancy(BigDecimal.valueOf(0.15))
                .readingTimestamp(twoHoursAgo)
                .fetchedAt(twoHoursAgo)
                .build();
        DetectorReading recentReading = DetectorReading.builder()
                .controllerId(TEST_CONTROLLER_ID)
                .detectorId(1)
                .detectorName("D1")
                .vehicleCount(10)
                .occupancy(BigDecimal.valueOf(0.25))
                .readingTimestamp(now)
                .fetchedAt(now)
                .build();
        detectorReadingRepository.saveAll(List.of(oldReading, recentReading));

        Map response = restClient.get()
                .uri("/api/controllers/{id}/detectors/history?from={from}&to={to}",
                        TEST_CONTROLLER_ID,
                        hourAgo.minusSeconds(60).toString(),
                        now.plusSeconds(60).toString())
                .retrieve()
                .body(Map.class);

        assertThat(response).isNotNull();
        List<?> content = (List<?>) response.get("content");
        assertThat(content).hasSize(1);
    }

    // ==================== 404 Error Tests ====================

    @Test
    void getStatus_whenControllerNotFound_returns404() {
        HttpStatusCode statusCode = restClient.get()
                .uri("/api/controllers/{id}/status", "non.existent.controller")
                .exchange((request, response) -> response.getStatusCode());

        assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getDetectorReadings_whenControllerNotFound_returns404() {
        HttpStatusCode statusCode = restClient.get()
                .uri("/api/controllers/{id}/detectors", "non.existent.controller")
                .exchange((request, response) -> response.getStatusCode());

        assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void sendCommand_whenControllerNotFound_returns404() {
        CommandRequest request = new CommandRequest("CHANGE_PROGRAM", "SP2");

        HttpStatusCode statusCode = restClient.post()
                .uri("/api/controllers/{id}/commands", "non.existent.controller")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange((req, response) -> response.getStatusCode());

        assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getCommandHistory_whenControllerNotFound_returns404() {
        HttpStatusCode statusCode = restClient.get()
                .uri("/api/controllers/{id}/commands/history", "non.existent.controller")
                .exchange((request, response) -> response.getStatusCode());

        assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== Command History Tests ====================

    @Test
    void getCommandHistory_returnsCommandsInDescendingOrder() {
        // Send multiple commands
        restClient.post()
                .uri("/api/controllers/{id}/commands", TEST_CONTROLLER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CommandRequest("CHANGE_PROGRAM", "SP1"))
                .retrieve()
                .toBodilessEntity();

        restClient.post()
                .uri("/api/controllers/{id}/commands", TEST_CONTROLLER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CommandRequest("CHANGE_PROGRAM", "SP2"))
                .retrieve()
                .toBodilessEntity();

        Map response = restClient.get()
                .uri("/api/controllers/{id}/commands/history", TEST_CONTROLLER_ID)
                .retrieve()
                .body(Map.class);

        assertThat(response).isNotNull();
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        assertThat(content).hasSize(2);
        // Most recent command should be first
        assertThat(content.get(0).get("value")).isEqualTo("SP2");
        assertThat(content.get(1).get("value")).isEqualTo("SP1");
    }

    // ==================== Edge Case Tests ====================

    @Test
    void getStatus_whenNoStatusExists_returns204NoContent() {
        // Controller exists but has no status records
        HttpStatusCode statusCode = restClient.get()
                .uri("/api/controllers/{id}/status", TEST_CONTROLLER_ID)
                .exchange((request, response) -> response.getStatusCode());

        assertThat(statusCode).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getDetectorReadings_whenNoReadingsExist_returnsEmptyList() {
        DetectorReading[] response = restClient.get()
                .uri("/api/controllers/{id}/detectors", TEST_CONTROLLER_ID)
                .retrieve()
                .body(DetectorReading[].class);

        assertThat(response).isEmpty();
    }

    @Test
    void getCommandHistory_whenNoCommandsExist_returnsEmptyPage() {
        Map response = restClient.get()
                .uri("/api/controllers/{id}/commands/history", TEST_CONTROLLER_ID)
                .retrieve()
                .body(Map.class);

        assertThat(response).isNotNull();
        List<?> content = (List<?>) response.get("content");
        assertThat(content).isEmpty();
        assertThat(response.get("totalElements")).isEqualTo(0);
    }

    @Test
    void getDetectorReadingsHistory_withPagination_respectsPageSize() {
        Instant now = Instant.now();
        // Create 5 readings
        for (int i = 0; i < 5; i++) {
            DetectorReading reading = DetectorReading.builder()
                    .controllerId(TEST_CONTROLLER_ID)
                    .detectorId(1)
                    .detectorName("D1")
                    .vehicleCount(i * 10)
                    .occupancy(BigDecimal.valueOf(0.1 * i))
                    .readingTimestamp(now.minusSeconds(i * 60))
                    .fetchedAt(now.minusSeconds(i * 60))
                    .build();
            detectorReadingRepository.save(reading);
        }

        Map response = restClient.get()
                .uri("/api/controllers/{id}/detectors/history?page=0&size=2", TEST_CONTROLLER_ID)
                .retrieve()
                .body(Map.class);

        assertThat(response).isNotNull();
        List<?> content = (List<?>) response.get("content");
        assertThat(content).hasSize(2);
        assertThat(response.get("totalElements")).isEqualTo(5);
        assertThat(response.get("totalPages")).isEqualTo(3);
    }
}
