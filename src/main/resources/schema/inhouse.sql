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

CREATE TABLE inhouse_experiments (
    id SERIAL NOT NULL PRIMARY KEY,
    eid VARCHAR,
    threelc VARCHAR,
    code VARCHAR,
    journal VARCHAR,
    proc_id INTEGER,
    remarks VARCHAR,
    UNIQUE(threelc,code),
    UNIQUE(proc_id)
);

