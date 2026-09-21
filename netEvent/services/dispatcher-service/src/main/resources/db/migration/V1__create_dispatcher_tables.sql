-- Migration V1 for dispatcher_db (dispatcher-service)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS notifications_task (
    task_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID NOT NULL,
    run_id UUID NOT NULL,
    correlation_id UUID NOT NULL,
    channel_code VARCHAR NOT NULL,
    recipient_id UUID NOT NULL,
    status VARCHAR NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','SENDING','SENT','FAILED','DEAD_LETTER')),
    retry_count INT NOT NULL DEFAULT 0,
    content TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_task_dedup UNIQUE (notification_id, channel_code, recipient_id)
);

CREATE INDEX IF NOT EXISTS idx_task_notification ON notifications_task(notification_id);
CREATE INDEX IF NOT EXISTS idx_task_recipient ON notifications_task(recipient_id);

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
