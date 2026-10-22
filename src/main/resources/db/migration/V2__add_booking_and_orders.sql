CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    available BOOLEAN NOT NULL,
    CONSTRAINT uk_products_name UNIQUE (name),
    CONSTRAINT ck_products_price_positive CHECK (price > 0)
);

CREATE TABLE cinema_orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users (id),
    hold_id VARCHAR(36) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    status VARCHAR(24) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    confirmed_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_order_user_idempotency UNIQUE (user_id, idempotency_key),
    CONSTRAINT uk_order_hold UNIQUE (hold_id),
    CONSTRAINT ck_order_total_non_negative CHECK (total_amount >= 0)
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES cinema_orders (id),
    screening_id BIGINT NOT NULL REFERENCES screenings (id),
    seat_id BIGINT NOT NULL REFERENCES seats (id),
    price_snapshot NUMERIC(10, 2) NOT NULL,
    status VARCHAR(24) NOT NULL,
    CONSTRAINT ck_ticket_price_positive CHECK (price_snapshot > 0)
);

CREATE UNIQUE INDEX uk_ticket_active_screening_seat
    ON tickets (screening_id, seat_id)
    WHERE status = 'CONFIRMED';

CREATE INDEX ix_tickets_order ON tickets (order_id);
CREATE INDEX ix_orders_user_created ON cinema_orders (user_id, created_at DESC);

CREATE TABLE order_products (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES cinema_orders (id),
    product_id BIGINT NOT NULL REFERENCES products (id),
    quantity INTEGER NOT NULL,
    unit_price_snapshot NUMERIC(10, 2) NOT NULL,
    CONSTRAINT uk_order_product UNIQUE (order_id, product_id),
    CONSTRAINT ck_order_product_quantity_positive CHECK (quantity > 0),
    CONSTRAINT ck_order_product_price_positive CHECK (unit_price_snapshot > 0)
);
