-- Default role for public self-registration (POST /api/v1/auth/register).
-- No special permissions: an Employee can log in and act as themselves
-- (their own activities/reports), but has none of the admin-only
-- Create/Update/Delete/Assign permissions granted to SuperAdmin.
INSERT INTO roles (name)
VALUES ('Employee');