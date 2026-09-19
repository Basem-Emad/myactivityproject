ALTER TABLE users ADD COLUMN role_id BIGINT;

-- Migrate existing string roles (e.g. the seeded admin from V4) to the new role_id FK.
UPDATE users u
SET role_id = r.id
FROM roles r
WHERE u.role = r.name;

ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id);

ALTER TABLE users DROP COLUMN role;
