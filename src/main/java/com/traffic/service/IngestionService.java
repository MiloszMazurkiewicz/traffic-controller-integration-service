package com.traffic.service;

import com.traffic.adapter.ProtocolAdapter;
import com.traffic.adapter.dto.ControllerStatusDto;
import com.traffic.adapter.dto.DetectorReadingsDto;
import com.traffic.config.ControllersConfig;
import com.traffic.domain.Controller;
import com.traffic.domain.ControllerStatus;
import com.traffic.domain.DetectorReading;
import com.traffic.repository.ControllerRepository;
import com.traffic.repository.ControllerStatusRepository;
import com.traffic.repository.DetectorReadingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {

    private final ProtocolAdapter protocolAdapter;
    private final ControllersConfig controllersConfig;
    private final ControllerRepository controllerRepository;
    private final ControllerStatusRepository controllerStatusRepository;
    private final DetectorReadingRepository detectorReadingRepository;

    @PostConstruct
    @Transactional
    public void registerControllers() {
        List<String> controllerIds = controllersConfig.getIds();
        log.info("Registering {} controllers", controllerIds.size());

        for (String controllerId : controllerIds) {
            if (!controllerRepository.existsById(controllerId)) {
                Controller controller = Controller.builder()
                        .id(controllerId)
                        .registeredAt(Instant.now())
                        .build();
                controllerRepository.save(controller);
                log.info("Registered controller: {}", controllerId);
            }
        }
    }

    @Scheduled(fixedRateString = "${ingestion.polling-interval-ms}")
    @Transactional
    public void pollControllers() {
        List<String> controllerIds = controllersConfig.getIds();
        Instant fetchedAt = Instant.now();

        log.debug("Polling {} controllers", controllerIds.size());

        for (String controllerId : controllerIds) {
            try {
                pollStatus(controllerId, fetchedAt);
                pollDetectorReadings(controllerId, fetchedAt);
            } catch (Exception e) {
                log.error("Error polling controller {}: {}", controllerId, e.getMessage());
            }
        }
    }

    private void pollStatus(String controllerId, Instant fetchedAt) {
        ControllerStatusDto statusDto = protocolAdapter.readStatus(controllerId);
        ControllerStatus status = mapToEntity(statusDto, fetchedAt);
        controllerStatusRepository.save(status);
        log.debug("Saved status for controller {}: {}", controllerId, status.getState());
    }

    private void pollDetectorReadings(String controllerId, Instant fetchedAt) {
        DetectorReadingsDto readingsDto = protocolAdapter.readDetectorReadings(controllerId);
        List<DetectorReading> readings = mapToEntities(readingsDto, fetchedAt);
        detectorReadingRepository.saveAll(readings);
        log.debug("Saved {} detector readings for controller {}", readings.size(), controllerId);
    }

    private ControllerStatus mapToEntity(ControllerStatusDto dto, Instant fetchedAt) {
        List<ControllerStatus.ErrorInfo> errors = dto.getErrors() != null
                ? dto.getErrors().stream()
                    .map(e -> ControllerStatus.ErrorInfo.builder()
                            .code(e.getCode())
                            .message(e.getMessage())
                            .build())
                    .toList()
                : List.of();

        return ControllerStatus.builder()
                .controllerId(dto.getControllerId())
                .state(dto.getState())
                .program(dto.getProgram())
                .fetchedAt(fetchedAt)
                .errors(errors)
                .build();
    }

    private List<DetectorReading> mapToEntities(DetectorReadingsDto dto, Instant fetchedAt) {
        return dto.getDetectors().stream()
                .map(d -> DetectorReading.builder()
                        .controllerId(dto.getControllerId())
                        .detectorId(d.getId())
                        .detectorName(d.getName())
                        .vehicleCount(d.getVehicleCount())
                        .occupancy(d.getOccupancy())
                        .readingTimestamp(d.getTimestamp())
                        .fetchedAt(fetchedAt)
                        .build())
                .toList();
    }
}
