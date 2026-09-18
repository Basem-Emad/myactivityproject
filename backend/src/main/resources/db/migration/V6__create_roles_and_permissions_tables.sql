-- Fixed permission catalog. Permissions are seeded once and managed only via migrations,
-- not via API, to keep the authorization model predictable.
CREATE TABLE permissions (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

-- Roles are dynamic: an admin can create new roles and attach existing permissions to them
-- through the Role API, without any code change or redeploy.
CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Permission catalog
INSERT INTO permissions (name) VALUES
    ('USER_READ'),
    ('USER_CREATE'),
    ('USER_UPDATE'),
    ('USER_DELETE'),
    ('MASTERDATA_MANAGE'),
    ('ACTIVITY_VIEW_TEAM');

-- Default roles (matching the User Roles table in the requirements document)
INSERT INTO roles (name) VALUES ('EMPLOYEE'), ('MANAGER'), ('ADMIN');

-- EMPLOYEE gets no extra permissions: an employee can always manage their OWN
-- activity entries regardless of permissions, this is enforced by ownership checks
-- in the service layer, not by the permission system.

-- MANAGER can view (not necessarily edit) the team's activities and dashboard.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'MANAGER' AND p.name = 'ACTIVITY_VIEW_TEAM';

-- ADMIN gets full user management + master data management + team visibility.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.name IN (
    'USER_READ', 'USER_CREATE', 'USER_UPDATE', 'USER_DELETE',
    'MASTERDATA_MANAGE', 'ACTIVITY_VIEW_TEAM'
);
