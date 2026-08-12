CREATE TABLE activity_subjects (
                                   id BIGSERIAL PRIMARY KEY,
                                   name VARCHAR(255) NOT NULL UNIQUE,
                                   subject_type VARCHAR(50) NOT NULL,
                                   description VARCHAR(500),
                                   active BOOLEAN NOT NULL DEFAULT TRUE
);