DO $$
BEGIN
  CREATE ROLE sa LOGIN PASSWORD 'blackduck';
  EXCEPTION WHEN DUPLICATE_OBJECT THEN
  RAISE NOTICE 'not creating role my_role -- it already exists';
END
$$;

SELECT 'CREATE DATABASE alertdb WITH OWNER sa'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname= 'alertdb');

CREATE SCHEMA IF NOT EXISTS alert;
