-- Migration V1 for delivery_db (channel-workers)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS delivery_log (
    delivery_log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL,
    notification_id UUID NOT NULL,
    run_id UUID NOT NULL,
    correlation_id UUID NOT NULL,
    channel_code VARCHAR(20) NOT NULL,
    recipient_id UUID,
    recipient_contact VARCHAR(255) NOT NULL,
    attempt_no INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PROCESSING','SUCCESS','FAILED','TIMEOUT')),
    request_payload JSONB,
    provider_code VARCHAR(50),
    provider_message_id VARCHAR(255),
    response_code VARCHAR(50),
    response_body TEXT,
    error_code VARCHAR(100),
    error_message TEXT,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at TIMESTAMPTZ,
    duration_ms INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_task_attempt UNIQUE (task_id, attempt_no)
);

CREATE INDEX IF NOT EXISTS idx_delivery_log_task ON delivery_log(task_id);
CREATE INDEX IF NOT EXISTS idx_delivery_log_notification ON delivery_log(notification_id);
CREATE INDEX IF NOT EXISTS idx_delivery_log_channel ON delivery_log(channel_code);
CREATE INDEX IF NOT EXISTS idx_delivery_log_failed ON delivery_log(created_at) WHERE status IN ('FAILED','TIMEOUT');
CREATE INDEX IF NOT EXISTS brin_delivery_log_created_at ON delivery_log USING BRIN(created_at);

CREATE TABLE IF NOT EXISTS processed_event (
    event_id UUID NOT NULL,
    consumer_name VARCHAR NOT NULL,
    processed_at TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (event_id, consumer_name)
);
