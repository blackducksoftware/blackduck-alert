DO $$
BEGIN
    -- install dblink extension
    CREATE EXTENSION IF NOT EXISTS dblink;
    -- create role and database if there are errors they will be logged.
    PERFORM dblink('user=test dbname=postgres', 'CREATE ROLE sa LOGIN PASSWORD ''blackduck'' ', FALSE);
    PERFORM dblink('user=test dbname=postgres', 'CREATE DATABASE alertdb WITH OWNER sa', FALSE);
    PERFORM dblink('user=test dbname=postgres', 'CREATE SCHEMA IF NOT EXISTS alert AUTHORIZATION sa', FALSE);
END
$$;
