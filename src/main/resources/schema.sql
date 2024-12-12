
CREATE TABLE dyn_enums (
    id  SERIAL NOT NULL PRIMARY KEY,
    type VARCHAR NOT NULL,
    value VARCHAR NOT NULL,
    UNIQUE (type, value));

INSERT INTO dyn_enums (type, value) VALUES 
    ('EntityType', 'experiment'),
    ('EntityType', 'journal'),
    ('EntityType', 'request'),
    ('EntityType', 'asset'),
    ('EntityType', 'location'),
    ('EntityType', 'container')
    ('EntityType', 'sample'),
    ('EntityType', 'text'),
    ('EntityType', 'task'),
    ('EntityType', 'worksheet'),
    ('EntityType', 'assetType'),
    ('EntityType', 'monomer'),
    ('EntityType', 'chemicalDrawing'),
    ('EntityType', 'batch'),
    ('EntityType', 'plateContainer'),
    ('EntityType', 'plate'),
    ('EntityType', 'ado'),
    ('EntityType', 'attribute'),
    ('AttributeType', 'choice'),
    ('AttributeType', 'auto'),
    ('FieldDesignation','default'),
    ('FieldDesignation','asset'),
    ('FieldDesignation','batch');

CREATE TABLE signalsentities (
    id VARCHAR PRIMARY KEY,
    snb_type INTEGER NOT NULL REFERENCES dyn_enums(id) ON UPDATE CASCADE ON DELETE CASCADE,
    eid VARCHAR,
    name VARCHAR,
    description VARCHAR,
    created_at TIMESTAMP,
    created_by VARCHAR,
    owner VARCHAR,
    edited_at TIMESTAMP,
    edited_by VARCHAR,
    digest BIGINT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

/*
 * Children of entities: We cannot use a foreign key for
 * child_id because the child may not yet be entered into
 * the signalsentities table, when the child_id is discovered.
 */
CREATE TABLE signalsentities_children (
    signals_entity_id VARCHAR NOT NULL REFERENCES signalsentities(id),
    child_id VARCHAR NOT NULL,
    PRIMARY KEY (signals_entity_id, child_id)
);

/*
 * Ancestors of entities: We cannot use a foreign key for
 * ancestor_id because the ancestor may not yet be entered into
 * the signalsentities table, when the ancestor_id is discovered.
 */

CREATE TABLE signalsentities_ancestors (
    signals_entity_id VARCHAR NOT NULL REFERENCES signalsentities(id),
    ancestor_id VARCHAR NOT NULL,
    PRIMARY KEY (signals_entity_id, ancestor_id)
);

CREATE TABLE signalsentities_flags (
    signals_entity_id VARCHAR,
    flag_value VARCHAR,
    FOREIGN KEY (signals_entity_id) REFERENCES signalsentities(id)
);

CREATE TABLE attribute_definitions (
    id VARCHAR NOT NULL PRIMARY KEY,
    attr_type INTEGER NOT NULL REFERENCES dyn_enums(id),
    name VARCHAR,
    description VARCHAR,
    format VARCHAR
);

CREATE TABLE attribute_values (
    id  VARCHAR NOT NULL REFERENCES attribute_definitions(id),
    value VARCHAR,
    PRIMARY KEY (id, value)
);

CREATE TABLE synonyms (
    id VARCHAR NOT NULL REFERENCES signalsentities(id),
    value VARCHAR NOT NULL,
    PRIMARY KEY (id, value)
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
    defining_entity_id VARCHAR /* NOT NULL signalsentities(id) */,
    description VARCHAR,
    designation INTEGER NOT NULL REFERENCES dyn_enums(id),
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
    measure INTEGER NOT NULL,
    PRIMARY KEY (field_id, measure)
);

CREATE TABLE field_options (
    id VARCHAR NOT NULL REFERENCES field_definitions(id),
    value VARCHAR NOT NULL,
    PRIMARY KEY (id, value)
);

CREATE TABLE field_values (
    entity_id VARCHAR NOT NULL REFERENCES signalsentities(id),
    field_id VARCHAR NOT NULL REFERENCES field_definitions(id),
    value VARCHAR,
    PRIMARY KEY (entity_id, field_id)
);

CREATE TABLE attachments (
    id VARCHAR NOT NULL PRIMARY KEY,
    name VARCHAR,
    entity_type INTEGER NOT NULL REFERENCES dyn_enums(id),
    created_at TIMESTAMP,
    edited_at TIMESTAMP,
    digest VARCHAR,
    ancestor_id VARCHAR NOT NULL
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
    id VARCHAR NOT NULL REFERENCES container_types (id),
    value VARCHAR NOT NULL REFERENCES field_definitions (id),
    PRIMARY KEY (id, value)
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
    uniqueness VARCHAR,
    has_image BOOLEAN NOT NULL DEFAULT FALSE,
    has_sequence BOOLEAN NOT NULL DEFAULT FALSE,
    has_drawing BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE library_fields (
    id VARCHAR NOT NULL REFERENCES libraries (id),
    value VARCHAR NOT NULL REFERENCES field_definitions (id),
    PRIMARY KEY (id, value)
);

CREATE TABLE materials (
    id VARCHAR NOT NULL PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR /* NOT NULL REFERENCES users(id) */,
    description VARCHAR,
    digest VARCHAR,
    edited_at TIMESTAMP,
    edited_by VARCHAR /* NOT NULL REFERENCES users(id) */,
    library_id VARCHAR REFERENCES libraries(id),
    name VARCHAR,
    owner VARCHAR /* NOT NULL REFERENCES users(id) */
);

CREATE TABLE material_batches (
    id VARCHAR NOT NULL PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR /* NOT NULL REFERENCES users(id) */,
    description VARCHAR,
    digest VARCHAR,
    edited_at TIMESTAMP,
    edited_by VARCHAR /* NOT NULL REFERENCES users(id) */,
    material_id VARCHAR REFERENCES materials(id),
    name VARCHAR,
    owner VARCHAR /* NOT NULL REFERENCES users(id) */
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
