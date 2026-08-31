CREATE TABLE activity_entries (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,       -- temporary: will become FK to users table
    date        DATE         NOT NULL,
    start_time  TIME         NOT NULL,
    end_time    TIME         NOT NULL,
    duration_minutes    INTEGER      NOT NULL,        -- exact duration in minutes
    activity_type_id    BIGINT NOT NULL REFERENCES activity_types(id),
    activity_subject_id BIGINT NOT NULL REFERENCES activity_subjects(id),
    task_description    TEXT   NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Index for the overlap query: "find all entries for this user on this date"
CREATE INDEX idx_activity_entries_user_date
    ON activity_entries (user_id, date);
