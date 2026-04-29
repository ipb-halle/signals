/*
 *
 */
CREATE USER spider PASSWORD 'spider';
CREATE DATABASE spider WITH ENCODING 'UTF8' OWNER spider;

\connect spider spider

CREATE TABLE namespaces (
    id      SERIAL NOT NULL PRIMARY KEY,
    name    VARCHAR NOT NULL COLLATE "C",
    UNIQUE (name)
);

CREATE TABLE directories (
    id                      BIGSERIAL NOT NULL PRIMARY KEY,
    namespace_id            INTEGER NOT NULL REFERENCES namespaces(id) ON UPDATE CASCADE ON DELETE CASCADE,
    path                    VARCHAR NOT NULL COLLATE "C",
    new_entries             BIGINT NOT NULL DEFAULT 0,
    changed_entries         BIGINT NOT NULL DEFAULT 0,
    vanished_entries        BIGINT NOT NULL DEFAULT 0,
    accumulated_sizes       BIGINT NOT NULL DEFAULT 0,
    change_time             TIMESTAMP NOT NULL DEFAULT now(),
    missing                 BOOLEAN NOT NULL DEFAULT false,
    UNIQUE (namespace_id, path)
);

CREATE TABLE principals (
    id                  SERIAL NOT NULL PRIMARY KEY,
    principal           VARCHAR NOT NULL,
    is_group            BOOLEAN,
    is_everyone         BOOLEAN,
    guid                INTEGER
);

INSERT INTO principals (principal, is_group, is_everyone, guid) VALUES
    ('OWNER@',    false, false, NULL),
    ('GROUP@',    true,  false, NULL),
    ('EVERYONE@', false, true,  NULL);

CREATE TABLE acls (
    id                  BIGSERIAL NOT NULL PRIMARY KEY,
    raw_attribute       BYTEA NOT NULL,
    UNIQUE (raw_attribute)
);

CREATE TABLE acl_details (
    id                  BIGSERIAL NOT NULL PRIMARY KEY,
    acl_id              BIGINT REFERENCES acls(id) ON UPDATE CASCADE ON DELETE CASCADE,
    seq                 INTEGER,
    principal_id        INTEGER NOT NULL REFERENCES principals(id) ON UPDATE CASCADE ON DELETE CASCADE,
    type                INTEGER,
    file_inherit        BOOLEAN NOT NULL DEFAULT FALSE,
    dir_inherit         BOOLEAN NOT NULL DEFAULT FALSE,
    no_propagate        BOOLEAN NOT NULL DEFAULT FALSE,
    inherit_only        BOOLEAN NOT NULL DEFAULT FALSE,
    read_data           BOOLEAN NOT NULL DEFAULT FALSE,
    write_data          BOOLEAN NOT NULL DEFAULT FALSE,
    append_data         BOOLEAN NOT NULL DEFAULT FALSE,
    execute             BOOLEAN NOT NULL DEFAULT FALSE,
    delete              BOOLEAN NOT NULL DEFAULT FALSE,
    delete_child        BOOLEAN NOT NULL DEFAULT FALSE,
    read_attr           BOOLEAN NOT NULL DEFAULT FALSE,
    write_attr          BOOLEAN NOT NULL DEFAULT FALSE,
    read_named_attr     BOOLEAN NOT NULL DEFAULT FALSE,
    write_named_attr    BOOLEAN NOT NULL DEFAULT FALSE,
    read_acl            BOOLEAN NOT NULL DEFAULT FALSE,
    write_acl           BOOLEAN NOT NULL DEFAULT FALSE,
    write_owner         BOOLEAN NOT NULL DEFAULT FALSE,
    synchronize         BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (acl_id, seq)
);

CREATE TABLE files (
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    name        VARCHAR NOT NULL COLLATE "C",
    path_id     BIGINT NOT NULL REFERENCES directories(id) ON UPDATE CASCADE ON DELETE CASCADE,
    size        BIGINT,
    type        INTEGER,
    mode        INTEGER,
    uid         INTEGER REFERENCES principals(id),
    gid         INTEGER REFERENCES principals(id),
    atime       TIMESTAMP,
    mtime       TIMESTAMP,
    ctime       TIMESTAMP,
    digest      BYTEA,
    link_target VARCHAR,
    missing     BOOLEAN NOT NULL DEFAULT false,
    acl_id      BIGINT REFERENCES acls(id),
    UNIQUE (path_id, name)
);

