ALTER TABLE users
    ADD CONSTRAINT uk_users_user_name UNIQUE (user_name);