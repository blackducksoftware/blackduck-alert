DO
$$
    BEGIN
        -- install dblink extension
        CREATE EXTENSION IF NOT EXISTS dblink;
        -- install uuid-ossp extension
        CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
        -- create role and database if there are errors they will be logged.
        PERFORM dblink('user=test dbname=postgres', 'CREATE ROLE sa WITH SUPERUSER LOGIN ENCRYPTED PASSWORD ''blackduck'' ', FALSE);
        PERFORM dblink('user=sa dbname=postgres', 'CREATE DATABASE alertdb WITH OWNER sa', FALSE);
    END
$$
