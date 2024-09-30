\set SIGNALS_SCHEMA signals
\set SIGNALS_DATABASE signals
\set SIGNALS_USER signals
\set SIGNALS_PW signals
--  quoted stuff --
\set SIGNALS_DATABASE_QUOTED '\'' :SIGNALS_DATABASE '\''
\set SIGNALS_PW_QUOTED '\'' :SIGNALS_PW '\''

/*
 *=========================================================
 *
 * terminate all database sessions to get
 * exclusive access
 *
 * SELECT pg_terminate_backend(pg_stat_activity.pid)
 * FROM pg_stat_activity
 * WHERE pg_stat_activity.datname = :SIGNALS_DATABASE_QUOTED
 *   AND pid <> pg_backend_pid();
 */

/*
 * clean up
 *
 * -- the following statement fails if SIGNALS_USER is not known!
 * -- REASSIGN OWNED BY :SIGNALS_USER TO postgres;
 * DROP SCHEMA IF EXISTS :SIGNALS_SCHEMA CASCADE;
 * DROP DATABASE IF EXISTS :SIGNALS_DATABASE;
 * DROP USER IF EXISTS :SIGNALS_USER;
 */

/*
 * (re-)create database objects
 */
-- roles --
CREATE USER :SIGNALS_USER PASSWORD :SIGNALS_PW_QUOTED;

/*
 * -- db --
 * CREATE DATABASE :SIGNALS_DATABASE WITH ENCODING 'UTF8' OWNER :SIGNALS_USER;
 */

\connect :SIGNALS_DATABASE

-- schema --
CREATE SCHEMA AUTHORIZATION :SIGNALS_USER;

-- adjust schema search path --
ALTER USER :SIGNALS_USER SET search_path to :SIGNALS_SCHEMA,public;

GRANT USAGE ON SCHEMA :SIGNALS_SCHEMA, public TO :SIGNALS_USER;
GRANT CONNECT, TEMPORARY, TEMP  ON  DATABASE :SIGNALS_DATABASE to :SIGNALS_USER;
GRANT SELECT, UPDATE, INSERT, DELETE ON ALL TABLES IN SCHEMA :SIGNALS_SCHEMA to :SIGNALS_USER;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public to :SIGNALS_USER;
REVOKE ALL ON ALL TABLES IN SCHEMA :SIGNALS_SCHEMA FROM public;

\connect - :SIGNALS_USER

BEGIN TRANSACTION;

-- tables --

CREATE TABLE signalsentities (
    id VARCHAR NOT NULL PRIMARY KEY,
    snb_type VARCHAR,
    eid VARCHAR,
    name VARCHAR,
    description VARCHAR,
    created_at TIMESTAMP,
    created_by VARCHAR,
    owner VARCHAR,
    edited_at TIMESTAMP,
    edited_by VARCHAR,
    digest BIGINT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
);

CREATE TABLE signalsentities_children (
    signals_entity_id VARCHAR,
    child_id VARCHAR,
    PRIMARY KEY (signals_entity_id, child_id),
    FOREIGN KEY (signals_entity_id) REFERENCES signalsentities(id) ON DELETE CASCADE
);

CREATE TABLE signalsentities_flags (
    signals_entity_id VARCHAR,
    flag_value VARCHAR,
    PRIMARY KEY (signals_entity_id, flag_value),
    FOREIGN KEY (signals_entity_id) REFERENCES signalsentities(id) ON DELETE CASCADE
);

CREATE TABLE location_types (
    id VARCHAR NOT NULL PRIMARY KEY,
    name VARCHAR,
    description VARCHAR
);

CREATE TABLE locations (
    id VARCHAR NOT NULL PRIMARY KEY,
    ancestor_id VARCHAR,
    ancestor_name VARCHAR,
    barcode VARCHAR,
    created_at DATE,
    created_by VARCHAR /* REFERENCES users(id) */,
    name VARCHAR,
    description VARCHAR,
    grid BOOLEAN,
    grid_rows INTEGER,
    grid_columns INTEGER,
    type_id VARCHAR,
    type_name VARCHAR,
    updated_at DATE,
    updated_by VARCHAR /* REFERENCES users(id) */
);

