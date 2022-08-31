
CREATE TABLE signalsentities (
    id VARCHAR NOT NULL PRIMARY KEY,
    type VARCHAR,
    json_string VARCHAR
);

CREATE TABLE location_types (
    id VARCHAR NOT NULL PRIMARY KEY,
    name VARCHAR,
    description VARCHAR,
    json_string VARCHAR
);

CREATE TABLE locations (
    id VARCHAR NOT NULL PRIMARY KEY,
    barcode VARCHAR,
    name VARCHAR,
    description VARCHAR,
    grid BOOLEAN,
    rows INTEGER,
    columns INTEGER,
    type_id VARCHAR,
    type_name VARCHAR,
    ancestor_id VARCHAR,
    ancestor_name VARCHAR,
    json_string VARCHAR
);

CREATE TABLE roles (
    id INTEGER NOT NULL PRIMARY KEY,
    name VARCHAR,
    description VARCHAR,
    json_string VARCHAR
);

CREATE TABLE role_privileges (
    role_id INTEGER NOT NULL REFERENCES roles(id) ON UPDATE CASCADE ON DELETE CASCADE,
    privilege VARCHAR NOT NULL
);

CREATE TABLE users (
    id INTEGER NOT NULL PRIMARY KEY,
    alias VARCHAR,
    country VARCHAR,
    created_at TIMESTAMP,
    email VARCHAR,
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    first_name VARCHAR,
    last_login_at TIMESTAMP,
    organization VARCHAR,
    user_name VARCHAR,
    json_string VARCHAR
);
