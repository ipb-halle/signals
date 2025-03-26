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

CREATE TABLE inhouse_compound_synonyms (
    id  SERIAL NOT NULL PRIMARY KEY,
    mol_id INTEGER NOT NULL,
    synonym VARCHAR
);

