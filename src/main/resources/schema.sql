
CREATE TABLE signalsentities (
    id VARCHAR NOT NULL PRIMARY KEY,
    snb_type VARCHAR,
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
    grid_rows INTEGER,
    grid_columns INTEGER,
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
    last_name VARCHAR,
    organization VARCHAR,
    user_name VARCHAR,
    json_string VARCHAR
);

CREATE TABLE groups (
    id INTEGER NOT NULL PRIMARY KEY,
    created_at TIMESTAMP,
    edited_at TIMESTAMP,
    description VARCHAR,
    digest VARCHAR,
    name VARCHAR,
    is_system BOOLEAN,
    snb_type VARCHAR,
    json_string VARCHAR
);

CREATE TABLE field_definitions (
    id VARCHAR NOT NULL PRIMARY KEY,
    attribute_list_eid VARCHAR,
    default_unit VARCHAR,
    description VARCHAR,
    field_type VARCHAR,
    hidden VARCHAR,
    key VARCHAR,
    required VARCHAR,
    title VARCHAR,
    user_defined VARCHAR
);

CREATE TABLE field_measures (
    field_id VARCHAR NOT NULL REFERENCES field_definitions(id),
    measure VARCHAR NOT NULL
);

CREATE TABLE attachments (
    id VARCHAR NOT NULL PRIMARY KEY,
    created_at TIMESTAMP,
    entity_id VARCHAR,
    entity_type VARCHAR,
    file_name VARCHAR,
    template BOOLEAN,
    updated_at TIMESTAMP
    version_id VARCHAR
);
