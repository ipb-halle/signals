/*
 * Toolkit database
 *
 * If you maintain more than a single instance (e.g. production and trial), 
 * you need to set up a database for each of these instances
 */
CREATE USER toolkit PASSWORD 'toolkit';
CREATE DATABASE toolkit WITH ENCODING 'UTF8' OWNER toolkit;

/*
 * \connect toolkit
 */
