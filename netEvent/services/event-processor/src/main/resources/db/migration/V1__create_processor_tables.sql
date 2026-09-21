CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS notification_run (
    run_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id BIGINT NOT NULL,
    correlation_id UUID NOT NULL UNIQUE,
    trigger_type VARCHAR DEFAULT 'EVENT' CHECK (trigger_type IN ('CRON','MANUAL','EVENT')),
    triggered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    status VARCHAR NOT NULL DEFAULT 'RECEIVED'
        CHECK (status IN ('RECEIVED','VALIDATING','PROCESSING','CONTENT_READY','WAITING_APPROVAL','APPROVED','REJECTED','TIMEOUT','ROUTING','DISPATCHING','COMPLETED','PARTIAL_FAILED','FAILED')),
    approval_required BOOLEAN NOT NULL DEFAULT false,
    error_code VARCHAR,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_run_correlation ON notification_run(correlation_id);
CREATE INDEX IF NOT EXISTS idx_run_status ON notification_run(status, triggered_at);

CREATE TABLE IF NOT EXISTS notifications (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id UUID NOT NULL UNIQUE,
    notification_event_id UUID NOT NULL UNIQUE,
    profile_id BIGINT NOT NULL,
    contents TEXT,
    context_data JSONB,
    approve_level INT,
    status VARCHAR NOT NULL DEFAULT 'WAITING_APPROVAL',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_notifications_event_id ON notifications(notification_event_id);
CREATE INDEX IF NOT EXISTS idx_notifications_run ON notifications(run_id);

CREATE TABLE IF NOT EXISTS outbox_event (
    outbox_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID UNIQUE NOT NULL,
    aggregate_type VARCHAR NOT NULL,
    aggregate_id UUID NOT NULL,
    topic VARCHAR NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','PUBLISHED','FAILED')),
    retry_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now(),
    published_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS processed_event (
    event_id UUID NOT NULL,
    consumer_name VARCHAR NOT NULL,
    processed_at TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (event_id, consumer_name)
);
