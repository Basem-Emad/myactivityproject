-- Clean up any orphaned rows before adding the foreign key constraint
DELETE FROM activity_entries
WHERE user_id NOT IN (SELECT id FROM users);

-- Add foreign key constraint linking activity_entries to users
ALTER TABLE activity_entries
    ADD CONSTRAINT fk_activity_entries_user
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE;
