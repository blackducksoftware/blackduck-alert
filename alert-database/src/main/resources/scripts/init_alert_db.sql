CREATE SCHEMA IF NOT EXISTS ALERT;
CREATE SCHEMA IF NOT EXISTS PUBLIC;

create table if not exists ALERT.PROVIDER_PROJECTS
(
    ID                  BIGSERIAL,
    NAME                VARCHAR(512),
    DESCRIPTION         VARCHAR(255),
    HREF                VARCHAR(255),
    PROJECT_OWNER_EMAIL VARCHAR(255),
    PROVIDER            VARCHAR(255),
    constraint BLACKDUCK_PROJECT_KEY
        primary key (ID)
);

create table if not exists ALERT.PROVIDER_USERS
(
    ID            BIGSERIAL,
    EMAIL_ADDRESS VARCHAR(255),
    OPT_OUT       BOOLEAN,
    PROVIDER      VARCHAR(255),
    constraint BLACKDUCK_USER_KEY
        primary key (ID)
);

create table if not exists ALERT.SYSTEM_STATUS
(
    ID                        BIGINT not null,
    INITIALIZED_CONFIGURATION BOOLEAN,
    STARTUP_TIME              TIMESTAMP WITH TIME ZONE,
    constraint SYSTEM_STATUS_KEY
        primary key (ID)
);

create table if not exists ALERT.SYSTEM_MESSAGES
(
    ID         BIGSERIAL,
    CREATED_AT TIMESTAMP WITH TIME ZONE,
    SEVERITY   VARCHAR(50),
    CONTENT    VARCHAR(255),
    TYPE       VARCHAR(255),
    constraint SYSTEM_MESSAGES_KEY
        primary key (ID)
);

create table if not exists ALERT.RAW_NOTIFICATION_CONTENT
(
    ID                     BIGSERIAL,
    CREATED_AT             TIMESTAMP WITH TIME ZONE,
    PROVIDER               VARCHAR(255),
    PROVIDER_CREATION_TIME TIMESTAMP WITH TIME ZONE,
    NOTIFICATION_TYPE      VARCHAR(255),
    CONTENT                VARCHAR,
    constraint RAW_NOTIFICATION_CONTENT_KEY
        primary key (ID)
);

create table if not exists ALERT.PROVIDER_USER_PROJECT_RELATION
(
    PROVIDER_USER_ID    BIGINT not null,
    PROVIDER_PROJECT_ID BIGINT not null,
    constraint FK_PROVIDER_PROJECT_ID
        foreign key (PROVIDER_PROJECT_ID) references ALERT.PROVIDER_PROJECTS (ID)
            on delete cascade,
    constraint FK_PROVIDER_USER_ID
        foreign key (PROVIDER_USER_ID) references ALERT.PROVIDER_USERS (ID)
            on delete cascade
);

create table if not exists ALERT.ROLES
(
    ID       BIGSERIAL,
    ROLENAME VARCHAR(255),
    CUSTOM   BOOLEAN default FALSE not null,
    constraint ROLE_KEY
        primary key (ID)
);

create table if not exists ALERT.SETTINGS_KEY
(
    ID    BIGSERIAL,
    KEY   VARCHAR(255),
    VALUE VARCHAR(255),
    constraint SETTINGS_KEY_KEY
        primary key (ID),
    unique (KEY)
);

create table if not exists ALERT.REGISTERED_DESCRIPTORS
(
    ID      BIGSERIAL,
    TYPE_ID BIGINT,
    NAME    VARCHAR(255),
    constraint REGISTERED_DESCRIPTORS_KEY
        primary key (ID)
);

create table if not exists ALERT.DESCRIPTOR_TYPES
(
    ID   BIGSERIAL,
    TYPE VARCHAR(255),
    constraint DESCRIPTOR_TYPES_KEY
        primary key (ID)
);

create table if not exists ALERT.DEFINED_FIELDS
(
    ID         BIGSERIAL,
    SOURCE_KEY VARCHAR(255)          not null
        unique,
    SENSITIVE  BOOLEAN default FALSE not null,
    constraint DEFINED_FIELDS_KEY
        primary key (ID)
);

