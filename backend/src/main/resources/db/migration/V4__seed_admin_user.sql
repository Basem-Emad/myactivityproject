INSERT INTO users (name, email, password, role, active, created_at, updated_at)
VALUES (
           'Admin',
           'admin@myproject.com',
           '$2a$10$R.d4YMiEDk0YsZ.2Csyibegso7DPeZBbKthSkKO5G8cchGcUPX13',
           'ADMIN',
           true,
           now(),
           now()
       );