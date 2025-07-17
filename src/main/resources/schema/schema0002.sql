/*
 * IPB Signals Client
 * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

\set SIGNALS_USER signals
\set SIGNALS_DATABASE signals

\connect :SIGNALS_DATABASE :SIGNALS_USER

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
    ('EntityType', 'container'),
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

CREATE TABLE local_config (
    id  SERIAL NOT NULL PRIMARY KEY,
    entity_id VARCHAR NOT NULL,
    feature VARCHAR NOT NULL,
    value VARCHAR,
    UNIQUE (feature, entity_id)
);

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
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    template BOOLEAN NOT NULL DEFAULT FALSE
);

/*
 * Children of entities: We cannot use a foreign key for
 * child_id because the child may not yet be entered into
 * the signalsentities table, when the child_id is discovered.
 */
CREATE TABLE signalsentities_children (
    signals_entity_id VARCHAR NOT NULL REFERENCES signalsentities(id) ON UPDATE CASCADE ON DELETE CASCADE,
    child_id VARCHAR NOT NULL,
    PRIMARY KEY (signals_entity_id, child_id)
);

CREATE TABLE attribute_definitions (
    id VARCHAR NOT NULL PRIMARY KEY,
    attr_type INTEGER NOT NULL REFERENCES dyn_enums(id) ON UPDATE CASCADE ON DELETE CASCADE,
    name VARCHAR,
    description VARCHAR,
    format VARCHAR
);

CREATE TABLE attribute_values (
    id  VARCHAR NOT NULL REFERENCES attribute_definitions(id) ON UPDATE CASCADE ON DELETE CASCADE,
    value VARCHAR,
    PRIMARY KEY (id, value)
);

