package com.traffic.service;

import com.traffic.AbstractIntegrationTest;
import com.traffic.domain.Controller;
import com.traffic.domain.ControllerStatus;
import com.traffic.domain.DetectorReading;
import com.traffic.repository.ControllerRepository;
import com.traffic.repository.ControllerStatusRepository;
import com.traffic.repository.DetectorReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IngestionServiceTest extends AbstractIntegrationTest {

    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private ControllerRepository controllerRepository;

    @Autowired
    private ControllerStatusRepository controllerStatusRepository;

    @Autowired
    private DetectorReadingRepository detectorReadingRepository;

    @BeforeEach
    void setUp() {
        detectorReadingRepository.deleteAll();
        controllerStatusRepository.deleteAll();
    }

    @Test
    void registerControllers_createsControllersFromConfig() {
        List<Controller> controllers = controllerRepository.findAll();

        assertThat(controllers).isNotEmpty();
        assertThat(controllers.stream().map(Controller::getId))
                .contains("fd132.z1.highway.a21.loc", "fd11.z1.downtown.loc");
    }

    @Test
    void pollControllers_savesStatusAndReadings() {
        long initialStatusCount = controllerStatusRepository.count();
        long initialReadingsCount = detectorReadingRepository.count();

        ingestionService.pollControllers();

        List<ControllerStatus> statuses = controllerStatusRepository.findAll();
        List<DetectorReading> readings = detectorReadingRepository.findAll();

        assertThat(statuses.size()).isGreaterThan((int) initialStatusCount);
        assertThat(readings.size()).isGreaterThan((int) initialReadingsCount);
    }

    @Test
    void pollControllers_savesStatusForEachController() {
        ingestionService.pollControllers();

        List<ControllerStatus> statuses = controllerStatusRepository.findAll();

        assertThat(statuses).extracting(ControllerStatus::getControllerId)
                .contains("fd132.z1.highway.a21.loc", "fd11.z1.downtown.loc");
    }
}
