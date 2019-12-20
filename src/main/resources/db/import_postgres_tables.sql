\COPY ALERT.SYSTEM_STATUS FROM '/opt/blackduck/alert/alert-config/data/temp/SYSTEM_STATUS.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.SYSTEM_MESSAGES FROM '/opt/blackduck/alert/alert-config/data/temp/SYSTEM_MESSAGES.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.SETTINGS_KEY FROM '/opt/blackduck/alert/alert-config/data/temp/SETTINGS_KEY.csv' DELIMITER ',' CSV HEADER;

\COPY ALERT.RAW_NOTIFICATION_CONTENT FROM '/opt/blackduck/alert/alert-config/data/temp/RAW_NOTIFICATION_CONTENT.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.AUDIT_ENTRIES FROM '/opt/blackduck/alert/alert-config/data/temp/AUDIT_ENTRIES.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.AUDIT_NOTIFICATION_RELATION FROM '/opt/blackduck/alert/alert-config/data/temp/AUDIT_NOTIFICATION_RELATION.csv' DELIMITER ',' CSV HEADER;

\COPY ALERT.DESCRIPTOR_TYPES FROM '/opt/blackduck/alert/alert-config/data/temp/DESCRIPTOR_TYPES.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.REGISTERED_DESCRIPTORS FROM '/opt/blackduck/alert/alert-config/data/temp/REGISTERED_DESCRIPTORS.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.DEFINED_FIELDS FROM '/opt/blackduck/alert/alert-config/data/temp/DEFINED_FIELDS.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.CONFIG_CONTEXTS FROM '/opt/blackduck/alert/alert-config/data/temp/CONFIG_CONTEXTS.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.DESCRIPTOR_FIELDS FROM '/opt/blackduck/alert/alert-config/data/temp/DESCRIPTOR_FIELDS.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.FIELD_CONTEXTS FROM '/opt/blackduck/alert/alert-config/data/temp/FIELD_CONTEXTS.csv' DELIMITER ',' CSV HEADER;

\COPY ALERT.DESCRIPTOR_CONFIGS FROM '/opt/blackduck/alert/alert-config/data/temp/DESCRIPTOR_CONFIGS.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.CONFIG_GROUPS FROM '/opt/blackduck/alert/alert-config/data/temp/CONFIG_GROUPS.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.FIELD_VALUES FROM '/opt/blackduck/alert/alert-config/data/temp/FIELD_VALUES.csv' DELIMITER ',' CSV HEADER;

\COPY ALERT.USERS FROM '/opt/blackduck/alert/alert-config/data/temp/USERS.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.ROLES FROM '/opt/blackduck/alert/alert-config/data/temp/ROLES.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.USER_ROLES FROM '/opt/blackduck/alert/alert-config/data/temp/USER_ROLES.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.PERMISSION_MATRIX FROM '/opt/blackduck/alert/alert-config/data/temp/PERMISSION_MATRIX.csv' DELIMITER ',' CSV HEADER;

\COPY ALERT.PROVIDER_PROJECTS FROM '/opt/blackduck/alert/alert-config/data/temp/PROVIDER_PROJECTS.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.PROVIDER_USERS FROM '/opt/blackduck/alert/alert-config/data/temp/PROVIDER_USERS.csv' DELIMITER ',' CSV HEADER;
\COPY ALERT.PROVIDER_USER_PROJECT_RELATION FROM '/opt/blackduck/alert/alert-config/data/temp/PROVIDER_USER_PROJECT_RELATION.csv' DELIMITER ',' CSV HEADER;