create table if not exists ALERT.DESCRIPTOR_FIELDS
(
    DESCRIPTOR_ID BIGINT not null,
    FIELD_ID      BIGINT not null,
    constraint DESCRIPTOR_FIELDS_KEY
        primary key (DESCRIPTOR_ID, FIELD_ID),
    constraint FK_DESCRIPTOR_FIELD
        foreign key (FIELD_ID) references ALERT.DEFINED_FIELDS (ID)
            on delete cascade,
    constraint FK_FIELD_DESCRIPTOR
        foreign key (DESCRIPTOR_ID) references ALERT.REGISTERED_DESCRIPTORS (ID)
            on delete cascade
);

create table if not exists ALERT.CONFIG_CONTEXTS
(
    ID      BIGSERIAL,
    CONTEXT VARCHAR(31)
        unique,
    constraint CONFIG_CONTEXTS_KEY
        primary key (ID)
);

create table if not exists ALERT.PERMISSION_MATRIX
(
    ROLE_ID       BIGINT  not null,
    OPERATIONS    INTEGER not null,
    DESCRIPTOR_ID BIGINT  not null,
    CONTEXT_ID    BIGINT  not null,
    constraint PERMISSION_MATRIX_KEY_UPDATED
        primary key (ROLE_ID, OPERATIONS, DESCRIPTOR_ID, CONTEXT_ID),
    constraint FK_PERMISSION_CONTEXT_ID
        foreign key (CONTEXT_ID) references ALERT.CONFIG_CONTEXTS (ID)
            on delete cascade,
    constraint FK_PERMISSION_DESCRIPTOR_ID
        foreign key (DESCRIPTOR_ID) references ALERT.REGISTERED_DESCRIPTORS (ID)
            on delete cascade,
    constraint FK_PERMISSION_ROLE
        foreign key (ROLE_ID) references ALERT.ROLES (ID)
            on delete cascade
);

create table if not exists ALERT.DESCRIPTOR_CONFIGS
(
    ID            BIGSERIAL,
    DESCRIPTOR_ID BIGINT,
    CONTEXT_ID    BIGINT,
    CREATED_AT    TIMESTAMP WITH TIME ZONE,
    LAST_UPDATED  TIMESTAMP WITH TIME ZONE,
    constraint DESCRIPTOR_CONFIGS_KEY
        primary key (ID),
    constraint FK_CONFIG_CONTEXT
        foreign key (CONTEXT_ID) references ALERT.CONFIG_CONTEXTS (ID)
            on delete cascade,
    constraint FK_CONFIG_DESCRIPTOR
        foreign key (DESCRIPTOR_ID) references ALERT.REGISTERED_DESCRIPTORS (ID)
            on delete cascade
);

create table if not exists ALERT.FIELD_CONTEXTS
(
    FIELD_ID   BIGINT not null,
    CONTEXT_ID BIGINT not null,
    constraint FIELD_CONTEXTS_KEY
        primary key (FIELD_ID, CONTEXT_ID),
    constraint FK_CONTEXT_FIELD
        foreign key (FIELD_ID) references ALERT.DEFINED_FIELDS (ID)
            on delete cascade,
    constraint FK_FIELD_CONTEXT
        foreign key (CONTEXT_ID) references ALERT.CONFIG_CONTEXTS (ID)
            on delete cascade
);

create table if not exists ALERT.CONFIG_GROUPS
(
    CONFIG_ID BIGINT not null,
    JOB_ID    UUID   not null,
    constraint CONFIG_GROUPS_KEY
        primary key (CONFIG_ID),
    constraint FK_CONFIG_GROUP_VALUE
        foreign key (CONFIG_ID) references ALERT.DESCRIPTOR_CONFIGS (ID)
            on delete cascade
);

create table if not exists ALERT.FIELD_VALUES
(
    ID          BIGSERIAL,
    CONFIG_ID   BIGINT,
    FIELD_ID    BIGINT,
    FIELD_VALUE VARCHAR(512),
    constraint CONFIG_VALUES_KEY
        primary key (ID),
    constraint FK_DEFINED_FIELD_VALUE
        foreign key (FIELD_ID) references ALERT.DEFINED_FIELDS (ID)
            on delete cascade,
    constraint FK_DESCRIPTOR_CONFIG_VALUE
        foreign key (CONFIG_ID) references ALERT.DESCRIPTOR_CONFIGS (ID)
            on delete cascade
);

create table if not exists ALERT.AUTHENTICATION_TYPE
(
    ID   BIGSERIAL,
    NAME VARCHAR(255),
    constraint AUTH_TYPE_KEY
        primary key (ID)
);

