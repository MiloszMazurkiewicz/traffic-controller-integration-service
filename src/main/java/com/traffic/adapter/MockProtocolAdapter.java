package com.traffic.adapter;

import com.traffic.adapter.dto.CommandResultDto;
import com.traffic.adapter.dto.ControllerStatusDto;
import com.traffic.adapter.dto.DetectorReadingsDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Random;

@Component
public class MockProtocolAdapter implements ProtocolAdapter {

    private static final List<String> STATES = List.of(
            "OPERATIONAL", "DETECTOR_FAILURE", "SIGNAL_FAILURE", "MAINTENANCE", "OFFLINE"
    );

    private static final List<String> PROGRAMS = List.of("SP1", "SP2", "SP3", "NIGHT", "EMERGENCY");

    private static final List<String> ERROR_CODES = List.of("E101", "E102", "E103", "E201", "E301");

    private static final List<String> ERROR_MESSAGES = List.of(
            "Detector D1 malfunction",
            "Detector D2 malfunction",
            "Detector D3 malfunction",
            "Signal group SG1 offline",
            "Signal group SG2 offline",
            "Communication timeout",
            "Power supply warning"
    );

    private final Random random = new Random();

    @Override
    public ControllerStatusDto readStatus(String controllerId) {
        String state = STATES.get(random.nextInt(STATES.size()));
        List<ControllerStatusDto.ErrorDto> errors = generateErrors(state);

        return ControllerStatusDto.builder()
                .controllerId(controllerId)
                .state(state)
                .program(PROGRAMS.get(random.nextInt(PROGRAMS.size())))
                .lastUpdated(Instant.now())
                .errors(errors)
                .build();
    }

    @Override
    public DetectorReadingsDto readDetectorReadings(String controllerId) {
        int detectorCount = 2 + random.nextInt(4);

        List<DetectorReadingsDto.DetectorDto> detectors = java.util.stream.IntStream
                .rangeClosed(1, detectorCount)
                .mapToObj(i -> DetectorReadingsDto.DetectorDto.builder()
                        .id(i)
                        .name("D" + i)
                        .vehicleCount(random.nextInt(50))
                        .occupancy(BigDecimal.valueOf(random.nextDouble())
                                .setScale(4, RoundingMode.HALF_UP))
                        .timestamp(Instant.now())
                        .build())
                .toList();

        return DetectorReadingsDto.builder()
                .controllerId(controllerId)
                .detectors(detectors)
                .build();
    }

    @Override
    public CommandResultDto sendCommand(String controllerId, String command, String value) {
        boolean success = random.nextDouble() > 0.1;

        return CommandResultDto.builder()
                .controllerId(controllerId)
                .command(command)
                .success(success)
                .value(value)
                .timestamp(Instant.now())
                .build();
    }

    private List<ControllerStatusDto.ErrorDto> generateErrors(String state) {
        if ("OPERATIONAL".equals(state)) {
            return List.of();
        }

        int errorCount = 1 + random.nextInt(3);
        return java.util.stream.IntStream.range(0, errorCount)
                .mapToObj(i -> ControllerStatusDto.ErrorDto.builder()
                        .code(ERROR_CODES.get(random.nextInt(ERROR_CODES.size())))
                        .message(ERROR_MESSAGES.get(random.nextInt(ERROR_MESSAGES.size())))
                        .build())
                .toList();
    }
}
