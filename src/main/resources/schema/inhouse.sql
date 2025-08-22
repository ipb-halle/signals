/*
 * Database tables for the InhouseDB migration project
 * do NOT include in test code
 */
CREATE TABLE inhouse_compounds (
    id  SERIAL NOT NULL PRIMARY KEY,
    eid VARCHAR,
    mol_id INTEGER NOT NULL,
    casrn VARCHAR,
    remarks VARCHAR,
    ipb_code VARCHAR,
    name VARCHAR,
    UNIQUE (mol_id)
);


CREATE TABLE inhouse_experiments (
    id SERIAL NOT NULL PRIMARY KEY,
    eid VARCHAR,
    threelc VARCHAR,
    code VARCHAR,
    journal VARCHAR,
    proc_id INTEGER,
    remarks VARCHAR,
    import_successful BOOLEAN DEFAULT FALSE,
    UNIQUE(threelc,code),
    UNIQUE(proc_id)
);



CREATE TABLE inhouse_organisms (
    id SERIAL PRIMARY KEY,
    org_id INTEGER,
    species_script_id VARCHAR,
    strain_script_id VARCHAR,
    remarks VARCHAR
);

CREATE TABLE inhouse_extract (
    id SERIAL PRIMARY KEY,
    eid VARCHAR,
    extract_id INTEGER,
    corr_orgproc_id INTEGER,
    last_solvent VARCHAR,
    storage_place VARCHAR,
    extract_code VARCHAR,
    tara VARCHAR,
    amount DECIMAL(10, 3),
    volume DECIMAL(10, 3),
    concentration DECIMAL(10, 3),
    solution BOOLEAN,
    remarks VARCHAR,
    hplc VARCHAR,
    extract_plate_id VARCHAR,
    extract_position VARCHAR,
    extract_barcode VARCHAR,
    ipb_code VARCHAR
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
    corr_id INTEGER,
    context VARCHAR,
    mol_id INTEGER,
    proc_id INTEGER,
    org_id INTEGER
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

