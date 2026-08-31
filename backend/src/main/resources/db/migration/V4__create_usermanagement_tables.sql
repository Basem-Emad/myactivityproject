CREATE TABLE roles
(
    id BIGSERIAL,
    name character varying(255) NOT NULL,
    CONSTRAINT roles_pkey PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE users
(
    id BIGSERIAL,
    date_of_birth date,
    email character varying(255),
    gender smallint,
    password character varying(255) NOT NULL,
    user_name character varying(255),
    role_id bigint,

    CONSTRAINT users_pkey PRIMARY KEY (id),

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles (id)
        ON DELETE NO ACTION,

    CONSTRAINT users_gender_check
        CHECK (gender >= 0 AND gender <= 1)
);

CREATE TABLE permissions
(
    id BIGSERIAL,
    name character varying(255) NOT NULL,

    CONSTRAINT permissions_pkey PRIMARY KEY (id),
    CONSTRAINT uk_permissions_name UNIQUE (name)
);

CREATE TABLE role_permissions
(
    role_id bigint NOT NULL,
    permission_id bigint NOT NULL,

    CONSTRAINT role_permissions_pkey
        PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES roles (id),

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions (id)
);