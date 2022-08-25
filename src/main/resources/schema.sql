
CREATE TABLE signalsentity (
    id VARCHAR NOT NULL PRIMARY KEY,
    type VARCHAR,
    json_string VARCHAR
);

CREATE TABLE location (
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
