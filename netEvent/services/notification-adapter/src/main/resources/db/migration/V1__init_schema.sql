CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS notification_event (
    notification_event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_code VARCHAR NOT NULL UNIQUE,
    profile_id BIGINT NOT NULL,
    event_name VARCHAR NOT NULL,
    raw_event_payload JSONB NOT NULL,
    status VARCHAR NOT NULL DEFAULT 'RECEIVED' CHECK (status IN ('RECEIVED','PROCESSING','PROCESSED','INVALID','INCOMPLETE')),
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ NULL,
    correlation_id UUID NOT NULL DEFAULT gen_random_uuid()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_notification_event_code ON notification_event(event_code);
CREATE INDEX IF NOT EXISTS idx_notification_event_status ON notification_event(status, received_at);

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
