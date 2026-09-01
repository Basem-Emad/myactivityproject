INSERT INTO permissions (name)
VALUES
    ('Create_User'),
    ('Update_User'),
    ('Delete_User'),
	('Create_Role'),
	('Update_Role'),
	('Delete_Role'),
	 ('Read_User'),
	('Read_Role'),
   ('Assign_Permissions')
	;
	INSERT INTO public.roles(name)
    	VALUES ('SuperAdmin');

INSERT INTO role_permissions (role_id, permission_id)
        SELECT r.id, p.id
        FROM roles r
        CROSS JOIN permissions p
        WHERE r.name = 'SuperAdmin';

INSERT INTO users (
    date_of_birth,
    email,
    gender,
    password,
    user_name,
    role_id
)
SELECT
    '2000-01-01',
    'admin@example.com',
    0,
    '$2a$10$7lBDW/qPHSr4vvFvd.aiy.gV0OI2H6HyBPAKmfX1bIKNog8TycTMC',
    'admin',
    r.id
FROM roles r
WHERE r.name = 'SuperAdmin';