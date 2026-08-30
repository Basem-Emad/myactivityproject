CREATE TABLE activity_entries (
                                  id BIGSERIAL PRIMARY KEY,
                                  user_id BIGINT NOT NULL,
                                  activity_type_id BIGINT NOT NULL,
                                  activity_subject_id BIGINT NOT NULL,
                                  activity_date DATE NOT NULL,
                                  start_time TIME NOT NULL,
                                  end_time TIME NOT NULL,
                                  duration_minutes INTEGER NOT NULL,
                                  task_description TEXT NOT NULL,
                                  created_at TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP,

                                  CONSTRAINT fk_activity_entries_user
                                      FOREIGN KEY (user_id) REFERENCES users(id),
                                  CONSTRAINT fk_activity_entries_activity_type
                                      FOREIGN KEY (activity_type_id) REFERENCES activity_types(id),
                                  CONSTRAINT fk_activity_entries_activity_subject
                                      FOREIGN KEY (activity_subject_id) REFERENCES activity_subjects(id)
);