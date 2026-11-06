CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_app_users_username
    ON app_users (username);

CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_categories_name
    ON categories (name);

CREATE TABLE IF NOT EXISTS movies (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(120) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL,
    age_rating INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_movies_slug
    ON movies (slug);

CREATE TABLE IF NOT EXISTS auditoriums (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_auditoriums_name
    ON auditoriums (name);

CREATE TABLE IF NOT EXISTS seats (
    id BIGSERIAL PRIMARY KEY,
    auditorium_id BIGINT NOT NULL REFERENCES auditoriums (id),
    row_number INTEGER NOT NULL,
    seat_number INTEGER NOT NULL,
    type VARCHAR(24) NOT NULL,
    CONSTRAINT uk_seat_auditorium_row_number
        UNIQUE (auditorium_id, row_number, seat_number)
);

CREATE TABLE IF NOT EXISTS screenings (
    id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL REFERENCES movies (id),
    auditorium_id BIGINT NOT NULL REFERENCES auditoriums (id),
    starts_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    base_price NUMERIC(10, 2) NOT NULL,
    status VARCHAR(24) NOT NULL,
    CONSTRAINT uk_screening_auditorium_start
        UNIQUE (auditorium_id, starts_at)
);
