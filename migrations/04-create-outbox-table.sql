CREATE TABLE IF NOT EXISTS outbox_event (
    id            BIGSERIAL PRIMARY KEY,
    payload       JSONB NOT NULL,
    topic         VARCHAR(255) NOT NULL,
    status        VARCHAR(20) NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_retry_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_status_pending ON outbox_event(status) WHERE status = 'PENDING';
