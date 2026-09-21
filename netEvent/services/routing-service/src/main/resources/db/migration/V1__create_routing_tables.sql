CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. channel
CREATE TABLE IF NOT EXISTS channel (
    channel_code VARCHAR PRIMARY KEY,
    channel_name VARCHAR NOT NULL,
    queue_name VARCHAR NULL,
    config JSONB NOT NULL DEFAULT '{}',
    status VARCHAR NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2. routing_template
CREATE TABLE IF NOT EXISTS routing_template (
    routing_template_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    channel_code VARCHAR NOT NULL REFERENCES channel(channel_code),
    profile_id BIGINT NOT NULL, -- Logical reference to profile(profile_id)
    content TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    frequency_mode VARCHAR CHECK (frequency_mode IN ('REALTIME','SCHEDULED','BATCH_WINDOW')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE PARTIAL UNIQUE INDEX IF NOT EXISTS uq_template_active_per_channel_profile 
    ON routing_template(channel_code, profile_id) WHERE is_active = true;

-- 3. recipient
CREATE TABLE IF NOT EXISTS recipient (
    recipient_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id BIGINT NULL,
    name VARCHAR NOT NULL,
    organization_id UUID NULL,
    channel_contacts JSONB NOT NULL,
    status VARCHAR NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','MUTED'))
);

CREATE INDEX IF NOT EXISTS idx_recipient_account_id ON recipient(account_id);

-- 4. recipient_group
CREATE TABLE IF NOT EXISTS recipient_group (
    group_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_name VARCHAR NOT NULL UNIQUE
);

-- 5. recipient_group_member
CREATE TABLE IF NOT EXISTS recipient_group_member (
    recipient_id UUID NOT NULL REFERENCES recipient(recipient_id),
    group_id UUID NOT NULL REFERENCES recipient_group(group_id),
    PRIMARY KEY (recipient_id, group_id)
);

CREATE INDEX IF NOT EXISTS idx_rgm_group_id ON recipient_group_member(group_id);

-- 6. Outbox & Inbox Pattern
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

-- SEED DATA FOR ROUTING
INSERT INTO channel (channel_code, channel_name, queue_name, config, status)
VALUES 
  ('SMS', 'SMS Gateway', 'delivery.sms.v1', '{}', 'ACTIVE'),
  ('EMAIL', 'Email Gateway', 'delivery.email.v1', '{}', 'ACTIVE'),
  ('OTT', 'Telegram/OTT Gateway', 'delivery.ott.v1', '{}', 'ACTIVE')
ON CONFLICT (channel_code) DO NOTHING;

INSERT INTO recipient (recipient_id, name, channel_contacts, status)
VALUES 
  ('11111111-1111-1111-1111-111111111111', 'NOC Staff On-Duty', '{"EMAIL": "noc-alerts@viettel.com.vn", "SMS": "+84988888888", "OTT": "@noc_telegram_bot"}', 'ACTIVE'),
  ('22222222-2222-2222-2222-222222222222', 'Operations Manager', '{"EMAIL": "manager-ops@viettel.com.vn"}', 'ACTIVE')
ON CONFLICT DO NOTHING;
