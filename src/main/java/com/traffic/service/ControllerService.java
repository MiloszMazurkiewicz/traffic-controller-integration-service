package com.traffic.service;

import com.traffic.adapter.ProtocolAdapter;
import com.traffic.adapter.dto.CommandResultDto;
import com.traffic.domain.CommandExecution;
import com.traffic.domain.ControllerStatus;
import com.traffic.domain.DetectorReading;
import com.traffic.repository.CommandExecutionRepository;
import com.traffic.repository.ControllerRepository;
import com.traffic.repository.ControllerStatusRepository;
import com.traffic.repository.DetectorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ControllerService {

    private final ProtocolAdapter protocolAdapter;
    private final ControllerRepository controllerRepository;
    private final ControllerStatusRepository controllerStatusRepository;
    private final DetectorReadingRepository detectorReadingRepository;
    private final CommandExecutionRepository commandExecutionRepository;

    public ControllerStatus getLatestStatus(String controllerId) {
        validateController(controllerId);
        return controllerStatusRepository.findTopByControllerIdOrderByFetchedAtDesc(controllerId)
                .orElse(null);
    }

    public List<DetectorReading> getLatestDetectorReadings(String controllerId) {
        validateController(controllerId);
        return detectorReadingRepository.findLatestByControllerId(controllerId);
    }

    public Page<DetectorReading> getDetectorReadingsHistory(
            String controllerId,
            Instant from,
            Instant to,
            Pageable pageable) {
        validateController(controllerId);

        if (from != null && to != null) {
            return detectorReadingRepository.findByControllerIdAndFetchedAtBetween(
                    controllerId, from, to, pageable);
        }
        return detectorReadingRepository.findByControllerId(controllerId, pageable);
    }

    @Transactional
    public CommandExecution sendCommand(String controllerId, String command, String value) {
        validateController(controllerId);

        CommandResultDto result = protocolAdapter.sendCommand(controllerId, command, value);

        CommandExecution execution = CommandExecution.builder()
                .controllerId(controllerId)
                .command(command)
                .success(result.isSuccess())
                .value(value)
                .executedAt(result.getTimestamp())
                .build();

        return commandExecutionRepository.save(execution);
    }

    public Page<CommandExecution> getCommandHistory(String controllerId, Pageable pageable) {
        validateController(controllerId);
        return commandExecutionRepository.findByControllerIdOrderByExecutedAtDesc(controllerId, pageable);
    }

    private void validateController(String controllerId) {
        if (!controllerRepository.existsById(controllerId)) {
            throw new ControllerNotFoundException(controllerId);
        }
    }

    public static class ControllerNotFoundException extends RuntimeException {
        public ControllerNotFoundException(String controllerId) {
            super("Controller not found: " + controllerId);
        }
    }
}
