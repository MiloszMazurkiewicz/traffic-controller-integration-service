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
}
