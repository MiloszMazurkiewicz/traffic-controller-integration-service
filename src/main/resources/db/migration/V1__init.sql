-- Controllers table
CREATE TABLE controllers (
    id VARCHAR(100) PRIMARY KEY,
    registered_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Controller status history
CREATE TABLE controller_status (
    id BIGSERIAL PRIMARY KEY,
    controller_id VARCHAR(100) NOT NULL REFERENCES controllers(id),
    state VARCHAR(50) NOT NULL,
    program VARCHAR(50),
    fetched_at TIMESTAMP NOT NULL,
    errors JSONB
);

-- Detector readings history
CREATE TABLE detector_readings (
    id BIGSERIAL PRIMARY KEY,
    controller_id VARCHAR(100) NOT NULL REFERENCES controllers(id),
    detector_id INTEGER NOT NULL,
    detector_name VARCHAR(50),
    vehicle_count INTEGER,
    occupancy DECIMAL(5,4),
    reading_timestamp TIMESTAMP NOT NULL,
    fetched_at TIMESTAMP NOT NULL
);

-- Command execution history
CREATE TABLE command_executions (
    id BIGSERIAL PRIMARY KEY,
    controller_id VARCHAR(100) NOT NULL REFERENCES controllers(id),
    command VARCHAR(100) NOT NULL,
    success BOOLEAN NOT NULL,
    value VARCHAR(255),
    executed_at TIMESTAMP NOT NULL
);

-- Indexes for efficient historical queries
CREATE INDEX idx_controller_status_controller_fetched
    ON controller_status(controller_id, fetched_at DESC);

CREATE INDEX idx_detector_readings_controller_fetched
    ON detector_readings(controller_id, fetched_at DESC);

CREATE INDEX idx_command_executions_controller_executed
    ON command_executions(controller_id, executed_at DESC);
