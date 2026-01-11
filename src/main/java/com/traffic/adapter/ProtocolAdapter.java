package com.traffic.adapter;

import com.traffic.adapter.dto.CommandResultDto;
import com.traffic.adapter.dto.ControllerStatusDto;
import com.traffic.adapter.dto.DetectorReadingsDto;

import java.util.List;

public interface ProtocolAdapter {

    ControllerStatusDto readStatus(String controllerId);

    DetectorReadingsDto readDetectorReadings(String controllerId);

    CommandResultDto sendCommand(String controllerId, String command, String value);
}