CREATE TABLE roles (
    id VARCHAR NOT NULL PRIMARY KEY,
    name VARCHAR,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR,
    ldap_role BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (name)
);

CREATE TABLE role_priv_defs (
    id VARCHAR NOT NULL PRIMARY KEY
);

CREATE TABLE role_privileges (
    role_id VARCHAR NOT NULL REFERENCES roles(id) ON UPDATE CASCADE ON DELETE CASCADE,
    privilege VARCHAR NOT NULL REFERENCES role_priv_defs(id) ON UPDATE CASCADE ON DELETE CASCADE,
    UNIQUE (role_id, privilege)
);

CREATE TABLE users (
    id VARCHAR NOT NULL PRIMARY KEY,
    alias VARCHAR,
    country VARCHAR,
    created_at TIMESTAMP,
    email VARCHAR,
    mutable BOOLEAN,
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    first_name VARCHAR,
    last_login_at TIMESTAMP,
    last_name VARCHAR,
    organization VARCHAR,
    user_name VARCHAR,
    UNIQUE (user_name)
);

CREATE TABLE groups (
    id VARCHAR NOT NULL PRIMARY KEY,
    created_at TIMESTAMP,
    edited_at TIMESTAMP,
    description VARCHAR,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    digest VARCHAR,
    ldap_group BOOLEAN NOT NULL DEFAULT FALSE,
    name VARCHAR,
    is_system BOOLEAN,
    snb_type VARCHAR,
    UNIQUE (name)
);

CREATE TABLE user_roles (
    user_id VARCHAR NOT NULL REFERENCES users(id),
    role_id VARCHAR NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE group_memberships (
    user_id VARCHAR NOT NULL REFERENCES users(id),
    group_id VARCHAR NOT NULL  REFERENCES groups(id),
    PRIMARY KEY (user_id, group_id)
);

CREATE TABLE field_definitions (
    id VARCHAR NOT NULL PRIMARY KEY,
    attribute_list_eid VARCHAR,
    calculated BOOLEAN,
    default_unit VARCHAR,
    defined_by VARCHAR,
    description VARCHAR,
    field_type VARCHAR,
    hidden VARCHAR,
    key VARCHAR,
    multiselect BOOLEAN,
    read_only BOOLEAN,
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

CREATE TABLE libraries (
    id VARCHAR NOT NULL PRIMARY KEY,
    asset_display_name VARCHAR,
    asset_name_field_id VARCHAR,
    asset_numbering_format VARCHAR,
    batch_display_name VARCHAR,
    batch_numbering VARCHAR,
    created_at TIMESTAMP,
    created_by VARCHAR,
    digest VARCHAR,
    display_image VARCHAR,
    display_table VARCHAR,
    edited_at TIMESTAMP,
    edited_by VARCHAR,
    enabled BOOLEAN,
    entity_flags VARCHAR,
    materials_sample_mapping VARCHAR,
    name VARCHAR,
    uniqueness VARCHAR
);

CREATE TABLE library_fields (
    library_id VARCHAR NOT NULL REFERENCES libraries (id),
    field_definition_id VARCHAR NOT NULL REFERENCES field_definitions (id),
    type VARCHAR NOT NULL,
    PRIMARY KEY (library_id, field_definition_id, type)
);

CREATE TABLE materials (
    id VARCHAR NOT NULL PRIMARY KEY
);

CREATE TABLE containers (
    id VARCHAR NOT NULL PRIMARY KEY,
    amount FLOAT,
    barcode VARCHAR,
    container_type_id VARCHAR /* REFERENCES container_types(id) */,
    coordinate_x INTEGER,
    coordinate_y INTEGER,
    created_at TIMESTAMP,
    created_by VARCHAR /* REFERENCES users(id) */,
    digest VARCHAR,
    location_id VARCHAR,
    material_id VARCHAR /* REFERENCES materials(id) */,
    name VARCHAR,
    updated_at TIMESTAMP,
    updated_by VARCHAR /* REFERENCES users(id) */,
    unit VARCHAR
);

-- done --

COMMIT;

