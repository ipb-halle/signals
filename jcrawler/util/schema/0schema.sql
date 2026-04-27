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
    UNIQUE (path_id, name)
);


CREATE TABLE aces (
    id                      BIGSERIAL NOT NULL PRIMARY KEY,
    file_id                 BIGINT NOT NULL REFERENCES files(id) ON UPDATE CASCADE ON DELETE CASCADE,
    ace_index               INTEGER,
/* ACE TYPE */
    type                    INTEGER,
/* ACE FLAGS */
    f_group                 BOOLEAN NOT NULL DEFAULT FALSE,
    f_directory_inherit     BOOLEAN NOT NULL DEFAULT FALSE,
    f_nopropagate_inherit   BOOLEAN NOT NULL DEFAULT FALSE,
    f_inherit_only          BOOLEAN NOT NULL DEFAULT FALSE,
    f_success_access        BOOLEAN NOT NULL DEFAULT FALSE,
    f_fail_access           BOOLEAN NOT NULL DEFAULT FALSE,
/* ACE PERMISSIONS */
    p_read_data             BOOLEAN NOT NULL DEFAULT FALSE,
    p_write_data            BOOLEAN NOT NULL DEFAULT FALSE,
    p_append_data           BOOLEAN NOT NULL DEFAULT FALSE,
    p_execute               BOOLEAN NOT NULL DEFAULT FALSE,
    p_delete                BOOLEAN NOT NULL DEFAULT FALSE,
    p_delete_child          BOOLEAN NOT NULL DEFAULT FALSE,
    p_read_attr             BOOLEAN NOT NULL DEFAULT FALSE,
    p_write_attr            BOOLEAN NOT NULL DEFAULT FALSE,
    p_read_named_attr       BOOLEAN NOT NULL DEFAULT FALSE,
    p_write_named_attr      BOOLEAN NOT NULL DEFAULT FALSE,
    p_read_acl              BOOLEAN NOT NULL DEFAULT FALSE,
    p_write_acl             BOOLEAN NOT NULL DEFAULT FALSE,
    p_write_owner           BOOLEAN NOT NULL DEFAULT FALSE,
    p_synchronize           BOOLEAN NOT NULL DEFAULT FALSE,
/* ACE PRINCIPAL */
    principal               INTEGER NOT NULL REFERENCES principals(id) ON UPDATE CASCADE ON DELETE CASCADE,
    UNIQUE(file_id, ace_index)
);