create table if not exists ALERT.USERS
(
    ID               BIGSERIAL,
    USERNAME         VARCHAR(2048)
        unique,
    PASSWORD         VARCHAR(2048),
    EMAIL_ADDRESS    VARCHAR(2048),
    EXPIRED          BOOLEAN default FALSE,
    LOCKED           BOOLEAN default FALSE,
    PASSWORD_EXPIRED BOOLEAN default FALSE,
    ENABLED          BOOLEAN default TRUE,
    AUTH_TYPE        BIGINT  default 1 not null,
    constraint USER_KEY
        primary key (ID),
    constraint FK_AUTH_TYPE_ID
        foreign key (AUTH_TYPE) references ALERT.AUTHENTICATION_TYPE (ID)
            on delete cascade
);

create table if not exists ALERT.USER_ROLES
(
    USER_ID BIGINT not null,
    ROLE_ID BIGINT not null,
    constraint FK_ROLE_ID
        foreign key (ROLE_ID) references ALERT.ROLES (ID)
            on delete cascade,
    constraint FK_USER_ID
        foreign key (USER_ID) references ALERT.USERS (ID)
            on delete cascade
);

create table if not exists ALERT.AUDIT_ENTRIES
(
    ID                BIGSERIAL
        primary key,
    ERROR_MESSAGE     VARCHAR(255),
    ERROR_STACK_TRACE VARCHAR,
    STATUS            VARCHAR(255),
    TIME_CREATED      TIMESTAMP WITH TIME ZONE,
    TIME_LAST_SENT    TIMESTAMP WITH TIME ZONE,
    COMMON_CONFIG_ID  UUID
);

create table if not exists ALERT.AUDIT_NOTIFICATION_RELATION
(
    AUDIT_ENTRY_ID  BIGINT not null,
    NOTIFICATION_ID BIGINT not null,
    primary key (AUDIT_ENTRY_ID, NOTIFICATION_ID),
    constraint FK_AUDIT_ENTRY_ID
        foreign key (AUDIT_ENTRY_ID) references ALERT.AUDIT_ENTRIES (ID)
            on delete cascade,
    constraint FK_AUDIT_NOTIFICATION_ID
        foreign key (NOTIFICATION_ID) references ALERT.RAW_NOTIFICATION_CONTENT (ID)
            on delete cascade
);

create table if not exists ALERT.CUSTOM_CERTIFICATES
(
    ID                  BIGSERIAL,
    ALIAS               VARCHAR(128),
    CERTIFICATE_CONTENT VARCHAR,
    constraint PK_CUSTOM_CERTIFICATES
        primary key (ID)
);

create table if not exists PUBLIC.DATABASECHANGELOGLOCK
(
    ID          INTEGER not null,
    LOCKED      BOOLEAN not null,
    LOCKGRANTED TIMESTAMP,
    LOCKEDBY    VARCHAR(255),
    constraint PK_DATABASECHANGELOGLOCK
        primary key (ID)
);

create table if not exists PUBLIC.DATABASECHANGELOG
(
    ID            VARCHAR(255) not null,
    AUTHOR        VARCHAR(255) not null,
    FILENAME      VARCHAR(255) not null,
    DATEEXECUTED  TIMESTAMP    not null,
    ORDEREXECUTED INTEGER      not null,
    EXECTYPE      VARCHAR(10)  not null,
    MD5SUM        VARCHAR(35),
    DESCRIPTION   VARCHAR(255),
    COMMENTS      VARCHAR(255),
    TAG           VARCHAR(255),
    LIQUIBASE     VARCHAR(20),
    CONTEXTS      VARCHAR(255),
    LABELS        VARCHAR(255),
    DEPLOYMENT_ID VARCHAR(10)
);

-- In order for the sequences to match up correctly in hibernate with postgres set the increment to hibernates default allocation size of 50.
ALTER SEQUENCE alert.audit_entries_id_seq INCREMENT 50;
ALTER SEQUENCE alert.descriptor_configs_id_seq INCREMENT 50;
ALTER SEQUENCE alert.field_values_id_seq INCREMENT 50;
ALTER SEQUENCE alert.provider_projects_id_seq INCREMENT 50;
ALTER SEQUENCE alert.provider_users_id_seq INCREMENT 50;
ALTER SEQUENCE alert.raw_notification_content_id_seq INCREMENT 50;
ALTER SEQUENCE alert.system_messages_id_seq INCREMENT 50;