CREATE TABLE synonyms (
    id VARCHAR NOT NULL REFERENCES signalsentities(id) ON UPDATE CASCADE ON DELETE CASCADE,
    value VARCHAR NOT NULL,
    PRIMARY KEY (id, value)
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
    user_id VARCHAR NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    role_id VARCHAR NOT NULL REFERENCES roles(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE group_memberships (
    user_id VARCHAR NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    group_id VARCHAR NOT NULL  REFERENCES groups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_id, group_id)
);

CREATE TABLE usershares (
    entity_id   VARCHAR NOT NULL REFERENCES signalsentities(id) ON UPDATE CASCADE ON DELETE CASCADE,
    user_id VARCHAR NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    can_read BOOLEAN NOT NULL DEFAULT FALSE,
    can_write BOOLEAN NOT NULL DEFAULT FALSE,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    has_full_control BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY(entity_id, user_id)
);

CREATE TABLE groupshares (
    entity_id   VARCHAR NOT NULL REFERENCES signalsentities(id) ON UPDATE CASCADE ON DELETE CASCADE,
    group_id VARCHAR NOT NULL REFERENCES groups(id) ON UPDATE CASCADE ON DELETE CASCADE,
    can_read BOOLEAN NOT NULL DEFAULT FALSE,
    can_write BOOLEAN NOT NULL DEFAULT FALSE,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    has_full_control BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY(entity_id, group_id)
);

CREATE VIEW signalsentities_shares AS
    SELECT entity_id, user_id, bool_or(can_read) AS can_read, bool_or(can_write) AS can_write,
      bool_or(is_admin) AS is_admin, bool_or(has_full_control) AS has_full_control
    FROM (
        SELECT eus.entity_id, eus.user_id, eus.can_read AS can_read, eus.can_write AS can_write,
          eus.is_admin AS is_admin, eus.has_full_control AS has_full_control
        FROM usershares AS eus
    UNION
        SELECT egs.entity_id, gm.user_id AS user_id, egs.can_read AS can_read, egs.can_write AS can_write,
          egs.is_admin AS is_admin, egs.has_full_control AS has_full_control
        FROM groupshares AS egs JOIN group_memberships as gm ON egs.group_id = gm.group_id
    ) AS shares GROUP BY shares.entity_id, shares.user_id;


CREATE TABLE field_definitions (
    id VARCHAR NOT NULL PRIMARY KEY,
    attribute_list_eid VARCHAR,
    default_unit VARCHAR,
    defined_by VARCHAR,
    defining_entity_id VARCHAR /* NOT NULL signalsentities(id) */,
    description VARCHAR,
    designation INTEGER NOT NULL REFERENCES dyn_enums(id) ON UPDATE CASCADE ON DELETE CASCADE,
    field_type INTEGER NOT NULL REFERENCES dyn_enums(id) ON UPDATE CASCADE ON DELETE CASCADE,
    key VARCHAR,
    title VARCHAR,
    calculated BOOLEAN NOT NULL DEFAULT FALSE,
    hidden BOOLEAN NOT NULL DEFAULT FALSE,
    multiselect BOOLEAN NOT NULL DEFAULT FALSE,
    read_only BOOLEAN NOT NULL DEFAULT FALSE,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    user_defined BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE field_measures (
    field_id VARCHAR NOT NULL REFERENCES field_definitions(id) ON UPDATE CASCADE ON DELETE CASCADE,
    measure INTEGER NOT NULL,
    PRIMARY KEY (field_id, measure)
);

CREATE TABLE field_options (
    id VARCHAR NOT NULL REFERENCES field_definitions(id) ON UPDATE CASCADE ON DELETE CASCADE,
    value VARCHAR NOT NULL,
    PRIMARY KEY (id, value)
);

CREATE TABLE field_values (
    entity_id VARCHAR NOT NULL REFERENCES signalsentities(id) ON UPDATE CASCADE ON DELETE CASCADE,
    field_id VARCHAR NOT NULL REFERENCES field_definitions(id) ON UPDATE CASCADE ON DELETE CASCADE,
    value VARCHAR,
    PRIMARY KEY (entity_id, field_id)
);

CREATE TABLE attachments (
    id SERIAL NOT NULL PRIMARY KEY,
    element_id VARCHAR REFERENCES signalsentities(id) ON UPDATE CASCADE ON DELETE CASCADE,
    field_id VARCHAR REFERENCES field_definitions(id) ON UPDATE CASCADE ON DELETE CASCADE,
    ancestor_id VARCHAR NOT NULL REFERENCES signalsentities(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE attachment_revisions (
    id SERIAL NOT NULL PRIMARY KEY,
    attachment_id INTEGER NOT NULL REFERENCES attachments(id) ON DELETE CASCADE ON UPDATE CASCADE,
    original_name VARCHAR,
    mime_type VARCHAR,          /* as defined by field value */
    file_id VARCHAR,
    size BIGINT
);

CREATE TABLE attachment_files (
    id SERIAL NOT NULL PRIMARY KEY,
    revision_id INTEGER NOT NULL REFERENCES attachment_revisions(id) ON UPDATE CASCADE ON DELETE CASCADE,
    mime_type VARCHAR,
    size BIGINT,
    digest VARCHAR
);

CREATE TABLE container_types (
    id VARCHAR NOT NULL PRIMARY KEY,
    created_at TIMESTAMP,
    description VARCHAR,
    in_use BOOLEAN NOT NULL DEFAULT FALSE,
    movable BOOLEAN NOT NULL DEFAULT FALSE,
    name VARCHAR,
    updated_at TIMESTAMP
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

CREATE TABLE materials (
    id VARCHAR NOT NULL PRIMARY KEY,
    created_at TIMESTAMP,
    created_by VARCHAR /* NOT NULL REFERENCES users(id) */,
    description VARCHAR,
    digest VARCHAR,
    edited_at TIMESTAMP,
    edited_by VARCHAR /* NOT NULL REFERENCES users(id) */,
    library_id VARCHAR REFERENCES libraries(id) ON UPDATE CASCADE ON DELETE CASCADE,
    name VARCHAR,
    owner VARCHAR /* NOT NULL REFERENCES users(id) */,
    entity_type INTEGER NOT NULL REFERENCES dyn_enums(id) ON UPDATE CASCADE ON DELETE CASCADE,
    material_id VARCHAR  REFERENCES materials(id)  ON UPDATE CASCADE ON DELETE CASCADE
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
    type_name VARCHAR,
    updated_at TIMESTAMP,
    updated_by VARCHAR /* REFERENCES users(id) */,
    unit VARCHAR,
    description VARCHAR
);

CREATE TABLE location_types (
    id VARCHAR NOT NULL PRIMARY KEY,
    name VARCHAR,
    description VARCHAR,
    created_at TIMESTAMP,
    in_use BOOLEAN NOT NULL DEFAULT FALSE,
    movable BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP
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

CREATE TABLE samples (
    id VARCHAR PRIMARY KEY,
    name VARCHAR NOT NULL,
    description TEXT,
    type INTEGER,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    edited_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR,
    edited_by VARCHAR,
    owner VARCHAR,
    digest BIGINT NOT NULL,
    ancestor_id VARCHAR,
    stoicRef_id VARCHAR,
    stoicRef_row_id VARCHAR,
    parent_container_id VARCHAR,
    template_id VARCHAR
);

CREATE TABLE sample_properties (
    property_id VARCHAR PRIMARY KEY,
    property_name VARCHAR,
    property_type VARCHAR
);

CREATE TABLE sample_property_values (
    sample_id VARCHAR NOT NULL REFERENCES samples(id) ON DELETE CASCADE,
    property_id VARCHAR NOT NULL REFERENCES sample_properties(property_id) ON DELETE CASCADE,
    property_value VARCHAR,
    PRIMARY KEY (sample_id, property_id)
);

CREATE TABLE experiments (
    id VARCHAR PRIMARY KEY,
    name VARCHAR NOT NULL,
    description TEXT,
    type INTEGER,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    edited_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR,
    edited_by VARCHAR,
    owner VARCHAR,
    digest BIGINT NOT NULL,
    ancestor_id VARCHAR,
    template_id VARCHAR
);

CREATE TABLE experiment_properties (
    property_id VARCHAR PRIMARY KEY,
    property_name VARCHAR,
    property_type VARCHAR,
    template_id VARCHAR
);

CREATE TABLE experiment_property_values (
    experiment_id VARCHAR NOT NULL REFERENCES experiments(id) ON DELETE CASCADE,
    property_id VARCHAR NOT NULL REFERENCES experiment_properties(property_id) ON DELETE CASCADE,
    property_value VARCHAR,
    PRIMARY KEY (experiment_id, property_id)
);

CREATE TABLE inhouse_compounds(
    id SERIAL PRIMARY KEY,
    eid VARCHAR,
    mol_id INTEGER,
    casrn VARCHAR,
    remarks TEXT,
    ipb_code VARCHAR,
    name VARCHAR
);

CREATE TABLE inhouse_container (
    id SERIAL PRIMARY KEY,
    eid VARCHAR,
    sample_id INTEGER,
    amount DOUBLE PRECISION,
    tara DOUBLE PRECISION,
    volume DOUBLE PRECISION,
    concentration DOUBLE PRECISION,
    sample_code VARCHAR,
    purity INTEGER,
    appearance VARCHAR,
    remarks VARCHAR,
    ipb_code VARCHAR,
    last_solvent VARCHAR,
    compound_correlation_id INTEGER,
    organism_correlation_id INTEGER,
    location VARCHAR,
    location_id INTEGER,
    row INTEGER,
    container_column INTEGER
);

CREATE TABLE inhouse_correlation (
    id SERIAL PRIMARY KEY,
    context VARCHAR,
    mol_id INTEGER,
    procedure_id INTEGER,
    organism_id INTEGER
);

CREATE TABLE inhouse_experiments (
    id SERIAL PRIMARY KEY,
    eid VARCHAR,
    threelc VARCHAR,
    code VARCHAR,
    journal VARCHAR,
    proc_id INTEGER,
    remarks VARCHAR
);

CREATE TABLE inhouse_locations(
    id SERIAL PRIMARY KEY,
    eid VARCHAR,
    name VARCHAR,
    location_columns INTEGER,
    location_rows INTEGER,
    zero_based BOOLEAN
);

CREATE TABLE inhouse_synonyms(
    id SERIAL PRIMARY KEY,
    inhouse_id INTEGER,
    type VARCHAR,
    synonym VARCHAR
);

CREATE TABLE inhouse_taxonomy (
    id SERIAL PRIMARY KEY,
    eid VARCHAR,
    inhouse_id INTEGER,
    inhouse_parent_id INTEGER,
    organism_id INTEGER,
    parent VARCHAR,
    taxonomy_level VARCHAR,
    name VARCHAR

);

CREATE TABLE ados (
    id VARCHAR(255) PRIMARY KEY,
    eid VARCHAR(255),
    name VARCHAR(255),
    description TEXT,
    type INTEGER,
    ipb_code VARCHAR(50),
    mol_id VARCHAR(50),
    proc_id INTEGER,
    sample_id VARCHAR(255),
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    state VARCHAR(50),
    ancestor_id VARCHAR(255),
    template_id VARCHAR(255)
);

CREATE TABLE ado_properties (
    property_id VARCHAR(64) PRIMARY KEY,
    property_name VARCHAR(255),
    property_type VARCHAR(64),
    template_id VARCHAR(64)
);

CREATE TABLE ado_property_values (
    ado_id VARCHAR(64),
    property_id VARCHAR(64),
    property_value TEXT,
    PRIMARY KEY (ado_id, property_id),
     CONSTRAINT fk_ado_prop_val_property FOREIGN KEY (property_id)
           REFERENCES ado_properties(property_id)
           ON DELETE CASCADE,
       CONSTRAINT fk_ado_prop_val_entity FOREIGN KEY (ado_id)
           REFERENCES ados(id)
           ON DELETE CASCADE
);