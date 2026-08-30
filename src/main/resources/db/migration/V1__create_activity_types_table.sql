CREATE TABLE activity_types (
                                id BIGSERIAL PRIMARY KEY,
                                name VARCHAR(255) NOT NULL UNIQUE,
                                description VARCHAR(500),
                                active BOOLEAN NOT NULL DEFAULT TRUE
);