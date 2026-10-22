ALTER TABLE cinema_orders ADD COLUMN cancelled_at TIMESTAMP(6) WITH TIME ZONE;

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(40) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    username VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    category_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    delivered_at TIMESTAMP(6) WITH TIME ZONE,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error VARCHAR(1000)
);

CREATE INDEX ix_outbox_pending ON outbox_events (created_at) WHERE delivered_at IS NULL;
