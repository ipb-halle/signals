
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
    multiselect BOOLEAN,
    required VARCHAR,
    title VARCHAR,
    user_defined VARCHAR
);

CREATE TABLE field_measures (
    field_id VARCHAR NOT NULL REFERENCES field_definitions(id),
    measure VARCHAR NOT NULL,
    PRIMARY KEY (field_id, measure)
);

CREATE TABLE field_options (
    field_id VARCHAR NOT NULL REFERENCES field_definitions(id),
    option VARCHAR NOT NULL,
    PRIMARY KEY (field_id, option)
);

CREATE TABLE attachments (
    id VARCHAR NOT NULL PRIMARY KEY,
    created_at TIMESTAMP,
    entity_id VARCHAR,
    entity_type VARCHAR,
    file_name VARCHAR,
    template BOOLEAN,
    updated_at TIMESTAMP,
    version_id VARCHAR
);

CREATE TABLE container_types (
    id VARCHAR NOT NULL PRIMARY KEY,
    created_at TIMESTAMP,
    description VARCHAR,
    in_use BOOLEAN,
    movable BOOLEAN,
    name VARCHAR,
    json_string VARCHAR,
    updated_at TIMESTAMP
);

CREATE TABLE container_type_attachments (
    container_type_id VARCHAR NOT NULL REFERENCES container_types (id),
    attachment_id VARCHAR NOT NULL REFERENCES attachments (id),
    PRIMARY KEY (container_type_id, attachment_id)
);

CREATE TABLE container_type_fields (
    container_type_id VARCHAR NOT NULL REFERENCES container_types (id),
    field_definition_id VARCHAR NOT NULL REFERENCES field_definitions (id),
    PRIMARY KEY (container_type_id, field_definition_id)
);

