\COPY ALERT.SYSTEM_STATUS FROM '/opt/blackduck/alert/alert-config/data/temp/SYSTEM_STATUS.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.SYSTEM_STATUS', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.SYSTEM_STATUS;
\COPY ALERT.SYSTEM_MESSAGES FROM '/opt/blackduck/alert/alert-config/data/temp/SYSTEM_MESSAGES.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.SYSTEM_MESSAGES', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.SYSTEM_MESSAGES;
\COPY ALERT.SETTINGS_KEY FROM '/opt/blackduck/alert/alert-config/data/temp/SETTINGS_KEY.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.SETTINGS_KEY', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.SETTINGS_KEY;

\COPY ALERT.RAW_NOTIFICATION_CONTENT FROM '/opt/blackduck/alert/alert-config/data/temp/RAW_NOTIFICATION_CONTENT.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.RAW_NOTIFICATION_CONTENT', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.RAW_NOTIFICATION_CONTENT;
\COPY ALERT.AUDIT_ENTRIES FROM '/opt/blackduck/alert/alert-config/data/temp/AUDIT_ENTRIES.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.AUDIT_ENTRIES', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.AUDIT_ENTRIES;
\COPY ALERT.AUDIT_NOTIFICATION_RELATION FROM '/opt/blackduck/alert/alert-config/data/temp/AUDIT_NOTIFICATION_RELATION.csv' DELIMITER ',' CSV HEADER;

\COPY ALERT.DESCRIPTOR_TYPES FROM '/opt/blackduck/alert/alert-config/data/temp/DESCRIPTOR_TYPES.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.DESCRIPTOR_TYPES', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.DESCRIPTOR_TYPES;
\COPY ALERT.REGISTERED_DESCRIPTORS FROM '/opt/blackduck/alert/alert-config/data/temp/REGISTERED_DESCRIPTORS.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.REGISTERED_DESCRIPTORS', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.REGISTERED_DESCRIPTORS;
\COPY ALERT.DEFINED_FIELDS FROM '/opt/blackduck/alert/alert-config/data/temp/DEFINED_FIELDS.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.DEFINED_FIELDS', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.DEFINED_FIELDS;
\COPY ALERT.CONFIG_CONTEXTS FROM '/opt/blackduck/alert/alert-config/data/temp/CONFIG_CONTEXTS.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.CONFIG_CONTEXTS', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.CONFIG_CONTEXTS;
\COPY ALERT.DESCRIPTOR_FIELDS FROM '/opt/blackduck/alert/alert-config/data/temp/DESCRIPTOR_FIELDS.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.FIELD_CONTEXTS FROM '/opt/blackduck/alert/alert-config/data/temp/FIELD_CONTEXTS.csv' DELIMITER ',' CSV HEADER;

\COPY ALERT.DESCRIPTOR_CONFIGS FROM '/opt/blackduck/alert/alert-config/data/temp/DESCRIPTOR_CONFIGS.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.DESCRIPTOR_CONFIGS', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.DESCRIPTOR_CONFIGS;
\COPY ALERT.CONFIG_GROUPS FROM '/opt/blackduck/alert/alert-config/data/temp/CONFIG_GROUPS.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.FIELD_VALUES FROM '/opt/blackduck/alert/alert-config/data/temp/FIELD_VALUES.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.FIELD_VALUES', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.FIELD_VALUES;

\COPY ALERT.AUTHENTICATION_TYPE FROM '/opt/blackduck/alert/alert-config/data/temp/AUTHENTICATION_TYPE.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.AUTHENTICATION_TYPE', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.USERS;
\COPY ALERT.USERS FROM '/opt/blackduck/alert/alert-config/data/temp/USERS.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.USERS', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.USERS;
\COPY ALERT.ROLES FROM '/opt/blackduck/alert/alert-config/data/temp/ROLES.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.ROLES', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.ROLES;
\COPY ALERT.USER_ROLES FROM '/opt/blackduck/alert/alert-config/data/temp/USER_ROLES.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.PERMISSION_MATRIX FROM '/opt/blackduck/alert/alert-config/data/temp/PERMISSION_MATRIX.csv' DELIMITER ',' CSV HEADER;

\COPY ALERT.PROVIDER_PROJECTS FROM '/opt/blackduck/alert/alert-config/data/temp/PROVIDER_PROJECTS.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.PROVIDER_PROJECTS', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.PROVIDER_PROJECTS;
\COPY ALERT.PROVIDER_USERS FROM '/opt/blackduck/alert/alert-config/data/temp/PROVIDER_USERS.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.PROVIDER_USERS', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.PROVIDER_USERS;
\COPY ALERT.PROVIDER_USER_PROJECT_RELATION FROM '/opt/blackduck/alert/alert-config/data/temp/PROVIDER_USER_PROJECT_RELATION.csv' DELIMITER ',' CSV HEADER;

\COPY ALERT.CUSTOM_CERTIFICATES FROM '/opt/blackduck/alert/alert-config/data/temp/CUSTOM_CERTIFICATES.csv' DELIMITER ',' CSV HEADER;
SELECT setval(pg_get_serial_sequence('ALERT.CUSTOM_CERTIFICATES', 'id'), COALESCE(max(ID) + 1, 1), false)
FROM ALERT.CUSTOM_CERTIFICATES;
