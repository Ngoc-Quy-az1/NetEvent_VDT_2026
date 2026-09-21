CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS audit_log (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    correlation_id UUID NOT NULL,
    run_id UUID,
    event_type VARCHAR NOT NULL,
    service_name VARCHAR NOT NULL,
    stage VARCHAR NOT NULL,
    status VARCHAR NOT NULL CHECK (status IN ('SUCCESS','FAILED')),
    actor VARCHAR NOT NULL DEFAULT 'SYSTEM',
    metadata JSONB,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_log_correlation ON audit_log(correlation_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_run ON audit_log(run_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_event_type ON audit_log(event_type);
CREATE INDEX IF NOT EXISTS brin_audit_log_occurred ON audit_log USING BRIN(occurred_at);

CREATE TABLE IF NOT EXISTS audit_history (
    audit_history_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL,
    channel_code VARCHAR NOT NULL,
    payload_snapshot JSONB NOT NULL,
    recipient_contact VARCHAR NOT NULL,
    response_code VARCHAR,
    response_body TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_history_task ON audit_history(task_id);
CREATE INDEX IF NOT EXISTS idx_audit_history_channel ON audit_history(channel_code);
