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
 *
 *=====================================================================
 *
 * Global variables
 *
 */
\set SIGNALS_DATABASE signals
\set SIGNALS_PASSWORD signals
\set SIGNALS_SCHEMA signals
\set SIGNALS_USER signals

-- quoted variables --
\set SIGNALS_DATABASE_QUOTED '\'' :SIGNALS_DATABASE '\''
\set SIGNALS_PASSWORD_QUOTED '\'' :SIGNALS_PASSWORD '\''


/*
 * terminate all sessions from database
 */
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = :SIGNALS_DATABASE_QUOTED
  AND pid <> pg_backend_pid();

/*
 * clean up
 */
--  REASSIGN OWNED BY :SIGNALS_USER TO postgres;
DROP SCHEMA IF EXISTS :SIGNALS_SCHEMA CASCADE;
DROP DATABASE IF EXISTS :SIGNALS_DATABASE;
DROP USER IF EXISTS :SIGNALS_USER;

/*
 * (re-)create database objects
 */
-- roles --
CREATE USER :SIGNALS_USER PASSWORD :SIGNALS_PASSWORD_QUOTED;
-- db --
CREATE DATABASE :SIGNALS_DATABASE WITH ENCODING 'UTF8' OWNER :SIGNALS_USER;

\connect :SIGNALS_DATABASE

-- schema --
CREATE SCHEMA AUTHORIZATION :SIGNALS_USER;

-- adjust schema search path --
ALTER USER :SIGNALS_USER SET search_path to :SIGNALS_SCHEMA,public;

-- privileges --
GRANT USAGE ON SCHEMA :SIGNALS_SCHEMA, public TO :SIGNALS_USER;
GRANT CONNECT, TEMPORARY, TEMP  ON  DATABASE :SIGNALS_DATABASE to :SIGNALS_USER;
GRANT SELECT, UPDATE, INSERT, DELETE ON ALL TABLES IN SCHEMA :SIGNALS_SCHEMA to :SIGNALS_USER;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public to :SIGNALS_USER;
REVOKE ALL ON ALL TABLES IN SCHEMA :SIGNALS_SCHEMA FROM public;

-- check for usefull extensions and install it --
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

/*
 * \connect - :SIGNALS_USER
 */
