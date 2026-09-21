
-- =============================================================================
-- 1. ADAPTER DATABASE (adapter_db) - Publisher
-- =============================================================================
SELECT 'Creating database adapter_db...' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'adapter_db');
CREATE DATABASE adapter_db;

\connect adapter_db;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1.1 event_type
CREATE TABLE IF NOT EXISTS event_type (
    event_type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type_code VARCHAR NOT NULL UNIQUE,
    event_type_name VARCHAR NOT NULL
);

-- 1.2 holiday_occasion
CREATE TABLE IF NOT EXISTS holiday_occasion (
    holiday_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    holiday_code VARCHAR NOT NULL UNIQUE,
    holiday_name VARCHAR NOT NULL
);

-- 1.3 account
CREATE TABLE IF NOT EXISTS account (
    account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR NOT NULL UNIQUE,
    full_name VARCHAR NOT NULL,
    email VARCHAR NOT NULL UNIQUE,
    cell_phone VARCHAR,
    area_code VARCHAR,
    language VARCHAR,
    role_id SMALLINT,
    last_login TIMESTAMPTZ,
    deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 1.4 site
CREATE TABLE IF NOT EXISTS site (
    site_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_code VARCHAR NOT NULL UNIQUE,
    area_code VARCHAR NOT NULL,
    province_code VARCHAR NOT NULL,
    longitude NUMERIC(10,6),
    latitude NUMERIC(10,6),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 1.5 cell
CREATE TABLE IF NOT EXISTS cell (
    cell_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cell_code VARCHAR NOT NULL UNIQUE,
    site_id UUID NOT NULL REFERENCES site(site_id),
    device_code VARCHAR NOT NULL,
    sector VARCHAR,
    network VARCHAR,
    vendor VARCHAR,
    ci_serving_cell INTEGER,
    lac_tac INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_cell_site_id ON cell (site_id);

-- 1.6 event
CREATE TABLE IF NOT EXISTS event (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_code VARCHAR NOT NULL UNIQUE,
    event_name VARCHAR NOT NULL,
    event_type_id UUID REFERENCES event_type(event_type_id),
    holiday_id UUID REFERENCES holiday_occasion(holiday_id),
    event_level VARCHAR,
    annual BOOLEAN NOT NULL DEFAULT false,
    is_lunar BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_event_event_type_id ON event (event_type_id);
CREATE INDEX IF NOT EXISTS idx_event_holiday_id ON event (holiday_id);

-- 1.7 event_session
CREATE TABLE IF NOT EXISTS event_session (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_event_code VARCHAR NOT NULL UNIQUE,
    event_id UUID NOT NULL REFERENCES event(event_id),
    start_date TIMESTAMPTZ NOT NULL,
    end_date TIMESTAMPTZ NOT NULL,
    lunar_start_date TIMESTAMPTZ,
    lunar_end_date TIMESTAMPTZ,
    expected_participants INTEGER,
    status VARCHAR NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_event_session_event_id ON event_session (event_id);

-- 1.8 cell_session
CREATE TABLE IF NOT EXISTS cell_session (
    cell_session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cell_id UUID NOT NULL REFERENCES cell(cell_id),
    session_id UUID NOT NULL REFERENCES event_session(session_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (cell_id, session_id)
);

CREATE INDEX IF NOT EXISTS idx_cell_session_cell_id ON cell_session (cell_id);
CREATE INDEX IF NOT EXISTS idx_cell_session_session_id ON cell_session (session_id);

-- 1.9 profile
CREATE TABLE IF NOT EXISTS profile (
    profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_name VARCHAR NOT NULL UNIQUE,
    display_name VARCHAR NOT NULL,
    display_name_ui VARCHAR,
    status VARCHAR NOT NULL DEFAULT 'ACTIVE',
    cron_expression VARCHAR,
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 1.10 profile_account
CREATE TABLE IF NOT EXISTS profile_account (
    profile_id UUID NOT NULL REFERENCES profile(profile_id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
    PRIMARY KEY (profile_id, account_id)
);

CREATE INDEX IF NOT EXISTS idx_profile_account_account_id ON profile_account (account_id);

-- 1.11 profile_event_session
CREATE TABLE IF NOT EXISTS profile_event_session (
    profile_id UUID NOT NULL REFERENCES profile(profile_id) ON DELETE CASCADE,
    session_id UUID NOT NULL REFERENCES event_session(session_id) ON DELETE CASCADE,
    PRIMARY KEY (profile_id, session_id)
);

CREATE INDEX IF NOT EXISTS idx_profile_event_session_session_id ON profile_event_session (session_id);

-- 1.12 channel
CREATE TABLE IF NOT EXISTS channel (
    channel_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    channel_name VARCHAR NOT NULL
);

-- 1.13 profile_channel
CREATE TABLE IF NOT EXISTS profile_channel (
    profile_id UUID NOT NULL REFERENCES profile(profile_id) ON DELETE CASCADE,
    channel_id UUID NOT NULL REFERENCES channel(channel_id) ON DELETE CASCADE,
    PRIMARY KEY (profile_id, channel_id)
);

CREATE INDEX IF NOT EXISTS idx_profile_channel_channel_id ON profile_channel (channel_id);

-- 1.14 template
CREATE TABLE IF NOT EXISTS template (
    template_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    channel_id UUID REFERENCES channel(channel_id),
    template_name VARCHAR NOT NULL,
    queue_name VARCHAR,
    config JSONB DEFAULT '{}',
    status VARCHAR NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_template_channel_id ON template (channel_id);

-- 1.15 notification_event
CREATE TABLE IF NOT EXISTS notification_event (
    notification_event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID REFERENCES profile(profile_id),
    event_id UUID REFERENCES event(event_id),
    raw_event_payload JSONB NOT NULL,
    status VARCHAR NOT NULL DEFAULT 'RECEIVED',
    batch_id VARCHAR(64),
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ NULL,
    published_at TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_notification_event_profile_id ON notification_event (profile_id);
CREATE INDEX IF NOT EXISTS idx_notification_event_event_id ON notification_event (event_id);
CREATE INDEX IF NOT EXISTS idx_notification_event_status_received ON notification_event (status, received_at);

-- 1.16 business_rule
CREATE TABLE IF NOT EXISTS business_rule (
    business_rule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_rule_code VARCHAR NOT NULL UNIQUE,
    business_rule_name VARCHAR NOT NULL,
    source_db_type VARCHAR,
    source_host VARCHAR,
    source_port INTEGER,
    source_database VARCHAR,
    source_schema VARCHAR,
    source_table VARCHAR,
    source_connection_ref VARCHAR,
    sql_query TEXT NOT NULL,
    description TEXT,
    status VARCHAR NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 1.17 profile_business_rule_mapping
CREATE TABLE IF NOT EXISTS profile_business_rule_mapping (
    profile_id UUID NOT NULL REFERENCES profile(profile_id) ON DELETE CASCADE,
    business_rule_id UUID NOT NULL REFERENCES business_rule(business_rule_id) ON DELETE CASCADE,
    status VARCHAR NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (profile_id, business_rule_id)
);

CREATE INDEX IF NOT EXISTS idx_pbrm_business_rule_id ON profile_business_rule_mapping (business_rule_id);

-- 1.18 account_channel
CREATE TABLE IF NOT EXISTS account_channel (
    account_channel_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES account(account_id) ON DELETE CASCADE,
    channel_id UUID NOT NULL REFERENCES channel(channel_id) ON DELETE CASCADE,
    contact_value VARCHAR NOT NULL,
    label VARCHAR,
    is_active BOOLEAN NOT NULL DEFAULT true,
    config JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_account_channel_account_id ON account_channel (account_id);
CREATE INDEX IF NOT EXISTS idx_account_channel_channel_id ON account_channel (channel_id);

-- 1.18 Logical Replication: Publication & Slot (adapter_db)
CREATE PUBLICATION adapter_config_pub FOR TABLE 
    business_rule, 
    profile_business_rule_mapping;

SELECT pg_create_logical_replication_slot('notification_config_sub', 'pgoutput');



-- =============================================================================
-- 2. NOTIFICATION SHARED DATABASE (notification_db) - Subscriber
-- =============================================================================
SELECT 'Creating database notification_db...' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_db');
CREATE DATABASE notification_db;

\connect notification_db;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2.1 notification
CREATE TABLE IF NOT EXISTS notification (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_event_id UUID,
    profile_id UUID,
    contents TEXT,
    context_data JSONB,
    status VARCHAR NOT NULL DEFAULT 'WAITING_APPROVAL',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_notification_event_id ON notification (notification_event_id);
CREATE INDEX IF NOT EXISTS idx_notification_profile_id ON notification (profile_id);

-- 2.2 routing_template
CREATE TABLE IF NOT EXISTS routing_template (
    routing_template_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    channel_id UUID,
    profile_id UUID,
    content TEXT NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2.3 recipient
CREATE TABLE IF NOT EXISTS recipient (
    recipient_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR,
    full_name VARCHAR,
    email VARCHAR,
    cell_phone VARCHAR,
    area_code VARCHAR,
    language VARCHAR,
    role_id SMALLINT,
    last_login TIMESTAMPTZ,
    deleted BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2.4 recipient_group & recipient_group_member
CREATE TABLE IF NOT EXISTS recipient_group (
    group_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_name VARCHAR NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS recipient_group_member (
    recipient_id UUID NOT NULL REFERENCES recipient(recipient_id) ON DELETE CASCADE,
    group_id UUID NOT NULL REFERENCES recipient_group(group_id) ON DELETE CASCADE,
    PRIMARY KEY (recipient_id, group_id)
);

CREATE INDEX IF NOT EXISTS idx_rgm_group_id ON recipient_group_member (group_id);

-- 2.5 notifications_task
CREATE TABLE IF NOT EXISTS notifications_task (
    task_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID NOT NULL REFERENCES notification(notification_id) ON DELETE CASCADE,
    channel_id UUID,
    recipient_id UUID NOT NULL REFERENCES recipient(recipient_id),
    routing_template_id UUID REFERENCES routing_template(routing_template_id),
    status VARCHAR NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    content TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_task_notification_id ON notifications_task (notification_id);
CREATE INDEX IF NOT EXISTS idx_task_recipient_id ON notifications_task (recipient_id);
CREATE INDEX IF NOT EXISTS idx_task_routing_template_id ON notifications_task (routing_template_id);

-- 2.6 delivery_log
CREATE TABLE IF NOT EXISTS delivery_log (
    delivery_log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES notifications_task(task_id) ON DELETE CASCADE,
    channel_code VARCHAR,
    recipient_contact VARCHAR,
    sent_at TIMESTAMPTZ,
    status VARCHAR NOT NULL,
    provider_response_code VARCHAR,
    provider_response_body TEXT,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    correlation_id UUID
);

CREATE INDEX IF NOT EXISTS idx_delivery_log_task_id ON delivery_log (task_id);
CREATE INDEX IF NOT EXISTS idx_delivery_log_correlation_id ON delivery_log (correlation_id);

-- 2.7 Replicated configuration tables from adapter_db
CREATE TABLE IF NOT EXISTS business_rule (
    business_rule_id UUID PRIMARY KEY,
    business_rule_code VARCHAR NOT NULL UNIQUE,
    business_rule_name VARCHAR NOT NULL,
    source_db_type VARCHAR,
    source_host VARCHAR,
    source_port INTEGER,
    source_database VARCHAR,
    source_schema VARCHAR,
    source_table VARCHAR,
    source_connection_ref VARCHAR,
    sql_query TEXT NOT NULL,
    description TEXT,
    status VARCHAR NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS profile_business_rule_mapping (
    profile_id UUID NOT NULL,
    business_rule_id UUID NOT NULL,
    status VARCHAR NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (profile_id, business_rule_id)
);

-- 2.8 Logical Replication: Subscription
CREATE SUBSCRIPTION notification_config_sub
CONNECTION 'host=/var/run/postgresql port=5432 dbname=adapter_db user=postgres password=postgrespassword'
PUBLICATION adapter_config_pub
WITH (create_slot = false);
