package com.traffic.controller;

import com.traffic.controller.dto.CommandRequest;
import com.traffic.domain.CommandExecution;
import com.traffic.domain.ControllerStatus;
import com.traffic.domain.DetectorReading;
import com.traffic.service.ControllerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/controllers/{controllerId}")
@RequiredArgsConstructor
public class MonitoringController {

    private final ControllerService controllerService;

    @GetMapping("/status")
    public ResponseEntity<ControllerStatus> getStatus(@PathVariable String controllerId) {
        ControllerStatus status = controllerService.getLatestStatus(controllerId);
        if (status == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/detectors")
    public ResponseEntity<List<DetectorReading>> getDetectorReadings(@PathVariable String controllerId) {
        List<DetectorReading> readings = controllerService.getLatestDetectorReadings(controllerId);
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/detectors/history")
    public ResponseEntity<Page<DetectorReading>> getDetectorReadingsHistory(
            @PathVariable String controllerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "fetchedAt") Pageable pageable) {

        Page<DetectorReading> history = controllerService.getDetectorReadingsHistory(
                controllerId, from, to, pageable);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/commands")
    public ResponseEntity<CommandExecution> sendCommand(
            @PathVariable String controllerId,
            @Valid @RequestBody CommandRequest request) {

        CommandExecution execution = controllerService.sendCommand(
                controllerId, request.getCommand(), request.getValue());
        return ResponseEntity.ok(execution);
    }

    @GetMapping("/commands/history")
    public ResponseEntity<Page<CommandExecution>> getCommandHistory(
            @PathVariable String controllerId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<CommandExecution> history = controllerService.getCommandHistory(controllerId, pageable);
        return ResponseEntity.ok(history);
    }
}
