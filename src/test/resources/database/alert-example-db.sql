--
-- PostgreSQL database dump
--

-- Dumped from database version 14.5
-- Dumped by pg_dump version 14.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE ONLY alert.slack_job_details DROP CONSTRAINT slack_job_details_job_id_fk;
ALTER TABLE ONLY alert.provider_task_properties DROP CONSTRAINT provider_task_properties_provider_config_id_fk;
ALTER TABLE ONLY alert.ms_teams_job_details DROP CONSTRAINT ms_teams_job_details_job_id_fk;
ALTER TABLE ONLY alert.notification_correlation_to_notification_relation DROP CONSTRAINT job_sub_task_notification_id_fk;
ALTER TABLE ONLY alert.notification_correlation_to_notification_relation DROP CONSTRAINT job_sub_task_notification_correlation_id_fk;
ALTER TABLE ONLY alert.job_notification_relation DROP CONSTRAINT job_mapping_notification_id_fk;
ALTER TABLE ONLY alert.job_notification_relation DROP CONSTRAINT job_mapping_job_id_fk;
ALTER TABLE ONLY alert.jira_server_job_details DROP CONSTRAINT jira_server_job_details_job_id_fk;
ALTER TABLE ONLY alert.jira_server_job_custom_fields DROP CONSTRAINT jira_server_job_custom_fields_job_id_fk;
ALTER TABLE ONLY alert.jira_cloud_job_details DROP CONSTRAINT jira_cloud_job_details_job_id_fk;
ALTER TABLE ONLY alert.jira_cloud_job_custom_fields DROP CONSTRAINT jira_cloud_job_custom_fields_job_id_fk;
ALTER TABLE ONLY alert.user_roles DROP CONSTRAINT fk_user_id;
ALTER TABLE ONLY alert.user_roles DROP CONSTRAINT fk_role_id;
ALTER TABLE ONLY alert.registered_descriptors DROP CONSTRAINT fk_registered_descriptors_type_id;
ALTER TABLE ONLY alert.permission_matrix DROP CONSTRAINT fk_permission_role;
ALTER TABLE ONLY alert.permission_matrix DROP CONSTRAINT fk_permission_descriptor_id;
ALTER TABLE ONLY alert.permission_matrix DROP CONSTRAINT fk_permission_context_id;
ALTER TABLE ONLY alert.raw_notification_content DROP CONSTRAINT fk_notification_provider_config_id;
ALTER TABLE ONLY alert.descriptor_fields DROP CONSTRAINT fk_field_descriptor;
ALTER TABLE ONLY alert.field_contexts DROP CONSTRAINT fk_field_context;
ALTER TABLE ONLY alert.descriptor_fields DROP CONSTRAINT fk_descriptor_field;
ALTER TABLE ONLY alert.field_values DROP CONSTRAINT fk_descriptor_config_value;
ALTER TABLE ONLY alert.field_values DROP CONSTRAINT fk_defined_field_value;
ALTER TABLE ONLY alert.field_contexts DROP CONSTRAINT fk_context_field;
ALTER TABLE ONLY alert.descriptor_configs DROP CONSTRAINT fk_config_descriptor;
ALTER TABLE ONLY alert.descriptor_configs DROP CONSTRAINT fk_config_context;
ALTER TABLE ONLY alert.users DROP CONSTRAINT fk_auth_type_id;
ALTER TABLE ONLY alert.audit_notification_relation DROP CONSTRAINT fk_audit_notification_id;
ALTER TABLE ONLY alert.audit_notification_relation DROP CONSTRAINT fk_audit_entry_id;
ALTER TABLE ONLY alert.email_job_details DROP CONSTRAINT email_job_details_job_id_fk;
ALTER TABLE ONLY alert.email_job_additional_email_addresses DROP CONSTRAINT email_job_additional_email_addresses_job_id_fk;
ALTER TABLE ONLY alert.configuration_non_proxy_hosts DROP CONSTRAINT configuration_non_proxy_hosts_id_fk;
ALTER TABLE ONLY alert.configuration_email_properties DROP CONSTRAINT configuration_email_properties_configuration_id_fk;
ALTER TABLE ONLY alert.blackduck_job_vulnerability_severity_filters DROP CONSTRAINT blackduck_job_vuln_severity_filters_job_id_fk;
ALTER TABLE ONLY alert.blackduck_job_projects DROP CONSTRAINT blackduck_job_projects_job_id_fk;
ALTER TABLE ONLY alert.blackduck_job_policy_filters DROP CONSTRAINT blackduck_job_policy_filters_job_id_fk;
ALTER TABLE ONLY alert.blackduck_job_notification_types DROP CONSTRAINT blackduck_job_notification_types_job_id_fk;
ALTER TABLE ONLY alert.blackduck_job_details DROP CONSTRAINT blackduck_job_details_job_id_fk;
ALTER TABLE ONLY alert.azure_boards_job_details DROP CONSTRAINT azure_boards_job_details_job_id_fk;
ALTER TABLE ONLY public.databasechangeloglock DROP CONSTRAINT databasechangeloglock_pkey;
ALTER TABLE ONLY alert.users DROP CONSTRAINT users_username_key;
ALTER TABLE ONLY alert.user_roles DROP CONSTRAINT user_roles_pk;
ALTER TABLE ONLY alert.users DROP CONSTRAINT user_key;
ALTER TABLE ONLY alert.system_status DROP CONSTRAINT system_status_key;
ALTER TABLE ONLY alert.system_messages DROP CONSTRAINT system_messages_key;
ALTER TABLE ONLY alert.slack_job_details DROP CONSTRAINT slack_job_details_pkey;
ALTER TABLE ONLY alert.settings_key DROP CONSTRAINT settings_key_key_key;
ALTER TABLE ONLY alert.settings_key DROP CONSTRAINT settings_key_key;
ALTER TABLE ONLY alert.roles DROP CONSTRAINT role_key;
ALTER TABLE ONLY alert.registered_descriptors DROP CONSTRAINT registered_descriptors_key;
ALTER TABLE ONLY alert.raw_notification_content DROP CONSTRAINT raw_notification_content_key;
ALTER TABLE ONLY alert.provider_task_properties DROP CONSTRAINT provider_task_properties_pkey;
ALTER TABLE ONLY alert.notification_correlation_to_notification_relation DROP CONSTRAINT pk_notification_correlation_to_notification_relation;
ALTER TABLE ONLY alert.job_notification_relation DROP CONSTRAINT pk_job_mapping_relation;
ALTER TABLE ONLY alert.custom_certificates DROP CONSTRAINT pk_custom_certificates;
ALTER TABLE ONLY alert.permission_matrix DROP CONSTRAINT permission_matrix_key_updated;
ALTER TABLE ONLY alert.oauth_credentials DROP CONSTRAINT oauth_credentials_pkey;
ALTER TABLE ONLY alert.ms_teams_job_details DROP CONSTRAINT ms_teams_job_details_pkey;
ALTER TABLE ONLY alert.job_sub_task_status DROP CONSTRAINT job_sub_task_status_pkey;
ALTER TABLE ONLY alert.job_sub_task_status DROP CONSTRAINT job_sub_task_status_notification_correlation_id_key;
ALTER TABLE ONLY alert.jira_server_job_details DROP CONSTRAINT jira_server_job_details_pkey;
ALTER TABLE ONLY alert.jira_server_job_custom_fields DROP CONSTRAINT jira_server_job_custom_fields_pkey;
ALTER TABLE ONLY alert.jira_cloud_job_details DROP CONSTRAINT jira_cloud_job_details_pkey;
ALTER TABLE ONLY alert.jira_cloud_job_custom_fields DROP CONSTRAINT jira_cloud_job_custom_fields_pkey;
ALTER TABLE ONLY alert.field_contexts DROP CONSTRAINT field_contexts_key;
ALTER TABLE ONLY alert.email_job_details DROP CONSTRAINT email_job_details_pkey;
ALTER TABLE ONLY alert.email_job_additional_email_addresses DROP CONSTRAINT email_job_additional_email_addresses_pkey;
ALTER TABLE ONLY alert.distribution_jobs DROP CONSTRAINT distribution_jobs_pkey;
ALTER TABLE ONLY alert.descriptor_types DROP CONSTRAINT descriptor_types_key;
ALTER TABLE ONLY alert.descriptor_fields DROP CONSTRAINT descriptor_fields_key;
ALTER TABLE ONLY alert.descriptor_configs DROP CONSTRAINT descriptor_configs_key;
ALTER TABLE ONLY alert.defined_fields DROP CONSTRAINT defined_fields_source_key_key;
ALTER TABLE ONLY alert.defined_fields DROP CONSTRAINT defined_fields_key;
ALTER TABLE ONLY alert.configuration_proxy DROP CONSTRAINT configuration_proxy_pkey;
ALTER TABLE ONLY alert.configuration_proxy DROP CONSTRAINT configuration_proxy_name_key;
ALTER TABLE ONLY alert.configuration_non_proxy_hosts DROP CONSTRAINT configuration_non_proxy_hosts_pkey;
ALTER TABLE ONLY alert.configuration_jira_server DROP CONSTRAINT configuration_jira_server_pkey;
ALTER TABLE ONLY alert.configuration_jira_server DROP CONSTRAINT configuration_jira_server_name_key;
ALTER TABLE ONLY alert.configuration_email_properties DROP CONSTRAINT configuration_email_properties_pkey;
ALTER TABLE ONLY alert.configuration_email DROP CONSTRAINT configuration_email_pkey;
ALTER TABLE ONLY alert.configuration_email DROP CONSTRAINT configuration_email_name_key;
ALTER TABLE ONLY alert.configuration_azure_boards DROP CONSTRAINT configuration_azure_boards_pkey;
ALTER TABLE ONLY alert.configuration_azure_boards DROP CONSTRAINT configuration_azure_boards_name_key;
ALTER TABLE ONLY alert.field_values DROP CONSTRAINT config_values_key;
ALTER TABLE ONLY alert.config_contexts DROP CONSTRAINT config_contexts_key;
ALTER TABLE ONLY alert.config_contexts DROP CONSTRAINT config_contexts_context_key;
ALTER TABLE ONLY alert.blackduck_job_vulnerability_severity_filters DROP CONSTRAINT blackduck_job_vulnerability_severity_filters_pkey;
ALTER TABLE ONLY alert.blackduck_job_projects DROP CONSTRAINT blackduck_job_projects_pk;
ALTER TABLE ONLY alert.blackduck_job_policy_filters DROP CONSTRAINT blackduck_job_policy_filters_pkey;
ALTER TABLE ONLY alert.blackduck_job_notification_types DROP CONSTRAINT blackduck_job_notification_types_pkey;
ALTER TABLE ONLY alert.blackduck_job_details DROP CONSTRAINT blackduck_job_details_pkey;
ALTER TABLE ONLY alert.azure_boards_job_details DROP CONSTRAINT azure_boards_job_details_pkey;
ALTER TABLE ONLY alert.authentication_type DROP CONSTRAINT auth_type_key;
ALTER TABLE ONLY alert.audit_notification_relation DROP CONSTRAINT audit_notification_relation_pkey;
ALTER TABLE ONLY alert.audit_entries DROP CONSTRAINT audit_entries_pkey;
ALTER TABLE alert.users ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.system_messages ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.settings_key ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.roles ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.registered_descriptors ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.raw_notification_content ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.field_values ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.descriptor_types ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.descriptor_configs ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.defined_fields ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.custom_certificates ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.config_contexts ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.authentication_type ALTER COLUMN id DROP DEFAULT;
ALTER TABLE alert.audit_entries ALTER COLUMN id DROP DEFAULT;
DROP TABLE public.databasechangeloglock;
DROP TABLE public.databasechangelog;
DROP SEQUENCE alert.users_id_seq;
DROP TABLE alert.users;
DROP TABLE alert.user_roles;
DROP TABLE alert.system_status;
DROP SEQUENCE alert.system_messages_id_seq;
DROP TABLE alert.system_messages;
DROP TABLE alert.slack_job_details;
DROP SEQUENCE alert.settings_key_id_seq;
DROP TABLE alert.settings_key;
DROP SEQUENCE alert.roles_id_seq;
DROP TABLE alert.roles;
DROP SEQUENCE alert.registered_descriptors_id_seq;
DROP TABLE alert.registered_descriptors;
DROP SEQUENCE alert.raw_notification_content_id_seq;
DROP TABLE alert.raw_notification_content;
DROP TABLE alert.provider_task_properties;
DROP TABLE alert.permission_matrix;
DROP TABLE alert.oauth_credentials;
DROP TABLE alert.notification_correlation_to_notification_relation;
DROP TABLE alert.ms_teams_job_details;
DROP TABLE alert.job_sub_task_status;
DROP TABLE alert.job_notification_relation;
DROP TABLE alert.jira_server_job_details;
DROP TABLE alert.jira_server_job_custom_fields;
DROP TABLE alert.jira_cloud_job_details;
DROP TABLE alert.jira_cloud_job_custom_fields;
DROP SEQUENCE alert.field_values_id_seq;
DROP TABLE alert.field_values;
DROP TABLE alert.field_contexts;
DROP TABLE alert.email_job_details;
DROP TABLE alert.email_job_additional_email_addresses;
DROP TABLE alert.distribution_jobs;
DROP SEQUENCE alert.descriptor_types_id_seq;
DROP TABLE alert.descriptor_types;
DROP TABLE alert.descriptor_fields;
DROP SEQUENCE alert.descriptor_configs_id_seq;
DROP TABLE alert.descriptor_configs;
DROP SEQUENCE alert.defined_fields_id_seq;
DROP TABLE alert.defined_fields;
DROP SEQUENCE alert.custom_certificates_id_seq;
DROP TABLE alert.custom_certificates;
DROP TABLE alert.configuration_proxy;
DROP TABLE alert.configuration_non_proxy_hosts;
DROP TABLE alert.configuration_jira_server;
DROP TABLE alert.configuration_email_properties;
DROP TABLE alert.configuration_email;
DROP TABLE alert.configuration_azure_boards;
DROP SEQUENCE alert.config_contexts_id_seq;
DROP TABLE alert.config_contexts;
DROP TABLE alert.blackduck_job_vulnerability_severity_filters;
DROP TABLE alert.blackduck_job_projects;
DROP TABLE alert.blackduck_job_policy_filters;
DROP TABLE alert.blackduck_job_notification_types;
DROP TABLE alert.blackduck_job_details;
DROP TABLE alert.azure_boards_job_details;
DROP SEQUENCE alert.authentication_type_id_seq;
DROP TABLE alert.authentication_type;
DROP TABLE alert.audit_notification_relation;
DROP SEQUENCE alert.audit_entries_id_seq;
DROP TABLE alert.audit_entries;
DROP FUNCTION public.get_user_id(text);
DROP FUNCTION public.get_role_id(text);
DROP FUNCTION public.get_provider_config_id(text);
DROP FUNCTION public.get_old_black_duck_provider_config_id();
DROP FUNCTION public.get_job_id_source_key_and_field_value();
DROP FUNCTION public.get_global_config_timestamps(text);
DROP FUNCTION public.get_global_config_source_key_and_field_value(text);
DROP FUNCTION public.get_field_id(text);
DROP FUNCTION public.get_descriptor_type_id(text);
DROP FUNCTION public.get_descriptor_id(text);
DROP FUNCTION public.get_context_id(text);
DROP FUNCTION public.get_auth_type_id(text);
DROP EXTENSION "uuid-ossp";
DROP SCHEMA alert;
--
-- Name: alert; Type: SCHEMA; Schema: -; Owner: sa
--

CREATE SCHEMA alert;


ALTER SCHEMA alert OWNER TO sa;

--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- Name: get_auth_type_id(text); Type: FUNCTION; Schema: public; Owner: sa
--

CREATE FUNCTION public.get_auth_type_id(text) RETURNS bigint
    LANGUAGE plpgsql
    AS $_$
                DECLARE result BIGINT;
                BEGIN
                    select ID
                    into result
                    from ALERT.AUTHENTICATION_TYPE where NAME = $1;
                    RETURN result;
                END;
            $_$;


ALTER FUNCTION public.get_auth_type_id(text) OWNER TO sa;

--
-- Name: get_context_id(text); Type: FUNCTION; Schema: public; Owner: sa
--

CREATE FUNCTION public.get_context_id(text) RETURNS bigint
    LANGUAGE plpgsql
    AS $_$
                DECLARE result BIGINT;
                BEGIN
                    select ID
                    into result
                    from ALERT.CONFIG_CONTEXTS where CONTEXT = $1;
                    RETURN result;
                END;
            $_$;


ALTER FUNCTION public.get_context_id(text) OWNER TO sa;

--
-- Name: get_descriptor_id(text); Type: FUNCTION; Schema: public; Owner: sa
--

CREATE FUNCTION public.get_descriptor_id(text) RETURNS bigint
    LANGUAGE plpgsql
    AS $_$
                DECLARE result BIGINT;
                BEGIN
                    select ID
                    into result
                    from ALERT.REGISTERED_DESCRIPTORS where NAME = $1;
                RETURN result;
                END;
            $_$;


ALTER FUNCTION public.get_descriptor_id(text) OWNER TO sa;

--
-- Name: get_descriptor_type_id(text); Type: FUNCTION; Schema: public; Owner: sa
--

CREATE FUNCTION public.get_descriptor_type_id(text) RETURNS bigint
    LANGUAGE plpgsql
    AS $_$
                DECLARE result BIGINT;
                BEGIN
                    select ID
                    into result
                    from ALERT.DESCRIPTOR_TYPES where TYPE = $1;
                    RETURN result;
                END;
            $_$;


ALTER FUNCTION public.get_descriptor_type_id(text) OWNER TO sa;

--
-- Name: get_field_id(text); Type: FUNCTION; Schema: public; Owner: sa
--

CREATE FUNCTION public.get_field_id(text) RETURNS bigint
    LANGUAGE plpgsql
    AS $_$
                DECLARE result BIGINT;
                BEGIN
                    select ID
                    into result from ALERT.DEFINED_FIELDS
                    where SOURCE_KEY = $1;
                    RETURN result;
                END;
            $_$;


ALTER FUNCTION public.get_field_id(text) OWNER TO sa;

--
-- Name: get_global_config_source_key_and_field_value(text); Type: FUNCTION; Schema: public; Owner: sa
--

CREATE FUNCTION public.get_global_config_source_key_and_field_value(text) RETURNS TABLE(source_key character varying, field_value character varying)
    LANGUAGE plpgsql
    AS $_$
                    BEGIN
                     RETURN QUERY
                      SELECT field.source_key, fv.field_value
                            FROM alert.descriptor_configs config
                            LEFT JOIN alert.field_values fv ON fv.config_id = config.id
                            INNER JOIN alert.defined_fields field on field.id = fv.field_id
                            WHERE config.descriptor_id = GET_DESCRIPTOR_ID($1)
                            and config.context_id = GET_CONTEXT_ID('GLOBAL');
                    END;
                $_$;


ALTER FUNCTION public.get_global_config_source_key_and_field_value(text) OWNER TO sa;

--
-- Name: get_global_config_timestamps(text); Type: FUNCTION; Schema: public; Owner: sa
--

CREATE FUNCTION public.get_global_config_timestamps(text) RETURNS TABLE(created_at timestamp with time zone, last_updated timestamp with time zone)
    LANGUAGE plpgsql
    AS $_$
                    BEGIN
                     RETURN QUERY
                      SELECT config.created_at, config.last_updated
                            FROM alert.descriptor_configs config
                            WHERE config.descriptor_id = GET_DESCRIPTOR_ID($1)
                            and config.context_id = GET_CONTEXT_ID('GLOBAL');
                    END;
                $_$;


ALTER FUNCTION public.get_global_config_timestamps(text) OWNER TO sa;

--
-- Name: get_job_id_source_key_and_field_value(); Type: FUNCTION; Schema: public; Owner: sa
--

CREATE FUNCTION public.get_job_id_source_key_and_field_value() RETURNS TABLE(job_id uuid, source_key character varying, field_value character varying)
    LANGUAGE plpgsql
    AS $$
                    BEGIN
                     RETURN QUERY
                      SELECT job.job_id, field.source_key, fv.field_value
                            FROM alert.config_groups job
                            INNER JOIN alert.descriptor_configs config ON config.id = job.config_id
                            LEFT JOIN alert.field_values fv ON fv.config_id = config.id
                            INNER JOIN alert.defined_fields field on field.id = fv.field_id;
                    END;
                $$;


ALTER FUNCTION public.get_job_id_source_key_and_field_value() OWNER TO sa;

--
-- Name: get_old_black_duck_provider_config_id(); Type: FUNCTION; Schema: public; Owner: sa
--

CREATE FUNCTION public.get_old_black_duck_provider_config_id() RETURNS bigint
    LANGUAGE plpgsql
    AS $$
                DECLARE result BIGINT;
                BEGIN
                    select ID
                    into result
                    from ALERT.DESCRIPTOR_CONFIGS where CONTEXT_ID = GET_CONTEXT_ID('GLOBAL') and DESCRIPTOR_ID = GET_DESCRIPTOR_ID('provider_blackduck');
                    RETURN result;
                END;
            $$;


ALTER FUNCTION public.get_old_black_duck_provider_config_id() OWNER TO sa;

--
-- Name: get_provider_config_id(text); Type: FUNCTION; Schema: public; Owner: sa
--

CREATE FUNCTION public.get_provider_config_id(text) RETURNS bigint
    LANGUAGE plpgsql
    AS $_$
                DECLARE result BIGINT;
                BEGIN
                    select ID
                    into result
                    from ALERT.DESCRIPTOR_CONFIGS
                    where DESCRIPTOR_ID = GET_DESCRIPTOR_ID($1)
                    and CONTEXT_ID = GET_CONTEXT_ID('GLOBAL')
                    LIMIT 1;
                    RETURN result;
                END;
            $_$;


ALTER FUNCTION public.get_provider_config_id(text) OWNER TO sa;

--
-- Name: get_role_id(text); Type: FUNCTION; Schema: public; Owner: sa
--

CREATE FUNCTION public.get_role_id(text) RETURNS bigint
    LANGUAGE plpgsql
    AS $_$
                DECLARE result BIGINT;
                BEGIN
                    select ID
                    into result
                    from ALERT.ROLES where ROLENAME = $1;
                    RETURN result;
                END;
            $_$;


ALTER FUNCTION public.get_role_id(text) OWNER TO sa;

--
-- Name: get_user_id(text); Type: FUNCTION; Schema: public; Owner: sa
--

CREATE FUNCTION public.get_user_id(text) RETURNS bigint
    LANGUAGE plpgsql
    AS $_$
                DECLARE result BIGINT;
                BEGIN
                    select ID
                    into result
                    from ALERT.USERS where USERNAME = $1;
                    RETURN result;
                END;
            $_$;


ALTER FUNCTION public.get_user_id(text) OWNER TO sa;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: audit_entries; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.audit_entries (
    id bigint NOT NULL,
    error_message character varying(255),
    error_stack_trace character varying,
    status character varying(255),
    time_created timestamp with time zone,
    time_last_sent timestamp with time zone,
    common_config_id uuid
);


ALTER TABLE alert.audit_entries OWNER TO sa;

--
-- Name: audit_entries_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.audit_entries_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.audit_entries_id_seq OWNER TO sa;

--
-- Name: audit_entries_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.audit_entries_id_seq OWNED BY alert.audit_entries.id;


--
-- Name: audit_notification_relation; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.audit_notification_relation (
    audit_entry_id bigint NOT NULL,
    notification_id bigint NOT NULL
);


ALTER TABLE alert.audit_notification_relation OWNER TO sa;

--
-- Name: authentication_type; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.authentication_type (
    id bigint NOT NULL,
    name character varying(255)
);


ALTER TABLE alert.authentication_type OWNER TO sa;

--
-- Name: authentication_type_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.authentication_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.authentication_type_id_seq OWNER TO sa;

--
-- Name: authentication_type_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.authentication_type_id_seq OWNED BY alert.authentication_type.id;


--
-- Name: azure_boards_job_details; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.azure_boards_job_details (
    job_id uuid NOT NULL,
    add_comments boolean DEFAULT false NOT NULL,
    project_name_or_id character varying NOT NULL,
    work_item_type character varying NOT NULL,
    work_item_completed_state character varying,
    work_item_reopen_state character varying
);


ALTER TABLE alert.azure_boards_job_details OWNER TO sa;

--
-- Name: blackduck_job_details; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.blackduck_job_details (
    job_id uuid NOT NULL,
    global_config_id bigint NOT NULL,
    filter_by_project boolean DEFAULT false NOT NULL,
    project_name_pattern character varying,
    project_version_name_pattern character varying
);


ALTER TABLE alert.blackduck_job_details OWNER TO sa;

--
-- Name: blackduck_job_notification_types; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.blackduck_job_notification_types (
    job_id uuid NOT NULL,
    notification_type character varying NOT NULL
);


ALTER TABLE alert.blackduck_job_notification_types OWNER TO sa;

--
-- Name: blackduck_job_policy_filters; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.blackduck_job_policy_filters (
    job_id uuid NOT NULL,
    policy_name character varying NOT NULL
);


ALTER TABLE alert.blackduck_job_policy_filters OWNER TO sa;

--
-- Name: blackduck_job_projects; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.blackduck_job_projects (
    job_id uuid NOT NULL,
    project_name character varying NOT NULL,
    href character varying NOT NULL
);


ALTER TABLE alert.blackduck_job_projects OWNER TO sa;

--
-- Name: blackduck_job_vulnerability_severity_filters; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.blackduck_job_vulnerability_severity_filters (
    job_id uuid NOT NULL,
    severity_name character varying NOT NULL
);


ALTER TABLE alert.blackduck_job_vulnerability_severity_filters OWNER TO sa;

--
-- Name: config_contexts; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.config_contexts (
    id bigint NOT NULL,
    context character varying(31)
);


ALTER TABLE alert.config_contexts OWNER TO sa;

--
-- Name: config_contexts_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.config_contexts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.config_contexts_id_seq OWNER TO sa;

--
-- Name: config_contexts_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.config_contexts_id_seq OWNED BY alert.config_contexts.id;


--
-- Name: configuration_azure_boards; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.configuration_azure_boards (
    configuration_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    last_updated timestamp with time zone,
    organization_name character varying NOT NULL,
    app_id character varying NOT NULL,
    client_secret character varying NOT NULL
);


ALTER TABLE alert.configuration_azure_boards OWNER TO sa;

--
-- Name: configuration_email; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.configuration_email (
    configuration_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    last_updated timestamp with time zone,
    smtp_host character varying NOT NULL,
    smtp_from character varying NOT NULL,
    port bigint,
    auth_required boolean DEFAULT false NOT NULL,
    auth_username character varying,
    auth_password character varying
);


ALTER TABLE alert.configuration_email OWNER TO sa;

--
-- Name: configuration_email_properties; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.configuration_email_properties (
    configuration_id uuid NOT NULL,
    property_key character varying NOT NULL,
    property_value character varying
);


ALTER TABLE alert.configuration_email_properties OWNER TO sa;

--
-- Name: configuration_jira_server; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.configuration_jira_server (
    configuration_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    last_updated timestamp with time zone,
    url character varying NOT NULL,
    username character varying NOT NULL,
    password character varying NOT NULL,
    disable_plugin_check boolean DEFAULT false NOT NULL
);


ALTER TABLE alert.configuration_jira_server OWNER TO sa;

--
-- Name: configuration_non_proxy_hosts; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.configuration_non_proxy_hosts (
    configuration_id uuid NOT NULL,
    hostname_pattern character varying NOT NULL
);


ALTER TABLE alert.configuration_non_proxy_hosts OWNER TO sa;

--
-- Name: configuration_proxy; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.configuration_proxy (
    configuration_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    last_updated timestamp with time zone,
    host character varying NOT NULL,
    port bigint,
    username character varying,
    password character varying
);


ALTER TABLE alert.configuration_proxy OWNER TO sa;

--
-- Name: custom_certificates; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.custom_certificates (
    id bigint NOT NULL,
    alias character varying(128),
    certificate_content character varying,
    last_updated timestamp without time zone
);


ALTER TABLE alert.custom_certificates OWNER TO sa;

--
-- Name: custom_certificates_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.custom_certificates_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.custom_certificates_id_seq OWNER TO sa;

--
-- Name: custom_certificates_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.custom_certificates_id_seq OWNED BY alert.custom_certificates.id;


--
-- Name: defined_fields; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.defined_fields (
    id bigint NOT NULL,
    source_key character varying(255) NOT NULL,
    sensitive boolean DEFAULT false NOT NULL
);


ALTER TABLE alert.defined_fields OWNER TO sa;

--
-- Name: defined_fields_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.defined_fields_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.defined_fields_id_seq OWNER TO sa;

--
-- Name: defined_fields_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.defined_fields_id_seq OWNED BY alert.defined_fields.id;


--
-- Name: descriptor_configs; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.descriptor_configs (
    id bigint NOT NULL,
    descriptor_id bigint,
    context_id bigint,
    created_at timestamp with time zone,
    last_updated timestamp with time zone
);


ALTER TABLE alert.descriptor_configs OWNER TO sa;

--
-- Name: descriptor_configs_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.descriptor_configs_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.descriptor_configs_id_seq OWNER TO sa;

--
-- Name: descriptor_configs_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.descriptor_configs_id_seq OWNED BY alert.descriptor_configs.id;


--
-- Name: descriptor_fields; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.descriptor_fields (
    descriptor_id bigint NOT NULL,
    field_id bigint NOT NULL
);


ALTER TABLE alert.descriptor_fields OWNER TO sa;

--
-- Name: descriptor_types; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.descriptor_types (
    id bigint NOT NULL,
    type character varying(255)
);


ALTER TABLE alert.descriptor_types OWNER TO sa;

--
-- Name: descriptor_types_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.descriptor_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.descriptor_types_id_seq OWNER TO sa;

--
-- Name: descriptor_types_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.descriptor_types_id_seq OWNED BY alert.descriptor_types.id;


--
-- Name: distribution_jobs; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.distribution_jobs (
    job_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    distribution_frequency character varying DEFAULT 'REAL_TIME'::character varying NOT NULL,
    processing_type character varying DEFAULT 'DEFAULT'::character varying NOT NULL,
    channel_descriptor_name character varying NOT NULL,
    created_at timestamp with time zone NOT NULL,
    last_updated timestamp with time zone,
    channel_global_config_id uuid
);


ALTER TABLE alert.distribution_jobs OWNER TO sa;

--
-- Name: email_job_additional_email_addresses; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.email_job_additional_email_addresses (
    job_id uuid NOT NULL,
    email_address character varying NOT NULL
);


ALTER TABLE alert.email_job_additional_email_addresses OWNER TO sa;

--
-- Name: email_job_details; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.email_job_details (
    job_id uuid NOT NULL,
    subject_line character varying,
    project_owner_only boolean DEFAULT false NOT NULL,
    additional_email_addresses_only boolean DEFAULT false NOT NULL,
    attachment_file_type character varying
);


ALTER TABLE alert.email_job_details OWNER TO sa;

--
-- Name: field_contexts; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.field_contexts (
    field_id bigint NOT NULL,
    context_id bigint NOT NULL
);


ALTER TABLE alert.field_contexts OWNER TO sa;

--
-- Name: field_values; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.field_values (
    id bigint NOT NULL,
    config_id bigint,
    field_id bigint,
    field_value character varying
);


ALTER TABLE alert.field_values OWNER TO sa;

--
-- Name: field_values_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.field_values_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.field_values_id_seq OWNER TO sa;

--
-- Name: field_values_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.field_values_id_seq OWNED BY alert.field_values.id;


--
-- Name: jira_cloud_job_custom_fields; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.jira_cloud_job_custom_fields (
    job_id uuid NOT NULL,
    field_name character varying NOT NULL,
    field_value character varying
);


ALTER TABLE alert.jira_cloud_job_custom_fields OWNER TO sa;

--
-- Name: jira_cloud_job_details; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.jira_cloud_job_details (
    job_id uuid NOT NULL,
    add_comments boolean DEFAULT false NOT NULL,
    issue_creator_email character varying,
    project_name_or_key character varying NOT NULL,
    issue_type character varying NOT NULL,
    resolve_transition character varying,
    reopen_transition character varying,
    issue_summary character varying
);


ALTER TABLE alert.jira_cloud_job_details OWNER TO sa;

--
-- Name: jira_server_job_custom_fields; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.jira_server_job_custom_fields (
    job_id uuid NOT NULL,
    field_name character varying NOT NULL,
    field_value character varying
);


ALTER TABLE alert.jira_server_job_custom_fields OWNER TO sa;

--
-- Name: jira_server_job_details; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.jira_server_job_details (
    job_id uuid NOT NULL,
    add_comments boolean DEFAULT false NOT NULL,
    issue_creator_username character varying,
    project_name_or_key character varying NOT NULL,
    issue_type character varying NOT NULL,
    resolve_transition character varying,
    reopen_transition character varying,
    issue_summary character varying
);


ALTER TABLE alert.jira_server_job_details OWNER TO sa;

--
-- Name: job_notification_relation; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.job_notification_relation (
    correlation_id uuid NOT NULL,
    job_id uuid NOT NULL,
    notification_id bigint NOT NULL
);


ALTER TABLE alert.job_notification_relation OWNER TO sa;

--
-- Name: job_sub_task_status; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.job_sub_task_status (
    id uuid NOT NULL,
    job_id uuid NOT NULL,
    remaining_event_count bigint NOT NULL,
    notification_correlation_id uuid NOT NULL
);


ALTER TABLE alert.job_sub_task_status OWNER TO sa;

--
-- Name: ms_teams_job_details; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.ms_teams_job_details (
    job_id uuid NOT NULL,
    webhook character varying NOT NULL
);


ALTER TABLE alert.ms_teams_job_details OWNER TO sa;

--
-- Name: notification_correlation_to_notification_relation; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.notification_correlation_to_notification_relation (
    notification_correlation_id uuid NOT NULL,
    notification_id bigint NOT NULL
);


ALTER TABLE alert.notification_correlation_to_notification_relation OWNER TO sa;

--
-- Name: oauth_credentials; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.oauth_credentials (
    configuration_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    access_token uuid,
    refresh_token uuid,
    exipiration_time_ms bigint DEFAULT 0
);


ALTER TABLE alert.oauth_credentials OWNER TO sa;

--
-- Name: permission_matrix; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.permission_matrix (
    role_id bigint NOT NULL,
    operations integer NOT NULL,
    descriptor_id bigint NOT NULL,
    context_id bigint NOT NULL
);


ALTER TABLE alert.permission_matrix OWNER TO sa;

--
-- Name: provider_task_properties; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.provider_task_properties (
    provider_config_id bigint,
    task_name character varying NOT NULL,
    property_name character varying NOT NULL,
    value character varying
);


ALTER TABLE alert.provider_task_properties OWNER TO sa;

--
-- Name: raw_notification_content; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.raw_notification_content (
    id bigint NOT NULL,
    created_at timestamp with time zone,
    provider character varying(255),
    provider_creation_time timestamp with time zone,
    notification_type character varying(255),
    content character varying,
    provider_config_id bigint NOT NULL,
    processed boolean DEFAULT false
);


ALTER TABLE alert.raw_notification_content OWNER TO sa;

--
-- Name: raw_notification_content_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.raw_notification_content_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.raw_notification_content_id_seq OWNER TO sa;

--
-- Name: raw_notification_content_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.raw_notification_content_id_seq OWNED BY alert.raw_notification_content.id;


--
-- Name: registered_descriptors; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.registered_descriptors (
    id bigint NOT NULL,
    type_id bigint,
    name character varying(255)
);


ALTER TABLE alert.registered_descriptors OWNER TO sa;

--
-- Name: registered_descriptors_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.registered_descriptors_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.registered_descriptors_id_seq OWNER TO sa;

--
-- Name: registered_descriptors_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.registered_descriptors_id_seq OWNED BY alert.registered_descriptors.id;


--
-- Name: roles; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.roles (
    id bigint NOT NULL,
    rolename character varying(255),
    custom boolean DEFAULT false NOT NULL
);


ALTER TABLE alert.roles OWNER TO sa;

--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.roles_id_seq OWNER TO sa;

--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.roles_id_seq OWNED BY alert.roles.id;


--
-- Name: settings_key; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.settings_key (
    id bigint NOT NULL,
    key character varying(255),
    value character varying(255)
);


ALTER TABLE alert.settings_key OWNER TO sa;

--
-- Name: settings_key_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.settings_key_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.settings_key_id_seq OWNER TO sa;

--
-- Name: settings_key_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.settings_key_id_seq OWNED BY alert.settings_key.id;


--
-- Name: slack_job_details; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.slack_job_details (
    job_id uuid NOT NULL,
    webhook character varying NOT NULL,
    channel_name character varying NOT NULL,
    channel_username character varying
);


ALTER TABLE alert.slack_job_details OWNER TO sa;

--
-- Name: system_messages; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.system_messages (
    id bigint NOT NULL,
    created_at timestamp with time zone,
    severity character varying(50),
    content character varying(255),
    type character varying(255)
);


ALTER TABLE alert.system_messages OWNER TO sa;

--
-- Name: system_messages_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.system_messages_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.system_messages_id_seq OWNER TO sa;

--
-- Name: system_messages_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.system_messages_id_seq OWNED BY alert.system_messages.id;


--
-- Name: system_status; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.system_status (
    id bigint NOT NULL,
    initialized_configuration boolean,
    startup_time timestamp with time zone
);


ALTER TABLE alert.system_status OWNER TO sa;

--
-- Name: user_roles; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.user_roles (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL
);


ALTER TABLE alert.user_roles OWNER TO sa;

--
-- Name: users; Type: TABLE; Schema: alert; Owner: sa
--

CREATE TABLE alert.users (
    id bigint NOT NULL,
    username character varying(2048),
    password character varying(2048),
    email_address character varying(2048),
    expired boolean DEFAULT false,
    locked boolean DEFAULT false,
    password_expired boolean DEFAULT false,
    enabled boolean DEFAULT true,
    auth_type bigint DEFAULT 1 NOT NULL
);


ALTER TABLE alert.users OWNER TO sa;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: alert; Owner: sa
--

CREATE SEQUENCE alert.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert.users_id_seq OWNER TO sa;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: alert; Owner: sa
--

ALTER SEQUENCE alert.users_id_seq OWNED BY alert.users.id;


--
-- Name: databasechangelog; Type: TABLE; Schema: public; Owner: sa
--

CREATE TABLE public.databasechangelog (
    id character varying(255) NOT NULL,
    author character varying(255) NOT NULL,
    filename character varying(255) NOT NULL,
    dateexecuted timestamp without time zone NOT NULL,
    orderexecuted integer NOT NULL,
    exectype character varying(10) NOT NULL,
    md5sum character varying(35),
    description character varying(255),
    comments character varying(255),
    tag character varying(255),
    liquibase character varying(20),
    contexts character varying(255),
    labels character varying(255),
    deployment_id character varying(10)
);


ALTER TABLE public.databasechangelog OWNER TO sa;

--
-- Name: databasechangeloglock; Type: TABLE; Schema: public; Owner: sa
--

CREATE TABLE public.databasechangeloglock (
    id integer NOT NULL,
    locked boolean NOT NULL,
    lockgranted timestamp without time zone,
    lockedby character varying(255)
);


ALTER TABLE public.databasechangeloglock OWNER TO sa;

--
-- Name: audit_entries id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.audit_entries ALTER COLUMN id SET DEFAULT nextval('alert.audit_entries_id_seq'::regclass);


--
-- Name: authentication_type id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.authentication_type ALTER COLUMN id SET DEFAULT nextval('alert.authentication_type_id_seq'::regclass);


--
-- Name: config_contexts id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.config_contexts ALTER COLUMN id SET DEFAULT nextval('alert.config_contexts_id_seq'::regclass);


--
-- Name: custom_certificates id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.custom_certificates ALTER COLUMN id SET DEFAULT nextval('alert.custom_certificates_id_seq'::regclass);


--
-- Name: defined_fields id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.defined_fields ALTER COLUMN id SET DEFAULT nextval('alert.defined_fields_id_seq'::regclass);


--
-- Name: descriptor_configs id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.descriptor_configs ALTER COLUMN id SET DEFAULT nextval('alert.descriptor_configs_id_seq'::regclass);


--
-- Name: descriptor_types id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.descriptor_types ALTER COLUMN id SET DEFAULT nextval('alert.descriptor_types_id_seq'::regclass);


--
-- Name: field_values id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.field_values ALTER COLUMN id SET DEFAULT nextval('alert.field_values_id_seq'::regclass);


--
-- Name: raw_notification_content id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.raw_notification_content ALTER COLUMN id SET DEFAULT nextval('alert.raw_notification_content_id_seq'::regclass);


--
-- Name: registered_descriptors id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.registered_descriptors ALTER COLUMN id SET DEFAULT nextval('alert.registered_descriptors_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.roles ALTER COLUMN id SET DEFAULT nextval('alert.roles_id_seq'::regclass);


--
-- Name: settings_key id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.settings_key ALTER COLUMN id SET DEFAULT nextval('alert.settings_key_id_seq'::regclass);


--
-- Name: system_messages id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.system_messages ALTER COLUMN id SET DEFAULT nextval('alert.system_messages_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.users ALTER COLUMN id SET DEFAULT nextval('alert.users_id_seq'::regclass);


--
-- Data for Name: audit_entries; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.audit_entries (id, error_message, error_stack_trace, status, time_created, time_last_sent, common_config_id) FROM stdin;
\.


--
-- Data for Name: audit_notification_relation; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.audit_notification_relation (audit_entry_id, notification_id) FROM stdin;
\.


--
-- Data for Name: authentication_type; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.authentication_type (id, name) FROM stdin;
1	DATABASE
2	LDAP
3	SAML
\.


--
-- Data for Name: azure_boards_job_details; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.azure_boards_job_details (job_id, add_comments, project_name_or_id, work_item_type, work_item_completed_state, work_item_reopen_state) FROM stdin;
\.


--
-- Data for Name: blackduck_job_details; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.blackduck_job_details (job_id, global_config_id, filter_by_project, project_name_pattern, project_version_name_pattern) FROM stdin;
67c15b13-d04d-4471-bc78-d6bc2fccf84d	15	f	\N	\N
\.


--
-- Data for Name: blackduck_job_notification_types; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.blackduck_job_notification_types (job_id, notification_type) FROM stdin;
67c15b13-d04d-4471-bc78-d6bc2fccf84d	PROJECT
67c15b13-d04d-4471-bc78-d6bc2fccf84d	BOM_EDIT
67c15b13-d04d-4471-bc78-d6bc2fccf84d	PROJECT_VERSION
67c15b13-d04d-4471-bc78-d6bc2fccf84d	LICENSE_LIMIT
67c15b13-d04d-4471-bc78-d6bc2fccf84d	RULE_VIOLATION_CLEARED
67c15b13-d04d-4471-bc78-d6bc2fccf84d	VULNERABILITY
67c15b13-d04d-4471-bc78-d6bc2fccf84d	COMPONENT_UNKNOWN_VERSION
67c15b13-d04d-4471-bc78-d6bc2fccf84d	RULE_VIOLATION
67c15b13-d04d-4471-bc78-d6bc2fccf84d	POLICY_OVERRIDE
\.


--
-- Data for Name: blackduck_job_policy_filters; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.blackduck_job_policy_filters (job_id, policy_name) FROM stdin;
\.


--
-- Data for Name: blackduck_job_projects; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.blackduck_job_projects (job_id, project_name, href) FROM stdin;
\.


--
-- Data for Name: blackduck_job_vulnerability_severity_filters; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.blackduck_job_vulnerability_severity_filters (job_id, severity_name) FROM stdin;
\.


--
-- Data for Name: config_contexts; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.config_contexts (id, context) FROM stdin;
1	GLOBAL
2	DISTRIBUTION
\.


--
-- Data for Name: configuration_azure_boards; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.configuration_azure_boards (configuration_id, name, created_at, last_updated, organization_name, app_id, client_secret) FROM stdin;
\.


--
-- Data for Name: configuration_email; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.configuration_email (configuration_id, name, created_at, last_updated, smtp_host, smtp_from, port, auth_required, auth_username, auth_password) FROM stdin;
d997d58e-451b-4254-9f36-2e07de557fe3	default-configuration	2022-10-27 11:23:21.311378+00	2022-10-27 11:23:21.311385+00	mailserver.example.com	user@example.com	\N	f	\N	\N
\.


--
-- Data for Name: configuration_email_properties; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.configuration_email_properties (configuration_id, property_key, property_value) FROM stdin;
\.


--
-- Data for Name: configuration_jira_server; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.configuration_jira_server (configuration_id, name, created_at, last_updated, url, username, password, disable_plugin_check) FROM stdin;
\.


--
-- Data for Name: configuration_non_proxy_hosts; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.configuration_non_proxy_hosts (configuration_id, hostname_pattern) FROM stdin;
\.


--
-- Data for Name: configuration_proxy; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.configuration_proxy (configuration_id, name, created_at, last_updated, host, port, username, password) FROM stdin;
\.


--
-- Data for Name: custom_certificates; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.custom_certificates (id, alias, certificate_content, last_updated) FROM stdin;
\.


--
-- Data for Name: defined_fields; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.defined_fields (id, source_key, sensitive) FROM stdin;
1	blackduck.url	f
2	blackduck.api.key	t
3	blackduck.timeout	f
4	channel.common.filter.by.project	f
5	channel.common.project.name.pattern	f
6	channel.common.configured.project	f
7	provider.distribution.notification.types	f
9	email.subject.line	f
10	project.owner.only	f
11	email.addresses	f
12	channel.common.name	f
13	channel.common.channel.name	f
14	channel.common.provider.name	f
15	channel.common.frequency	f
16	mail.smtp.user	f
17	mail.smtp.host	f
18	mail.smtp.port	f
19	mail.smtp.connectiontimeout	f
20	mail.smtp.timeout	f
21	mail.smtp.writetimeout	f
22	mail.smtp.from	f
23	mail.smtp.localhost	f
24	mail.smtp.localaddress	f
25	mail.smtp.localport	f
26	mail.smtp.ehlo	f
27	mail.smtp.auth	f
28	mail.smtp.auth.mechanisms	f
29	mail.smtp.auth.login.disable	f
30	mail.smtp.auth.plain.disable	f
31	mail.smtp.auth.digest-md5.disable	f
32	mail.smtp.auth.ntlm.disable	f
33	mail.smtp.auth.ntlm.domain	f
34	mail.smtp.auth.ntlm.flags	f
35	mail.smtp.auth.xoauth2.disable	f
36	mail.smtp.submitter	f
37	mail.smtp.dsn.notify	f
38	mail.smtp.dsn.ret	f
39	mail.smtp.allow8bitmime	f
40	mail.smtp.sendpartial	f
41	mail.smtp.sasl.enable	f
42	mail.smtp.sasl.mechanisms	f
43	mail.smtp.sasl.authorizationid	f
44	mail.smtp.sasl.realm	f
45	mail.smtp.sasl.usecanonicalhostname	f
46	mail.smtp.quitwait	f
47	mail.smtp.reportsuccess	f
48	mail.smtp.ssl.enable	f
49	mail.smtp.ssl.checkserveridentity	f
50	mail.smtp.ssl.trust	f
51	mail.smtp.ssl.protocols	f
52	mail.smtp.ssl.ciphersuites	f
53	mail.smtp.starttls.enable	f
54	mail.smtp.starttls.required	f
55	mail.smtp.proxy.host	f
56	mail.smtp.proxy.port	f
57	mail.smtp.socks.host	f
58	mail.smtp.socks.port	f
59	mail.smtp.mailextension	f
60	mail.smtp.userset	f
61	mail.smtp.noop.strict	f
62	mail.smtp.password	t
63	channel.slack.webhook	f
64	channel.slack.channel.name	f
65	channel.slack.channel.username	f
66	settings.proxy.host	f
67	settings.proxy.port	f
68	settings.proxy.username	f
69	settings.proxy.password	t
70	settings.ldap.enabled	f
71	settings.ldap.server	f
72	settings.ldap.manager.dn	f
73	settings.ldap.manager.password	t
74	settings.ldap.authentication.type	f
75	settings.ldap.referral	f
76	settings.ldap.user.search.base	f
77	settings.ldap.user.search.filter	f
78	settings.ldap.user.dn.patterns	f
79	settings.ldap.user.attributes	f
80	settings.ldap.group.search.base	f
81	settings.ldap.group.search.filter	f
82	settings.ldap.group.role.attribute	f
83	scheduling.daily.processor.hour	f
84	scheduling.daily.processor.next.run	f
85	scheduling.purge.data.frequency	f
86	scheduling.purge.data.next.run	f
89	settings.encryption.password	t
90	settings.encryption.global.salt	t
92	settings.saml.enabled	f
93	settings.saml.force.auth	f
94	settings.saml.metadata.url	f
95	settings.saml.entity.id	f
96	settings.saml.entity.base.url	f
97	jira.cloud.url	f
98	jira.cloud.admin.email.address	f
99	jira.cloud.admin.api.token	t
100	jira.cloud.configure.plugin	f
101	channel.jira.cloud.add.comments	f
102	channel.jira.cloud.issue.creator	f
103	channel.jira.cloud.project.name	f
104	channel.jira.cloud.issue.type	f
105	channel.jira.cloud.resolve.workflow	f
106	channel.jira.cloud.reopen.workflow	f
107	email.additional.addresses	f
108	email.additional.addresses.only	f
109	settings.saml.metadata.file	f
113	settings.saml.role.attribute.mapping.name	f
114	channel.msteams.webhook	f
115	channel.jira.server.add.comments	f
116	channel.jira.server.issue.creator	f
117	channel.jira.server.project.name	f
118	channel.jira.server.issue.type	f
119	channel.jira.server.resolve.workflow	f
120	channel.jira.server.reopen.workflow	f
121	jira.server.url	f
8	provider.distribution.processing.type	f
122	jira.server.username	f
123	jira.server.password	t
124	jira.server.configure.plugin	f
125	channel.common.enabled	f
126	blackduck.policy.notification.filter	f
127	blackduck.vulnerability.notification.filter	f
128	email.attachment.format	f
129	provider.common.config.enabled	f
130	provider.common.config.name	f
131	channel.azure.boards.work.item.comment	f
132	channel.azure.boards.project	f
133	channel.azure.boards.work.item.type	f
134	channel.azure.boards.work.item.completed.state	f
135	channel.azure.boards.work.item.reopen.state	f
136	azure.boards.url	f
137	azure.boards.organization.name	f
138	azure.boards.client.id	t
139	azure.boards.client.secret	t
140	azure.boards.oauth.user.email	f
141	azure.boards.oauth	f
142	azure.boards.access.token	t
143	azure.boards.refresh.token	t
144	azure.boards.token.expiration.millis	f
145	provider.common.config.id	f
146	jira.cloud.disable.plugin.check	f
147	jira.server.disable.plugin.check	f
148	channel.jira.cloud.field.mapping	f
149	channel.jira.server.field.mapping	f
150	settings.saml.want.assertions.signed	f
151	channel.jira.cloud.issue.summary	f
152	channel.jira.server.issue.summary	f
153	settings.proxy.non.proxy.hosts	f
154	channel.common.project.version.name.pattern	f
155	channel.common.global.config.id	f
\.


--
-- Data for Name: descriptor_configs; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.descriptor_configs (id, descriptor_id, context_id, created_at, last_updated) FROM stdin;
1	4	1	2022-10-27 11:15:46.884823+00	2022-10-27 11:15:46.884823+00
2	5	1	2022-10-27 11:15:46.936383+00	2022-10-27 11:15:46.936383+00
3	7	1	2022-10-27 11:15:46.966743+00	2022-10-27 11:15:46.966743+00
4	1	1	2022-10-27 11:15:46.993631+00	2022-10-27 11:15:46.993631+00
5	13	1	2022-10-27 11:15:47.023426+00	2022-10-27 11:15:47.023426+00
6	2	1	2022-10-27 11:15:47.055956+00	2022-10-27 11:15:47.055956+00
7	3	1	2022-10-27 11:15:47.079992+00	2022-10-27 11:15:47.079992+00
8	14	1	2022-10-27 11:15:47.106919+00	2022-10-27 11:15:47.106919+00
10	10	1	2022-10-27 11:15:47.152966+00	2022-10-27 11:15:47.152966+00
11	6	1	2022-10-27 11:15:47.172321+00	2022-10-27 11:15:47.172321+00
12	8	1	2022-10-27 11:15:47.191822+00	2022-10-27 11:15:47.191822+00
13	11	1	2022-10-27 11:15:47.227932+00	2022-10-27 11:15:47.227932+00
14	12	1	2022-10-27 11:15:47.28652+00	2022-10-27 11:15:47.28652+00
9	9	1	2022-10-27 11:15:47.133273+00	2022-10-27 11:15:47.736366+00
15	1	1	2022-10-27 11:23:09.324737+00	2022-10-27 11:23:09.324737+00
\.


--
-- Data for Name: descriptor_fields; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.descriptor_fields (descriptor_id, field_id) FROM stdin;
1	1
1	2
1	3
1	4
1	5
1	6
1	7
1	8
1	126
1	127
2	9
2	10
2	11
2	12
2	13
2	14
2	15
2	16
2	17
2	18
2	19
2	20
2	21
2	22
2	23
2	24
2	25
2	26
2	27
2	28
2	29
2	30
2	31
2	32
2	33
2	34
2	35
2	36
2	37
2	38
2	39
2	40
2	41
2	42
2	43
2	44
2	45
2	46
2	47
2	48
2	49
2	50
2	51
2	52
2	53
2	54
2	55
2	56
2	57
2	58
2	59
2	60
2	61
2	62
2	107
2	108
2	125
2	128
3	12
3	13
3	14
3	15
3	63
3	64
3	65
3	125
4	66
4	67
4	68
4	69
4	89
4	90
5	83
5	84
5	85
5	86
6	4
6	5
6	6
6	7
6	8
7	12
7	13
7	14
7	15
7	97
7	98
7	99
7	100
7	101
7	102
7	103
7	104
7	105
7	106
7	125
8	12
8	13
8	14
8	15
8	114
8	125
9	70
9	71
9	72
9	73
9	74
9	75
9	76
9	77
9	78
9	79
9	80
9	81
9	82
9	92
9	93
9	94
9	95
9	96
9	109
9	113
10	12
10	13
10	14
10	15
10	115
10	116
10	117
10	118
10	119
10	120
10	121
10	122
10	123
10	125
1	129
1	130
14	125
14	12
14	13
14	14
14	15
14	131
14	132
14	133
14	134
14	135
14	136
14	137
14	138
14	139
14	140
14	141
14	142
14	143
14	144
1	145
7	146
10	147
7	148
10	149
10	124
9	150
7	151
10	152
4	153
1	154
14	155
2	155
7	155
10	155
8	155
3	155
\.


--
-- Data for Name: descriptor_types; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.descriptor_types (id, type) FROM stdin;
1	CHANNEL
2	PROVIDER
3	COMPONENT
\.


--
-- Data for Name: distribution_jobs; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.distribution_jobs (job_id, name, enabled, distribution_frequency, processing_type, channel_descriptor_name, created_at, last_updated, channel_global_config_id) FROM stdin;
67c15b13-d04d-4471-bc78-d6bc2fccf84d	example Job	t	DAILY	DEFAULT	channel_email	2022-10-27 11:23:44.966044+00	\N	\N
\.


--
-- Data for Name: email_job_additional_email_addresses; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.email_job_additional_email_addresses (job_id, email_address) FROM stdin;
\.


--
-- Data for Name: email_job_details; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.email_job_details (job_id, subject_line, project_owner_only, additional_email_addresses_only, attachment_file_type) FROM stdin;
67c15b13-d04d-4471-bc78-d6bc2fccf84d	\N	f	f	
\.


--
-- Data for Name: field_contexts; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.field_contexts (field_id, context_id) FROM stdin;
1	1
2	1
3	1
4	2
5	2
6	2
7	2
8	2
9	2
10	2
11	2
12	2
13	2
14	2
15	2
16	1
17	1
18	1
19	1
20	1
21	1
22	1
23	1
24	1
25	1
26	1
27	1
28	1
29	1
30	1
31	1
32	1
33	1
34	1
35	1
36	1
37	1
38	1
39	1
40	1
41	1
42	1
43	1
44	1
45	1
46	1
47	1
48	1
49	1
50	1
51	1
52	1
53	1
54	1
55	1
56	1
57	1
58	1
59	1
60	1
61	1
62	1
63	2
64	2
65	2
66	1
67	1
68	1
69	1
70	1
71	1
72	1
73	1
74	1
75	1
76	1
77	1
78	1
79	1
80	1
81	1
82	1
83	1
84	1
85	1
86	1
89	1
90	1
92	1
93	1
94	1
95	1
96	1
97	1
98	1
99	1
100	1
101	2
102	2
103	2
104	2
105	2
106	2
107	2
108	2
109	1
113	1
114	2
115	2
116	2
117	2
118	2
119	2
120	2
121	1
122	1
123	1
124	1
125	2
126	2
127	2
128	2
129	1
130	1
131	2
132	2
133	2
134	2
135	2
136	1
137	1
138	1
139	1
140	1
141	1
142	1
143	1
144	1
145	2
146	1
147	1
148	2
149	2
150	1
151	2
152	2
153	1
154	2
155	2
\.


--
-- Data for Name: field_values; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.field_values (id, config_id, field_id, field_value) FROM stdin;
1	9	92	false
2	15	2	82a82cafd75e60163d67935b7fe4b1c9ebaf11ecfd31940b11d764c1c59d774c075c7de7858768f08bcb119bc25d4713c54fa247455e2ca7cdf51de11199de55441a0f835859fd4db0b865917283cbc3ecc0b42486348ba4af37d82b2304a791d216b8c0d2b703e9dcc90ee911a8fccdc9571bec203c24df9cfecba33214f6caa905b7f96c31e4
3	15	129	true
4	15	1	https://blackduck.server.example.com
5	15	3	300
6	15	130	example-provider
\.


--
-- Data for Name: jira_cloud_job_custom_fields; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.jira_cloud_job_custom_fields (job_id, field_name, field_value) FROM stdin;
\.


--
-- Data for Name: jira_cloud_job_details; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.jira_cloud_job_details (job_id, add_comments, issue_creator_email, project_name_or_key, issue_type, resolve_transition, reopen_transition, issue_summary) FROM stdin;
\.


--
-- Data for Name: jira_server_job_custom_fields; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.jira_server_job_custom_fields (job_id, field_name, field_value) FROM stdin;
\.


--
-- Data for Name: jira_server_job_details; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.jira_server_job_details (job_id, add_comments, issue_creator_username, project_name_or_key, issue_type, resolve_transition, reopen_transition, issue_summary) FROM stdin;
\.


--
-- Data for Name: job_notification_relation; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.job_notification_relation (correlation_id, job_id, notification_id) FROM stdin;
\.


--
-- Data for Name: job_sub_task_status; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.job_sub_task_status (id, job_id, remaining_event_count, notification_correlation_id) FROM stdin;
\.


--
-- Data for Name: ms_teams_job_details; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.ms_teams_job_details (job_id, webhook) FROM stdin;
\.


--
-- Data for Name: notification_correlation_to_notification_relation; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.notification_correlation_to_notification_relation (notification_correlation_id, notification_id) FROM stdin;
\.


--
-- Data for Name: oauth_credentials; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.oauth_credentials (configuration_id, access_token, refresh_token, exipiration_time_ms) FROM stdin;
\.


--
-- Data for Name: permission_matrix; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.permission_matrix (role_id, operations, descriptor_id, context_id) FROM stdin;
1	255	1	1
1	255	1	2
1	255	2	1
1	255	2	2
1	255	3	1
1	255	3	2
1	255	4	1
1	255	5	1
1	255	6	1
1	255	7	1
1	255	7	2
1	255	8	1
1	255	8	2
1	255	9	1
1	255	10	1
1	255	10	2
1	255	11	1
2	4	5	1
2	20	1	1
2	20	2	1
2	20	3	1
2	20	6	1
2	20	7	1
2	20	8	1
2	20	10	1
2	255	1	2
2	255	2	2
2	255	3	2
2	255	7	2
2	255	8	2
2	255	10	2
3	4	1	2
3	4	2	2
3	4	3	2
3	4	7	2
3	4	8	2
3	4	10	2
1	255	12	1
2	0	12	1
1	255	13	1
2	4	13	1
1	255	14	1
1	255	14	2
2	20	14	1
2	255	14	2
3	4	14	2
\.


--
-- Data for Name: provider_task_properties; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.provider_task_properties (provider_config_id, task_name, property_name, value) FROM stdin;
\.


--
-- Data for Name: raw_notification_content; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.raw_notification_content (id, created_at, provider, provider_creation_time, notification_type, content, provider_config_id, processed) FROM stdin;
\.


--
-- Data for Name: registered_descriptors; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.registered_descriptors (id, type_id, name) FROM stdin;
1	2	provider_blackduck
2	1	channel_email
3	1	channel_slack
4	3	component_settings
5	3	component_scheduling
6	3	component_audit
7	1	channel_jira_cloud
8	1	msteamskey
9	3	component_authentication
10	1	channel_jira_server
11	3	component_users
12	3	component_certificates
13	3	component_tasks
14	1	channel_azure_boards
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.roles (id, rolename, custom) FROM stdin;
1	ALERT_ADMIN	f
2	ALERT_JOB_MANAGER	f
3	ALERT_USER	f
\.


--
-- Data for Name: settings_key; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.settings_key (id, key, value) FROM stdin;
\.


--
-- Data for Name: slack_job_details; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.slack_job_details (job_id, webhook, channel_name, channel_username) FROM stdin;
\.


--
-- Data for Name: system_messages; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.system_messages (id, created_at, severity, content, type) FROM stdin;
1	2022-10-27 11:15:47.535029+00	ERROR	Default admin user email missing	DEFAULT_ADMIN_USER_ERROR
2	2022-10-27 11:15:47.62232+00	WARNING	Black Duck configuration is invalid. Black Duck configurations missing.	BLACKDUCK_PROVIDER_CONFIGURATION_MISSING
3	2022-10-27 11:23:09.672369+00	WARNING	Can not connect to the Black Duck server with the configuration 'example-provider'.	15_BLACKDUCK_PROVIDER_CONNECTIVITY
\.


--
-- Data for Name: system_status; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.system_status (id, initialized_configuration, startup_time) FROM stdin;
1	f	2022-10-27 11:15:46.7575+00
\.


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.user_roles (user_id, role_id) FROM stdin;
1	1
2	2
3	3
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: alert; Owner: sa
--

COPY alert.users (id, username, password, email_address, expired, locked, password_expired, enabled, auth_type) FROM stdin;
1	sysadmin	$2a$16$Q3wfnhwA.1Qm3Tz3IkqDC.743C5KI7nJIuYlZ4xKXre/WBYpjUEFy	\N	f	f	f	t	1
2	jobmanager	$2a$16$Ek6E2PbHCTLrlsqSPuNsxexjcViC3n1Gl6AW2sl/yR3Vf52xTazui	\N	f	f	f	t	1
3	alertuser	$2a$16$Ek6E2PbHCTLrlsqSPuNsxexjcViC3n1Gl6AW2sl/yR3Vf52xTazui	\N	f	f	f	t	1
\.


--
-- Data for Name: databasechangelog; Type: TABLE DATA; Schema: public; Owner: sa
--

COPY public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) FROM stdin;
2019-12-11-10-36-41-001	gavink	liquibase/6.0.0/init-config-db.xml	2022-10-27 07:15:31.089863	1	EXECUTED	8:f2b3df00dc164ea13e3f6a33f223f26a	sqlFile		\N	4.9.1	\N	\N	6869329870
2019-12-13-13-57-17-245	gavink	liquibase/6.0.0/init-config-db.xml	2022-10-27 07:15:31.121609	2	EXECUTED	8:f360cd827d44b451a475a1ddef102669	createProcedure; createProcedure; createProcedure; createProcedure		\N	4.9.1	\N	\N	6869329870
2019-12-11-11-02-46-045	gavink	liquibase/6.0.0/init-config-db.xml	2022-10-27 07:15:31.153267	3	EXECUTED	8:46a38c01103c587a40358447f9b79131	sql; sql		\N	4.9.1	\N	\N	6869329870
2019-12-11-11-35-48-436	gavink	liquibase/6.0.0/init-config-db.xml	2022-10-27 07:15:31.179791	4	EXECUTED	8:b880e6604673e37dfaf8c4f2d309ae4c	sql; sql; sql		\N	4.9.1	\N	\N	6869329870
2019-12-11-12-04-43-379	gavink	liquibase/6.0.0/init-config-db.xml	2022-10-27 07:15:31.217304	5	EXECUTED	8:201632792a3013fec9c6bef007f03b61	insert tableName=REGISTERED_DESCRIPTORS; insert tableName=REGISTERED_DESCRIPTORS; insert tableName=REGISTERED_DESCRIPTORS; insert tableName=REGISTERED_DESCRIPTORS; insert tableName=REGISTERED_DESCRIPTORS; insert tableName=REGISTERED_DESCRIPTORS; i...		\N	4.9.1	\N	\N	6869329870
2019-12-11-12-28-09-422	gavink	liquibase/6.0.0/init-config-db.xml	2022-10-27 07:15:31.426036	6	EXECUTED	8:dd4141a37485083054a1bacdaebcee08	insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName...		\N	4.9.1	\N	\N	6869329870
2019-12-11-14-14-32-932	gavink	liquibase/6.0.0/init-config-db.xml	2022-10-27 07:15:31.642126	7	EXECUTED	8:2acb530f5b16d4d18d8343bd7a7f24fa	insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIE...		\N	4.9.1	\N	\N	6869329870
2019-12-11-14-38-14-264	gavink	liquibase/6.0.0/init-config-db.xml	2022-10-27 07:15:31.809405	8	EXECUTED	8:9bf1943b0aeb804607135659890f3302	insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS; insert tableName...		\N	4.9.1	\N	\N	6869329870
2019-12-17-09-02-35-275	gavink	liquibase/6.0.0/init-auth-db.xml	2022-10-27 07:15:31.821639	9	EXECUTED	8:3850c369f142ab34e88411a7bfc7077f	createProcedure; createProcedure; createProcedure		\N	4.9.1	\N	\N	6869329870
2019-12-12-08-44-02-287	gavink	liquibase/6.0.0/init-auth-db.xml	2022-10-27 07:15:31.841357	10	EXECUTED	8:32b704df8f27a8c42dd700352a1c2554	insert tableName=ROLES; insert tableName=ROLES; insert tableName=ROLES		\N	4.9.1	\N	\N	6869329870
2020-01-10-14-21-18-101	gavink	liquibase/6.0.0/init-auth-db.xml	2022-10-27 07:15:31.860211	11	EXECUTED	8:ab7bb9d22edb357c1ef1e2fee8823bd2	insert tableName=AUTHENTICATION_TYPE; insert tableName=AUTHENTICATION_TYPE; insert tableName=AUTHENTICATION_TYPE		\N	4.9.1	\N	\N	6869329870
2019-12-12-09-24-04-466	gavink	liquibase/6.0.0/init-auth-db.xml	2022-10-27 07:15:31.885038	12	EXECUTED	8:6af7c68dc43f47fc6c2c999f6a95d255	insert tableName=USERS; insert tableName=USERS; insert tableName=USERS		\N	4.9.1	\N	\N	6869329870
2019-12-12-09-11-55-999	gavink	liquibase/6.0.0/init-auth-db.xml	2022-10-27 07:15:31.953129	13	EXECUTED	8:3b8453158b0312ea33ac8b9c0cecc9a7	insert tableName=PERMISSION_MATRIX; insert tableName=PERMISSION_MATRIX; insert tableName=PERMISSION_MATRIX; insert tableName=PERMISSION_MATRIX; insert tableName=PERMISSION_MATRIX; insert tableName=PERMISSION_MATRIX; insert tableName=PERMISSION_MAT...		\N	4.9.1	\N	\N	6869329870
2019-12-12-09-34-27-326	gavink	liquibase/6.0.0/init-auth-db.xml	2022-10-27 07:15:31.969511	14	EXECUTED	8:b4ec76700f3c77df7a35f47f8cd068e6	insert tableName=USER_ROLES; insert tableName=USER_ROLES; insert tableName=USER_ROLES		\N	4.9.1	\N	\N	6869329870
2020-05-13-12-07-55-180	psantos	liquibase/6.0.0/environment-override-removal.xml	2022-10-27 07:15:31.990434	15	EXECUTED	8:c59fcc5c6db0dc0b854cfb9266358191	delete tableName=DEFINED_FIELDS		\N	4.9.1	\N	\N	6869329870
2020-05-04-14-09-12-649	psantos	liquibase/6.0.0/component-tasks.xml	2022-10-27 07:15:32.001479	16	EXECUTED	8:6d6ef4a7025ff70987b258e67476ea0f	insert tableName=REGISTERED_DESCRIPTORS		\N	4.9.1	\N	\N	6869329870
2020-05-04-14-41-58-554	psantos	liquibase/6.0.0/component-tasks.xml	2022-10-27 07:15:32.013849	17	EXECUTED	8:67ae4716c0ae54f069e06f38f8e508e1	insert tableName=PERMISSION_MATRIX; insert tableName=PERMISSION_MATRIX		\N	4.9.1	\N	\N	6869329870
2020-02-05-09-16-18-370	gavink	liquibase/6.0.0/settings-fields.xml	2022-10-27 07:15:32.025854	18	EXECUTED	8:972142744689a33bb4bc20b68b993927	delete tableName=defined_fields; delete tableName=defined_fields		\N	4.9.1	\N	\N	6869329870
2020-06-02-10-25-16-721	psantos	liquibase/6.0.0/settings-fields.xml	2022-10-27 07:15:32.036135	19	MARK_RAN	8:21060d5347838c459bce852d025098c5	update tableName=descriptor_configs		\N	4.9.1	\N	\N	6869329870
2020-06-02-10-52-08-950	psantos	liquibase/6.0.0/settings-fields.xml	2022-10-27 07:15:32.041024	20	MARK_RAN	8:d9174c8c4933c52b25791c5bf33b6482	update tableName=descriptor_configs		\N	4.9.1	\N	\N	6869329870
2020-02-11-08-58-36-804	gavink	liquibase/6.0.0/notification-migration-procedures.xml	2022-10-27 07:15:32.047823	21	EXECUTED	8:b658f63c63e59e8721bdacfff98ffb7f	createProcedure		\N	4.9.1	\N	\N	6869329870
2020-02-11-09-13-57-663	gavink	liquibase/6.0.0/notifications.xml	2022-10-27 07:15:32.0666	22	EXECUTED	8:16cb21647f8178401859cabb4e4ee622	addColumn tableName=raw_notification_content		\N	4.9.1	\N	\N	6869329870
2020-04-20-06-44-21-289	psantos	liquibase/6.0.0/notifications.xml	2022-10-27 07:15:32.074544	23	EXECUTED	8:b0c01fa8c81cbf5a03e66b86fd74f248	addNotNullConstraint columnName=provider_config_id, tableName=raw_notification_content		\N	4.9.1	\N	\N	6869329870
2020-02-11-09-21-42-238	gavink	liquibase/6.0.0/notifications.xml	2022-10-27 07:15:32.273802	24	EXECUTED	8:54ecbf17c68d0b097452a734423aef3f	addForeignKeyConstraint baseTableName=raw_notification_content, constraintName=FK_NOTIFICATION_PROVIDER_CONFIG_ID, referencedTableName=descriptor_configs		\N	4.9.1	\N	\N	6869329870
2020-02-12-15-43-59-483	gavink	liquibase/6.0.0/blackduck-procedures.xml	2022-10-27 07:15:32.282468	25	EXECUTED	8:0a75e3d46ba3e00c7b660368a2f6e679	createProcedure		\N	4.9.1	\N	\N	6869329870
2020-02-12-14-14-31-780	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.291421	26	EXECUTED	8:0ac61f555f341ba0f7bf2c785f858b79	insert tableName=defined_fields		\N	4.9.1	\N	\N	6869329870
2020-02-12-14-14-31-781	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.299826	27	EXECUTED	8:98896401d130623265283bb96f107157	insert tableName=defined_fields		\N	4.9.1	\N	\N	6869329870
2020-02-12-14-22-06-521	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.309007	28	EXECUTED	8:978a4a698701608d72094e78773f65f0	insert tableName=field_contexts		\N	4.9.1	\N	\N	6869329870
2020-02-12-14-22-06-522	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.320068	29	EXECUTED	8:b90abebb3acd7d5fc91826dc6e040188	insert tableName=field_contexts; insert tableName=field_contexts		\N	4.9.1	\N	\N	6869329870
2020-02-12-14-22-06-523	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.32996	30	EXECUTED	8:246b61199c7af14c8f7dbb1b2c082989	insert tableName=descriptor_fields		\N	4.9.1	\N	\N	6869329870
2020-02-12-14-22-06-524	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.340678	31	EXECUTED	8:ac7895544268e1b2e791793919859a79	insert tableName=descriptor_fields		\N	4.9.1	\N	\N	6869329870
2020-02-12-14-53-18-259	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.361975	32	MARK_RAN	8:223e3957d2e973d0fd6fe04552653272	insert tableName=field_values		\N	4.9.1	\N	\N	6869329870
2020-02-12-14-53-18-260	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.366663	33	MARK_RAN	8:fe54bce766d9a2ea331e515449d4d10b	insert tableName=field_values		\N	4.9.1	\N	\N	6869329870
2020-02-24-15-38-48-453	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.372488	34	EXECUTED	8:88acda4054a482508b9f012624a1d88a	sql		\N	4.9.1	\N	\N	6869329870
2020-02-14-11-34-06-730	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.385806	35	EXECUTED	8:9a622b79c92ddf806bc6aaf4ff7dc66c	addColumn tableName=provider_projects		\N	4.9.1	\N	\N	6869329870
2020-02-14-11-36-31-432	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.398933	36	EXECUTED	8:2b08727b3deba64dd2671a6c34f18cc4	addColumn tableName=provider_users		\N	4.9.1	\N	\N	6869329870
2020-02-14-11-34-06-731	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.409146	37	EXECUTED	8:748104d85203851778a113ad3628b772	dropColumn columnName=provider, tableName=provider_projects		\N	4.9.1	\N	\N	6869329870
2020-02-14-11-34-06-732	gavink	liquibase/6.0.0/provider-fields.xml	2022-10-27 07:15:32.419314	38	EXECUTED	8:b4282feb7212a432de9b7828109a23b9	dropColumn columnName=provider, tableName=provider_users		\N	4.9.1	\N	\N	6869329870
2020-02-20-14-35-09-961	gavink	liquibase/6.0.0/provider-task-properties.xml	2022-10-27 07:15:32.467793	39	EXECUTED	8:fd268e6ad0036da2736337da03a15254	createTable tableName=provider_task_properties		\N	4.9.1	\N	\N	6869329870
2020-02-20-14-55-54-948	gavink	liquibase/6.0.0/provider-task-properties.xml	2022-10-27 07:15:32.653118	40	EXECUTED	8:a5f1fed8845afd914df177f6407785cd	addForeignKeyConstraint baseTableName=provider_task_properties, constraintName=provider_task_properties_provider_config_id_fk, referencedTableName=descriptor_configs		\N	4.9.1	\N	\N	6869329870
2020-03-02-13-18-14-863	gavink	liquibase/6.0.0/refactor-field-names.xml	2022-10-27 07:15:32.669843	41	EXECUTED	8:eda4847799ab187ae95af65ac87df288	update tableName=defined_fields		\N	4.9.1	\N	\N	6869329870
2020-03-11-14-09-33-637	psantos	liquibase/6.0.0/certificates-timestamp.xml	2022-10-27 07:15:32.685256	42	EXECUTED	8:b95de20dae9a32e040db39bebebcd2ed	addColumn tableName=custom_certificates		\N	4.9.1	\N	\N	6869329870
2020-06-04-14-50-44-523	gavink	liquibase/6.1.0/descriptor-types-fk.xml	2022-10-27 07:15:32.696425	43	EXECUTED	8:7c849738400316837f1fc902caea162f	addForeignKeyConstraint baseTableName=registered_descriptors, constraintName=fk_registered_descriptors_type_id, referencedTableName=descriptor_types		\N	4.9.1	\N	\N	6869329870
2020-07-08-13-41-52-507	martinc	liquibase/6.2.0/azure-boards.xml	2022-10-27 07:15:32.708047	44	EXECUTED	8:1bffc0c2b5d68ecd789309d5282a801e	insert tableName=REGISTERED_DESCRIPTORS		\N	4.9.1	\N	\N	6869329870
2020-07-08-13-42-13-225	martinc	liquibase/6.2.0/azure-boards.xml	2022-10-27 07:15:32.736668	45	EXECUTED	8:deac38ed3813c2cea8872aa89930b47e	insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName...		\N	4.9.1	\N	\N	6869329870
2020-07-09-10-18-28-001	martinc	liquibase/6.2.0/azure-boards.xml	2022-10-27 07:15:32.77854	46	EXECUTED	8:5ab0d02b525e3fbf788329da0d7f44e2	insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIE...		\N	4.9.1	\N	\N	6869329870
2020-07-09-10-18-36-708	martinc	liquibase/6.2.0/azure-boards.xml	2022-10-27 07:15:32.812897	47	EXECUTED	8:37caf9a0fe8ead9b9b749841a1d89486	insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS; insert tableName...		\N	4.9.1	\N	\N	6869329870
2020-07-09-10-18-43-687	martinc	liquibase/6.2.0/azure-boards.xml	2022-10-27 07:15:32.828839	48	EXECUTED	8:a6e5b812e9e9c0fe21e5ab0173779b23	insert tableName=PERMISSION_MATRIX; insert tableName=PERMISSION_MATRIX; insert tableName=PERMISSION_MATRIX; insert tableName=PERMISSION_MATRIX; insert tableName=PERMISSION_MATRIX		\N	4.9.1	\N	\N	6869329870
2020-08-14-13-43-35-281	psantos	liquibase/6.2.0/azure-boards.xml	2022-10-27 07:15:32.836526	49	EXECUTED	8:98963c498e592f234110b4084bb927ae	modifyDataType columnName=FIELD_VALUE, tableName=FIELD_VALUES		\N	4.9.1	\N	\N	6869329870
create-field-to-store-provider-id	gavink	liquibase/6.3.0/provider-config-id-reference.xml	2022-10-27 07:15:32.846601	50	EXECUTED	8:88ad89951d0a965b60aa6488256085ae	insert tableName=defined_fields		\N	4.9.1	\N	\N	6869329870
associate-provider-id-field-with-provider	gavink	liquibase/6.3.0/provider-config-id-reference.xml	2022-10-27 07:15:32.859297	51	EXECUTED	8:7ec1fc9175a90871b78210b6f78b32c1	insert tableName=descriptor_fields		\N	4.9.1	\N	\N	6869329870
add-context-to-provider-id-field	gavink	liquibase/6.3.0/provider-config-id-reference.xml	2022-10-27 07:15:32.871262	52	EXECUTED	8:90b8cd2b92d9213aca7e4debd922c4f4	insert tableName=field_contexts		\N	4.9.1	\N	\N	6869329870
migrate-provider-config-name-to-id	gavink	liquibase/6.3.0/provider-config-id-reference.xml	2022-10-27 07:15:32.877612	53	EXECUTED	8:ac8032a47bcebea08fd9a56a3a9cc003	sql		\N	4.9.1	\N	\N	6869329870
delete-provider-name-field	gavink	liquibase/6.3.0/provider-config-id-reference.xml	2022-10-27 07:15:32.895637	54	EXECUTED	8:bd9f2388317750c8df7b87c833e0870b	delete tableName=field_contexts		\N	4.9.1	\N	\N	6869329870
create-disable-plugin-check-field-cloud	gavink	liquibase/6.3.0/jira-plugin-fields.xml	2022-10-27 07:15:32.90798	55	EXECUTED	8:698df285f0b0fc592fb576ec1d0c7d3b	insert tableName=defined_fields		\N	4.9.1	\N	\N	6869329870
associate-disable-plugin-check-field-with-channel-cloud	gavink	liquibase/6.3.0/jira-plugin-fields.xml	2022-10-27 07:15:32.923487	56	EXECUTED	8:dc9f9aa483811feaa6d21810597b931e	insert tableName=descriptor_fields		\N	4.9.1	\N	\N	6869329870
add-context-to-disable-plugin-check-field-cloud	gavink	liquibase/6.3.0/jira-plugin-fields.xml	2022-10-27 07:15:32.936847	57	EXECUTED	8:bec317f9171ef902198035aad2d06b4c	insert tableName=field_contexts		\N	4.9.1	\N	\N	6869329870
create-disable-plugin-check-field-server	gavink	liquibase/6.3.0/jira-plugin-fields.xml	2022-10-27 07:15:32.949938	58	EXECUTED	8:3372b40b248dc1528b8e9249fdd56fd0	insert tableName=defined_fields		\N	4.9.1	\N	\N	6869329870
associate-disable-plugin-check-field-with-channel-server	gavink	liquibase/6.3.0/jira-plugin-fields.xml	2022-10-27 07:15:32.968799	59	EXECUTED	8:a1d3e5f2f4e28b3ea8cd14f0b7fedf53	insert tableName=descriptor_fields		\N	4.9.1	\N	\N	6869329870
add-context-to-disable-plugin-check-field-server	gavink	liquibase/6.3.0/jira-plugin-fields.xml	2022-10-27 07:15:32.9789	60	EXECUTED	8:5a48a2a4b70cb4f92f077ee1e299e4ba	insert tableName=field_contexts		\N	4.9.1	\N	\N	6869329870
create-distribution-jobs-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:33.035668	61	EXECUTED	8:cf88acf16494d074742c8fdd0d38dd9c	createTable tableName=distribution_jobs		\N	4.9.1	\N	\N	6869329870
create-blackduck-job-details-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:33.081479	62	EXECUTED	8:fd50f77e8349256edb2ee396264c54ac	createTable tableName=blackduck_job_details		\N	4.9.1	\N	\N	6869329870
create-blackduck-job-details-table-fk	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:33.301449	63	EXECUTED	8:4284ef806558fa6da156dfde972d5893	addForeignKeyConstraint baseTableName=blackduck_job_details, constraintName=blackduck_job_details_job_id_fk, referencedTableName=distribution_jobs		\N	4.9.1	\N	\N	6869329870
create-blackduck-job-notification-types-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:33.356782	64	EXECUTED	8:3ad7c4023cdb830a6796520f9f8d81af	createTable tableName=blackduck_job_notification_types		\N	4.9.1	\N	\N	6869329870
create-blackduck-job-notification-types-table-fk	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:33.573483	65	EXECUTED	8:7ae0f8c68da4ee3c1c6df9bb071c0746	addForeignKeyConstraint baseTableName=blackduck_job_notification_types, constraintName=blackduck_job_notification_types_job_id_fk, referencedTableName=blackduck_job_details		\N	4.9.1	\N	\N	6869329870
create-blackduck-job-policy-filters-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:33.617551	66	EXECUTED	8:e4a73c99de2b4ca1051a4e601634fe53	createTable tableName=blackduck_job_policy_filters		\N	4.9.1	\N	\N	6869329870
create-blackduck-job-policy-filters-table-fk	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:33.867928	67	EXECUTED	8:80797a975cbfb71ac388c529d2781d65	addForeignKeyConstraint baseTableName=blackduck_job_policy_filters, constraintName=blackduck_job_policy_filters_job_id_fk, referencedTableName=blackduck_job_details		\N	4.9.1	\N	\N	6869329870
create-blackduck-job-vulnerability-severity-filters-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:33.937808	68	EXECUTED	8:07f4054b024d871236504b0d5678eccf	createTable tableName=blackduck_job_vulnerability_severity_filters		\N	4.9.1	\N	\N	6869329870
create-blackduck-job-vulnerability-severity-filters-table-fk	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:34.199941	69	EXECUTED	8:7564fc30b9141ab32379f0d19add0a74	addForeignKeyConstraint baseTableName=blackduck_job_vulnerability_severity_filters, constraintName=blackduck_job_vuln_severity_filters_job_id_fk, referencedTableName=blackduck_job_details		\N	4.9.1	\N	\N	6869329870
create-blackduck-job-projects-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:34.266578	70	EXECUTED	8:ef80b50bcd59062e8dedf67b04c02bfa	createTable tableName=blackduck_job_projects		\N	4.9.1	\N	\N	6869329870
create-blackduck-job-projects-table-fk	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:34.513248	71	EXECUTED	8:b550dc7b97be3eaa6e89e43c34dba8da	addForeignKeyConstraint baseTableName=blackduck_job_projects, constraintName=blackduck_job_projects_job_id_fk, referencedTableName=blackduck_job_details		\N	4.9.1	\N	\N	6869329870
create-azure-boards-job-details-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:34.591436	72	EXECUTED	8:1aa999316755f6b6f8261d55470dac00	createTable tableName=azure_boards_job_details		\N	4.9.1	\N	\N	6869329870
create-azure-boards-job-details-table-fk	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:34.846191	73	EXECUTED	8:a8d782b6eab2b15ecdc4c7e94c46d22b	addForeignKeyConstraint baseTableName=azure_boards_job_details, constraintName=azure_boards_job_details_job_id_fk, referencedTableName=distribution_jobs		\N	4.9.1	\N	\N	6869329870
create-email-job-details-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:34.914412	74	EXECUTED	8:422981c801240c7e7486e370a4278557	createTable tableName=email_job_details		\N	4.9.1	\N	\N	6869329870
create-email-job-details-table-fk	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:35.171149	75	EXECUTED	8:0f9158358e05d90de719d079423c4269	addForeignKeyConstraint baseTableName=email_job_details, constraintName=email_job_details_job_id_fk, referencedTableName=distribution_jobs		\N	4.9.1	\N	\N	6869329870
create-email-job-additional-email-addresses-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:35.235647	76	EXECUTED	8:50f6bcc2f9ae9ce07f4ef08a69b606fe	createTable tableName=email_job_additional_email_addresses		\N	4.9.1	\N	\N	6869329870
create-email-job-additional-email-addresses-table-fk	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:35.503046	77	EXECUTED	8:73a35b3e18e21d2f31f8fc24c0a7a36f	addForeignKeyConstraint baseTableName=email_job_additional_email_addresses, constraintName=email_job_additional_email_addresses_job_id_fk, referencedTableName=email_job_details		\N	4.9.1	\N	\N	6869329870
create-jira-cloud-job-details-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:35.562032	78	EXECUTED	8:930b0c8c150d35616b88af1c8943670d	createTable tableName=jira_cloud_job_details		\N	4.9.1	\N	\N	6869329870
create-jira-cloud-job-details-table-fk	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:35.830328	79	EXECUTED	8:497e86fd7b1fdd46e1cf465c697b647c	addForeignKeyConstraint baseTableName=jira_cloud_job_details, constraintName=jira_cloud_job_details_job_id_fk, referencedTableName=distribution_jobs		\N	4.9.1	\N	\N	6869329870
create-jira-server-job-details-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:35.901106	80	EXECUTED	8:d59bfdc599b358c7e770d44b24da3e65	createTable tableName=jira_server_job_details		\N	4.9.1	\N	\N	6869329870
create-jira-server-job-details-table-fk	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:36.182215	81	EXECUTED	8:bd5ba4097193d2701bd8e14be13cdb04	addForeignKeyConstraint baseTableName=jira_server_job_details, constraintName=jira_server_job_details_job_id_fk, referencedTableName=distribution_jobs		\N	4.9.1	\N	\N	6869329870
create-ms-teams-job-details-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:36.23368	82	EXECUTED	8:6d3d7a2565e84362901402376e6fa999	createTable tableName=ms_teams_job_details		\N	4.9.1	\N	\N	6869329870
create-ms-teams-job-details-table-fk	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:36.580238	83	EXECUTED	8:ba2b729c5d1bd0f42d8bd6dd8d61ceca	addForeignKeyConstraint baseTableName=ms_teams_job_details, constraintName=ms_teams_job_details_job_id_fk, referencedTableName=distribution_jobs		\N	4.9.1	\N	\N	6869329870
create-slack-job-details-table	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:36.634231	84	EXECUTED	8:3b724c58a396aaaa3ac42b74189cbb59	createTable tableName=slack_job_details		\N	4.9.1	\N	\N	6869329870
create-slack-job-details-table-fk	gavink	liquibase/6.4.0/static-distribution-jobs.xml	2022-10-27 07:15:36.932507	85	EXECUTED	8:fa4e1b2913085cd55f3a1ddf4468f5b2	addForeignKeyConstraint baseTableName=slack_job_details, constraintName=slack_job_details_job_id_fk, referencedTableName=distribution_jobs		\N	4.9.1	\N	\N	6869329870
create-initial-job-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:36.952939	86	EXECUTED	8:23e4c4d40bc20cab841260865c9c9ab5	sql		\N	4.9.1	\N	\N	6869329870
create-migration-helper-functions	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:36.967047	87	EXECUTED	8:31bf4066280bb8920b7357973b0af63c	createProcedure		\N	4.9.1	\N	\N	6869329870
update-initial-job-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:36.996359	88	EXECUTED	8:b7d8255cd48fc80076ae5956ac816981	sql; sql; sql; sql; sql		\N	4.9.1	\N	\N	6869329870
create-initial-blackduck-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.019054	89	EXECUTED	8:e810b5508ad37687929877c0a341cfe8	sql		\N	4.9.1	\N	\N	6869329870
update-blackduck-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.033739	90	EXECUTED	8:7ccbd01054c235852e2b41c06bca35fe	sql; sql; sql		\N	4.9.1	\N	\N	6869329870
join-blackduck-job-notification-types	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.054077	91	EXECUTED	8:4a5c7277ed988c5ec71cff956905d2d3	sql		\N	4.9.1	\N	\N	6869329870
join-blackduck-job-policy-filters	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.077815	92	EXECUTED	8:5a8ea457f924fdeb5d015732c6b9fc58	sql		\N	4.9.1	\N	\N	6869329870
join-blackduck-job-vulnerability-severity-filters	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.091072	93	EXECUTED	8:13af178193e8f493c6eda741e3eae6c3	sql		\N	4.9.1	\N	\N	6869329870
join-blackduck-job-project-filters	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.103237	94	EXECUTED	8:0ec03b7f2c65f501020a7cef390f65ec	sql		\N	4.9.1	\N	\N	6869329870
create-initial-azure-boards-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.114186	95	EXECUTED	8:c6a4ad30f4cbf5cad6dec0b2f4208cf0	sql		\N	4.9.1	\N	\N	6869329870
update-azure-boards-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.131211	96	EXECUTED	8:85c0dc1cd598f07b4b67aa7273cbc39c	sql; sql; sql; sql; sql		\N	4.9.1	\N	\N	6869329870
create-initial-email-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.143462	97	EXECUTED	8:691107c351ec50745a6b435169bb1a01	sql		\N	4.9.1	\N	\N	6869329870
update-email-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.169299	98	EXECUTED	8:280fe3b335a264197303b9b1faee2665	sql; sql; sql; sql		\N	4.9.1	\N	\N	6869329870
join-email-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.181813	99	EXECUTED	8:67e4761d1cd4bc0a68c6d52214f38939	sql		\N	4.9.1	\N	\N	6869329870
create-initial-jira-cloud-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.20044	100	EXECUTED	8:01da77208dbd03a004b24493ffef7248	sql		\N	4.9.1	\N	\N	6869329870
update-jira-cloud-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.220528	101	EXECUTED	8:21d55fe72552ba4bcedf38f8f8ffb494	sql; sql; sql; sql; sql; sql		\N	4.9.1	\N	\N	6869329870
create-initial-jira-server-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.231122	102	EXECUTED	8:9c23f96d462d5e83406cdcf1fedb438a	sql		\N	4.9.1	\N	\N	6869329870
update-jira-server-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.242942	103	EXECUTED	8:5d7df7702e76192086032fdc2304b09e	sql; sql; sql; sql; sql; sql		\N	4.9.1	\N	\N	6869329870
create-initial-ms-teams-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.252265	104	EXECUTED	8:bbed99a5447a482e6ad019bf13ba04dd	sql		\N	4.9.1	\N	\N	6869329870
update-ms-teams-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.258332	105	EXECUTED	8:5e3c3eec6122a436343deb9c79333380	sql		\N	4.9.1	\N	\N	6869329870
create-initial-slack-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.267605	106	EXECUTED	8:ebb0f038a7e4dca8ccc400144fdbf69c	sql		\N	4.9.1	\N	\N	6869329870
update-slack-job-detail-entries	gavink	liquibase/6.4.0/migrate-distribution-jobs.xml	2022-10-27 07:15:37.278053	107	EXECUTED	8:a2f8b431a9743d1a292cb110b3dd3d3f	sql; sql; sql		\N	4.9.1	\N	\N	6869329870
add-project-columns	gavink	liquibase/6.4.0/store-blackduck-project-href.xml	2022-10-27 07:15:37.287546	108	EXECUTED	8:f26de08afb5dd54ebfca7b47c53e3e6d	addColumn tableName=blackduck_job_projects		\N	4.9.1	\N	\N	6869329870
insert-provider-project-data-2	gavink	liquibase/6.4.0/store-blackduck-project-href.xml	2022-10-27 07:15:37.297187	109	EXECUTED	8:5a1dd152a09aec18c701aee28306e765	sql		\N	4.9.1	\N	\N	6869329870
delete-missing-projects-2	gavink	liquibase/6.4.0/store-blackduck-project-href.xml	2022-10-27 07:15:37.305474	110	EXECUTED	8:86d9da9e4ced9f298dae98fbe1abc352	delete tableName=blackduck_job_projects		\N	4.9.1	\N	\N	6869329870
drop-old-primary-key	gavink	liquibase/6.4.0/store-blackduck-project-href.xml	2022-10-27 07:15:37.342446	111	EXECUTED	8:ac62b21fcff2d62d2adfc1069abd2a3a	dropPrimaryKey tableName=blackduck_job_projects		\N	4.9.1	\N	\N	6869329870
add-new-primary-key	gavink	liquibase/6.4.0/store-blackduck-project-href.xml	2022-10-27 07:15:37.384588	112	EXECUTED	8:9e9c67b98e4d38925e5611beab0b195a	addPrimaryKey constraintName=blackduck_job_projects_pk, tableName=blackduck_job_projects		\N	4.9.1	\N	\N	6869329870
create-jira-cloud-custom-fields-table	gavink	liquibase/6.4.0/jira-custom-fields.xml	2022-10-27 07:15:37.437938	113	EXECUTED	8:2bfa7d8a7215bede18c1a653e095142f	createTable tableName=jira_cloud_job_custom_fields		\N	4.9.1	\N	\N	6869329870
create-jira-cloud-custom-fields-table-fk	gavink	liquibase/6.4.0/jira-custom-fields.xml	2022-10-27 07:15:37.743686	114	EXECUTED	8:254b5119a02310ddedb63e5c4caad335	addForeignKeyConstraint baseTableName=jira_cloud_job_custom_fields, constraintName=jira_cloud_job_custom_fields_job_id_fk, referencedTableName=jira_cloud_job_details		\N	4.9.1	\N	\N	6869329870
create-jira-server-custom-fields-table	gavink	liquibase/6.4.0/jira-custom-fields.xml	2022-10-27 07:15:37.798082	115	EXECUTED	8:2a90c13e7c4269fc8f096a1bd79dbcfa	createTable tableName=jira_server_job_custom_fields		\N	4.9.1	\N	\N	6869329870
create-jira-server-custom-fields-table-fk	gavink	liquibase/6.4.0/jira-custom-fields.xml	2022-10-27 07:15:38.114305	116	EXECUTED	8:86e890cfbed323e12c57a86174d431af	addForeignKeyConstraint baseTableName=jira_server_job_custom_fields, constraintName=jira_server_job_custom_fields_job_id_fk, referencedTableName=jira_server_job_details		\N	4.9.1	\N	\N	6869329870
add-defined-field-field-mapping	jrichard	liquibase/6.4.0/jira-custom-fields.xml	2022-10-27 07:15:38.13793	117	EXECUTED	8:8c60d3662fe55c9a91057ef58a1a6e4a	insert tableName=DEFINED_FIELDS; insert tableName=DEFINED_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=FIELD_CONTEXTS; insert tableName=FIELD_CONTEXTS		\N	4.9.1	\N	\N	6869329870
add_processed_column_to_notification_table	martinch	liquibase/6.4.0/notification-content-processing-column.xml	2022-10-27 07:15:38.155431	118	EXECUTED	8:dee97dc8643d2aef456b1d581cd5d108	addColumn tableName=raw_notification_content		\N	4.9.1	\N	\N	6869329870
remove-config-groups-table	gavink	liquibase/6.4.0/remove-config-groups-table.xml	2022-10-27 07:15:38.190561	119	EXECUTED	8:c738e4a18507e4b31eec40eb9160a8fc	dropTable tableName=config_groups		\N	4.9.1	\N	\N	6869329870
fix-jira-configure-plugin-field	gavink	liquibase/6.5.0/fix-jira-fields.xml	2022-10-27 07:15:38.20835	120	EXECUTED	8:6b31c2ec28cadea0edc17665afa685a1	insert tableName=descriptor_fields; delete tableName=descriptor_fields		\N	4.9.1	\N	\N	6869329870
add-saml-want-assertions-field	bmandel	liquibase/6.6.0/saml-field-update.xml	2022-10-27 07:15:38.229814	121	EXECUTED	8:41934bb63cb6a717e1c342623a9e2d41	insert tableName=DEFINED_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=FIELD_CONTEXTS		\N	4.9.1	\N	\N	6869329870
saml-want-assertions-field-default	bmandel	liquibase/6.6.0/saml-field-update.xml	2022-10-27 07:15:38.240418	122	EXECUTED	8:6d0303b126f3ab954fd7d06295026e8c	sql		\N	4.9.1	\N	\N	6869329870
add-jira-cloud-column	bmandel	liquibase/6.7.0/jira-config-update.xml	2022-10-27 07:15:38.253967	123	EXECUTED	8:05f98ea4a908bd862405b33f5b416c34	addColumn tableName=jira_cloud_job_details		\N	4.9.1	\N	\N	6869329870
add-jira-cloud-defined-field	bmandel	liquibase/6.7.0/jira-config-update.xml	2022-10-27 07:15:38.272398	124	EXECUTED	8:1849d140185f4bbcac1e788b299856d9	insert tableName=DEFINED_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=FIELD_CONTEXTS		\N	4.9.1	\N	\N	6869329870
add-jira-server-column	bmandel	liquibase/6.7.0/jira-config-update.xml	2022-10-27 07:15:38.285394	125	EXECUTED	8:5c1d6ad378f4e3ef6d8e0514a73b4977	addColumn tableName=jira_server_job_details		\N	4.9.1	\N	\N	6869329870
add-jira-server-defined-field	bmandel	liquibase/6.7.0/jira-config-update.xml	2022-10-27 07:15:38.301121	126	EXECUTED	8:ba37c82a81d48dfd4f0f269433acf0bd	insert tableName=DEFINED_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=FIELD_CONTEXTS		\N	4.9.1	\N	\N	6869329870
add-non-proxy-hosts-field	gavink	liquibase/6.7.0/non-proxy-hosts.xml	2022-10-27 07:15:38.320058	127	EXECUTED	8:c3358dcc779fb3f08ec438946c8bd4b0	insert tableName=defined_fields; insert tableName=descriptor_fields; insert tableName=field_contexts		\N	4.9.1	\N	\N	6869329870
remove-role-mapping-user-field	gavink	liquibase/6.7.0/remove-role-mapping-fields.xml	2022-10-27 07:15:38.332306	128	EXECUTED	8:64cce9366657e8c84daa272f75c40648	delete tableName=defined_fields		\N	4.9.1	\N	\N	6869329870
remove-role-mapping-job-manager-field	gavink	liquibase/6.7.0/remove-role-mapping-fields.xml	2022-10-27 07:15:38.34402	129	EXECUTED	8:0b10f85814fc34afc450532ff5af09f8	delete tableName=defined_fields		\N	4.9.1	\N	\N	6869329870
remove-role-mapping-admin-field	gavink	liquibase/6.7.0/remove-role-mapping-fields.xml	2022-10-27 07:15:38.36317	130	EXECUTED	8:cab2fe4192521088717e4105fd6df20a	delete tableName=defined_fields		\N	4.9.1	\N	\N	6869329870
update-primary-key-user-role-mapping	psantos	liquibase/6.9.0/primary-key-update.xml	2022-10-27 07:15:38.407464	131	EXECUTED	8:e9319f8395a64de71b0cfdb22394d0d0	addPrimaryKey constraintName=user_roles_pk, tableName=user_roles		\N	4.9.1	\N	\N	6869329870
delete-provider-user-project-relation-table	psantos	liquibase/6.9.0/delete-provider-tables.xml	2022-10-27 07:15:38.432363	132	EXECUTED	8:2445254062c9f263d15ecdabbd4b356f	dropTable tableName=provider_user_project_relation		\N	4.9.1	\N	\N	6869329870
delete-provider-projects-table	psantos	liquibase/6.9.0/delete-provider-tables.xml	2022-10-27 07:15:38.474767	133	EXECUTED	8:1bf417686cda58ddbf422a3cab38471a	dropTable tableName=provider_projects		\N	4.9.1	\N	\N	6869329870
delete-provider-users-table	psantos	liquibase/6.9.0/delete-provider-tables.xml	2022-10-27 07:15:38.511344	134	EXECUTED	8:810db3870c8b476a246cdd244c908763	dropTable tableName=provider_users		\N	4.9.1	\N	\N	6869329870
create-email-configuration-table	psantos	liquibase/6.9.0/static-configuration-tables.xml	2022-10-27 07:15:38.586314	135	EXECUTED	8:5a28227920081c4ddaa4532b9a8f2b4b	createTable tableName=configuration_email		\N	4.9.1	\N	\N	6869329870
create-email-properties-table	psantos	liquibase/6.9.0/static-configuration-tables.xml	2022-10-27 07:15:38.644974	136	EXECUTED	8:c2eccc74a32265fccee08a42fc194fec	createTable tableName=configuration_email_properties		\N	4.9.1	\N	\N	6869329870
create-email-configuration-properties-table-fk	psantos	liquibase/6.9.0/static-configuration-tables.xml	2022-10-27 07:15:38.932006	137	EXECUTED	8:21f69cfb61dbdd0c40cb6aa50882192a	addForeignKeyConstraint baseTableName=configuration_email_properties, constraintName=configuration_email_properties_configuration_id_fk, referencedTableName=configuration_email		\N	4.9.1	\N	\N	6869329870
create-configuration-migration-helper-functions	psantos	liquibase/6.9.0/migrate-helper-functions.xml	2022-10-27 07:15:38.94491	138	EXECUTED	8:c27fe3a85acf23779523bd032bb32c41	createProcedure; createProcedure		\N	4.9.1	\N	\N	6869329870
create-initial-email-config-entries	psantos	liquibase/6.9.0/migrate-email-global-config.xml	2022-10-27 07:15:38.953679	139	EXECUTED	8:91f3a4bb711e52f448f9456f0684f702	sql		\N	4.9.1	\N	\N	6869329870
update-initial-email-config-entries	psantos	liquibase/6.9.0/migrate-email-global-config.xml	2022-10-27 07:15:38.95926	140	MARK_RAN	8:e0a06608f9e379b32395ab0d3181b059	sql; sql; sql; sql; sql; sql; sql; sql		\N	4.9.1	\N	\N	6869329870
updated-initial-email-additional-properties	psantos	liquibase/6.9.0/migrate-email-global-config.xml	2022-10-27 07:15:38.965341	141	MARK_RAN	8:6116227ab1fa4a0ccf96b2ab9a029e2a	sql		\N	4.9.1	\N	\N	6869329870
create-proxy-configuration-table	martinch	liquibase/6.9.0/settings-proxy-static-config.xml	2022-10-27 07:15:39.02057	142	EXECUTED	8:93f57318f8444a7e04dc7c116955ac3d	createTable tableName=configuration_proxy		\N	4.9.1	\N	\N	6869329870
create-non-proxy-hosts-table	martinch	liquibase/6.9.0/settings-proxy-static-config.xml	2022-10-27 07:15:39.073887	143	EXECUTED	8:67e6b82faa430cd1f6466e859a7eb13b	createTable tableName=configuration_non_proxy_hosts		\N	4.9.1	\N	\N	6869329870
create-non-proxy-hosts-table-fk	martinch	liquibase/6.9.0/settings-proxy-static-config.xml	2022-10-27 07:15:39.375335	144	EXECUTED	8:2f7ab7a95c2654e4ce479503e97e00e5	addForeignKeyConstraint baseTableName=configuration_non_proxy_hosts, constraintName=configuration_non_proxy_hosts_id_fk, referencedTableName=configuration_proxy		\N	4.9.1	\N	\N	6869329870
create-initial-proxy-config-entries	martinch	liquibase/6.9.0/migrate-proxy-config.xml	2022-10-27 07:15:39.388444	145	EXECUTED	8:99d012287c694b5a02135d00193a6c49	sql		\N	4.9.1	\N	\N	6869329870
update-initial-proxy-config-entries	martinch	liquibase/6.9.0/migrate-proxy-config.xml	2022-10-27 07:15:39.39797	146	MARK_RAN	8:5f9cbbd6a0a4ea16d20051e49c8d7525	sql; sql; sql; sql; sql; sql		\N	4.9.1	\N	\N	6869329870
update-initial-proxy-non-proxy-hosts	martinch	liquibase/6.9.0/migrate-proxy-config.xml	2022-10-27 07:15:39.408576	147	MARK_RAN	8:6730bf7c391a2e1720ed0c9804645f26	sql		\N	4.9.1	\N	\N	6869329870
create-project-version-name-pattern-column	bmandel	liquibase/6.9.0/project-version-name-pattern.xml	2022-10-27 07:15:39.424879	148	EXECUTED	8:7e871f1da017817a6eb346063ebb1ab1	addColumn tableName=blackduck_job_details		\N	4.9.1	\N	\N	6869329870
add-defined-field-to-descriptor	bmandel	liquibase/6.9.0/project-version-name-pattern.xml	2022-10-27 07:15:39.443536	149	EXECUTED	8:0d315e5d9a7890953c8d3fa406ad4895	insert tableName=DEFINED_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=FIELD_CONTEXTS		\N	4.9.1	\N	\N	6869329870
create-jira-server-configuration-table	psantos	liquibase/6.10.0/static-jira-server-configuration-tables.xml	2022-10-27 07:15:39.518803	150	EXECUTED	8:257a185bd4e3df448db8df25d4e42638	createTable tableName=configuration_jira_server		\N	4.9.1	\N	\N	6869329870
create-global-channel-id-column	bmandel	liquibase/6.10.0/add-distribution-job-column.xml	2022-10-27 07:15:39.53089	151	EXECUTED	8:bf1ef4a9d20d2188bb4c41d435f56663	addColumn tableName=distribution_jobs		\N	4.9.1	\N	\N	6869329870
add-global-config-id-to-descriptors	bmandel	liquibase/6.10.0/add-distribution-job-column.xml	2022-10-27 07:15:39.560908	152	EXECUTED	8:e7d3e6abbed1573a3e9b004fbb6dc06f	insert tableName=DEFINED_FIELDS; insert tableName=FIELD_CONTEXTS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; insert tableName=DESCRIPTOR_FIELDS; i...		\N	4.9.1	\N	\N	6869329870
create-initial-jira-server-config-entries	psantos	liquibase/6.10.0/migrate-jira-server-global-config.xml	2022-10-27 07:15:39.579461	153	EXECUTED	8:0a90bc3abacf0e9692ef63097c3734a9	sql		\N	4.9.1	\N	\N	6869329870
update-initial-jira-server-config-entries	psantos	liquibase/6.10.0/migrate-jira-server-global-config.xml	2022-10-27 07:15:39.588011	154	MARK_RAN	8:39f04d5e792bd348b540763b67cb4db9	sql; sql; sql; sql; sql; sql		\N	4.9.1	\N	\N	6869329870
migrate-jira-jobs	psantos	liquibase/6.10.0/migrate-jira-server-global-config.xml	2022-10-27 07:15:39.59848	155	MARK_RAN	8:f2864ca0adbf8f710775cd71ba7723d6	sql		\N	4.9.1	\N	\N	6869329870
create-job-notification-mapping-relation-table	psantos	liquibase/6.11.0/job-notification-map.xml	2022-10-27 07:15:39.639012	156	EXECUTED	8:fe947778452550cd5102326bf0e9c67d	createTable tableName=job_notification_relation		\N	4.9.1	\N	\N	6869329870
create-job-notification-mapping-key	psantos	liquibase/6.11.0/job-notification-map.xml	2022-10-27 07:15:40.555775	157	EXECUTED	8:a7c377b0fc50fb35e89741c08189a1e4	addPrimaryKey constraintName=PK_JOB_MAPPING_RELATION, tableName=job_notification_relation		\N	4.9.1	\N	\N	6869329870
create-foreign-key-job-mapping-notification-id	psantos	liquibase/6.11.0/job-notification-map.xml	2022-10-27 07:15:40.873566	158	EXECUTED	8:f353ea73430fde46b4f2810d10a8a536	addForeignKeyConstraint baseTableName=job_notification_relation, constraintName=job_mapping_notification_id_fk, referencedTableName=raw_notification_content		\N	4.9.1	\N	\N	6869329870
create-foreign-key-job-mapping-job-id	psantos	liquibase/6.11.0/job-notification-map.xml	2022-10-27 07:15:41.208287	159	EXECUTED	8:e0f539df81d3ce5e877c16ed9eea3355	addForeignKeyConstraint baseTableName=job_notification_relation, constraintName=job_mapping_job_id_fk, referencedTableName=distribution_jobs		\N	4.9.1	\N	\N	6869329870
create-job-sub-task-status-table	psantos	liquibase/6.11.0/job-workflow-status.xml	2022-10-27 07:15:41.285486	160	EXECUTED	8:fc48703f0b149243299ed7906238b670	createTable tableName=job_sub_task_status		\N	4.9.1	\N	\N	6869329870
create-notification-correlation-to-notification-table	psantos	liquibase/6.11.0/job-workflow-status.xml	2022-10-27 07:15:41.306291	161	EXECUTED	8:467c841a81ca274334b750af35f45378	createTable tableName=notification_correlation_to_notification_relation		\N	4.9.1	\N	\N	6869329870
create-notification-correlation-to-notification-key	psantos	liquibase/6.11.0/job-workflow-status.xml	2022-10-27 07:15:42.118998	162	EXECUTED	8:16b2657ac412e48673b4cdae93b05772	addPrimaryKey constraintName=PK_NOTIFICATION_CORRELATION_TO_NOTIFICATION_RELATION, tableName=notification_correlation_to_notification_relation		\N	4.9.1	\N	\N	6869329870
create-foreign-key-job-sub-task-notification-correlation-id	psantos	liquibase/6.11.0/job-workflow-status.xml	2022-10-27 07:15:42.474289	163	EXECUTED	8:4f7bdd7d122fc952c96bfd65818c2a59	addForeignKeyConstraint baseTableName=notification_correlation_to_notification_relation, constraintName=job_sub_task_notification_correlation_id_fk, referencedTableName=job_sub_task_status		\N	4.9.1	\N	\N	6869329870
create-foreign-key-notification-id	psantos	liquibase/6.11.0/job-workflow-status.xml	2022-10-27 07:15:42.818208	164	EXECUTED	8:c8640cdb68f27753b6e10420b0424423	addForeignKeyConstraint baseTableName=notification_correlation_to_notification_relation, constraintName=job_sub_task_notification_id_fk, referencedTableName=raw_notification_content		\N	4.9.1	\N	\N	6869329870
create-azure-boards-configuration-table	martinch	liquibase/6.12.0/concrete-azure-boards-configuration-tables.xml	2022-10-27 07:15:42.886652	165	EXECUTED	8:ede41f3af47a1459034f4be10f16ebc9	createTable tableName=configuration_azure_boards		\N	4.9.1	\N	\N	6869329870
create-azure-boards-configuration-table	martinch	liquibase/6.12.0/oauth-credential-store.xml	2022-10-27 07:15:42.921364	166	EXECUTED	8:38055e2ba7d1a71f351b1f7aa1e161d4	createTable tableName=oauth_credentials		\N	4.9.1	\N	\N	6869329870
\.


--
-- Data for Name: databasechangeloglock; Type: TABLE DATA; Schema: public; Owner: sa
--

COPY public.databasechangeloglock (id, locked, lockgranted, lockedby) FROM stdin;
1	f	\N	\N
\.


--
-- Name: audit_entries_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.audit_entries_id_seq', 1, false);


--
-- Name: authentication_type_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.authentication_type_id_seq', 3, true);


--
-- Name: config_contexts_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.config_contexts_id_seq', 2, true);


--
-- Name: custom_certificates_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.custom_certificates_id_seq', 1, false);


--
-- Name: defined_fields_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.defined_fields_id_seq', 155, true);


--
-- Name: descriptor_configs_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.descriptor_configs_id_seq', 1, true);


--
-- Name: descriptor_types_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.descriptor_types_id_seq', 3, true);


--
-- Name: field_values_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.field_values_id_seq', 1, true);


--
-- Name: raw_notification_content_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.raw_notification_content_id_seq', 1, false);


--
-- Name: registered_descriptors_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.registered_descriptors_id_seq', 14, true);


--
-- Name: roles_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.roles_id_seq', 3, true);


--
-- Name: settings_key_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.settings_key_id_seq', 1, false);


--
-- Name: system_messages_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.system_messages_id_seq', 1, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: alert; Owner: sa
--

SELECT pg_catalog.setval('alert.users_id_seq', 3, true);


--
-- Name: audit_entries audit_entries_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.audit_entries
    ADD CONSTRAINT audit_entries_pkey PRIMARY KEY (id);


--
-- Name: audit_notification_relation audit_notification_relation_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.audit_notification_relation
    ADD CONSTRAINT audit_notification_relation_pkey PRIMARY KEY (audit_entry_id, notification_id);


--
-- Name: authentication_type auth_type_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.authentication_type
    ADD CONSTRAINT auth_type_key PRIMARY KEY (id);


--
-- Name: azure_boards_job_details azure_boards_job_details_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.azure_boards_job_details
    ADD CONSTRAINT azure_boards_job_details_pkey PRIMARY KEY (job_id);


--
-- Name: blackduck_job_details blackduck_job_details_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.blackduck_job_details
    ADD CONSTRAINT blackduck_job_details_pkey PRIMARY KEY (job_id);


--
-- Name: blackduck_job_notification_types blackduck_job_notification_types_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.blackduck_job_notification_types
    ADD CONSTRAINT blackduck_job_notification_types_pkey PRIMARY KEY (job_id, notification_type);


--
-- Name: blackduck_job_policy_filters blackduck_job_policy_filters_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.blackduck_job_policy_filters
    ADD CONSTRAINT blackduck_job_policy_filters_pkey PRIMARY KEY (job_id, policy_name);


--
-- Name: blackduck_job_projects blackduck_job_projects_pk; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.blackduck_job_projects
    ADD CONSTRAINT blackduck_job_projects_pk PRIMARY KEY (job_id, href);


--
-- Name: blackduck_job_vulnerability_severity_filters blackduck_job_vulnerability_severity_filters_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.blackduck_job_vulnerability_severity_filters
    ADD CONSTRAINT blackduck_job_vulnerability_severity_filters_pkey PRIMARY KEY (job_id, severity_name);


--
-- Name: config_contexts config_contexts_context_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.config_contexts
    ADD CONSTRAINT config_contexts_context_key UNIQUE (context);


--
-- Name: config_contexts config_contexts_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.config_contexts
    ADD CONSTRAINT config_contexts_key PRIMARY KEY (id);


--
-- Name: field_values config_values_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.field_values
    ADD CONSTRAINT config_values_key PRIMARY KEY (id);


--
-- Name: configuration_azure_boards configuration_azure_boards_name_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.configuration_azure_boards
    ADD CONSTRAINT configuration_azure_boards_name_key UNIQUE (name);


--
-- Name: configuration_azure_boards configuration_azure_boards_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.configuration_azure_boards
    ADD CONSTRAINT configuration_azure_boards_pkey PRIMARY KEY (configuration_id);


--
-- Name: configuration_email configuration_email_name_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.configuration_email
    ADD CONSTRAINT configuration_email_name_key UNIQUE (name);


--
-- Name: configuration_email configuration_email_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.configuration_email
    ADD CONSTRAINT configuration_email_pkey PRIMARY KEY (configuration_id);


--
-- Name: configuration_email_properties configuration_email_properties_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.configuration_email_properties
    ADD CONSTRAINT configuration_email_properties_pkey PRIMARY KEY (configuration_id, property_key);


--
-- Name: configuration_jira_server configuration_jira_server_name_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.configuration_jira_server
    ADD CONSTRAINT configuration_jira_server_name_key UNIQUE (name);


--
-- Name: configuration_jira_server configuration_jira_server_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.configuration_jira_server
    ADD CONSTRAINT configuration_jira_server_pkey PRIMARY KEY (configuration_id);


--
-- Name: configuration_non_proxy_hosts configuration_non_proxy_hosts_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.configuration_non_proxy_hosts
    ADD CONSTRAINT configuration_non_proxy_hosts_pkey PRIMARY KEY (configuration_id, hostname_pattern);


--
-- Name: configuration_proxy configuration_proxy_name_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.configuration_proxy
    ADD CONSTRAINT configuration_proxy_name_key UNIQUE (name);


--
-- Name: configuration_proxy configuration_proxy_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.configuration_proxy
    ADD CONSTRAINT configuration_proxy_pkey PRIMARY KEY (configuration_id);


--
-- Name: defined_fields defined_fields_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.defined_fields
    ADD CONSTRAINT defined_fields_key PRIMARY KEY (id);


--
-- Name: defined_fields defined_fields_source_key_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.defined_fields
    ADD CONSTRAINT defined_fields_source_key_key UNIQUE (source_key);


--
-- Name: descriptor_configs descriptor_configs_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.descriptor_configs
    ADD CONSTRAINT descriptor_configs_key PRIMARY KEY (id);


--
-- Name: descriptor_fields descriptor_fields_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.descriptor_fields
    ADD CONSTRAINT descriptor_fields_key PRIMARY KEY (descriptor_id, field_id);


--
-- Name: descriptor_types descriptor_types_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.descriptor_types
    ADD CONSTRAINT descriptor_types_key PRIMARY KEY (id);


--
-- Name: distribution_jobs distribution_jobs_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.distribution_jobs
    ADD CONSTRAINT distribution_jobs_pkey PRIMARY KEY (job_id);


--
-- Name: email_job_additional_email_addresses email_job_additional_email_addresses_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.email_job_additional_email_addresses
    ADD CONSTRAINT email_job_additional_email_addresses_pkey PRIMARY KEY (job_id, email_address);


--
-- Name: email_job_details email_job_details_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.email_job_details
    ADD CONSTRAINT email_job_details_pkey PRIMARY KEY (job_id);


--
-- Name: field_contexts field_contexts_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.field_contexts
    ADD CONSTRAINT field_contexts_key PRIMARY KEY (field_id, context_id);


--
-- Name: jira_cloud_job_custom_fields jira_cloud_job_custom_fields_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.jira_cloud_job_custom_fields
    ADD CONSTRAINT jira_cloud_job_custom_fields_pkey PRIMARY KEY (job_id, field_name);


--
-- Name: jira_cloud_job_details jira_cloud_job_details_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.jira_cloud_job_details
    ADD CONSTRAINT jira_cloud_job_details_pkey PRIMARY KEY (job_id);


--
-- Name: jira_server_job_custom_fields jira_server_job_custom_fields_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.jira_server_job_custom_fields
    ADD CONSTRAINT jira_server_job_custom_fields_pkey PRIMARY KEY (job_id, field_name);


--
-- Name: jira_server_job_details jira_server_job_details_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.jira_server_job_details
    ADD CONSTRAINT jira_server_job_details_pkey PRIMARY KEY (job_id);


--
-- Name: job_sub_task_status job_sub_task_status_notification_correlation_id_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.job_sub_task_status
    ADD CONSTRAINT job_sub_task_status_notification_correlation_id_key UNIQUE (notification_correlation_id);


--
-- Name: job_sub_task_status job_sub_task_status_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.job_sub_task_status
    ADD CONSTRAINT job_sub_task_status_pkey PRIMARY KEY (id);


--
-- Name: ms_teams_job_details ms_teams_job_details_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.ms_teams_job_details
    ADD CONSTRAINT ms_teams_job_details_pkey PRIMARY KEY (job_id);


--
-- Name: oauth_credentials oauth_credentials_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.oauth_credentials
    ADD CONSTRAINT oauth_credentials_pkey PRIMARY KEY (configuration_id);


--
-- Name: permission_matrix permission_matrix_key_updated; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.permission_matrix
    ADD CONSTRAINT permission_matrix_key_updated PRIMARY KEY (role_id, operations, descriptor_id, context_id);


--
-- Name: custom_certificates pk_custom_certificates; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.custom_certificates
    ADD CONSTRAINT pk_custom_certificates PRIMARY KEY (id);


--
-- Name: job_notification_relation pk_job_mapping_relation; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.job_notification_relation
    ADD CONSTRAINT pk_job_mapping_relation PRIMARY KEY (correlation_id, job_id, notification_id);


--
-- Name: notification_correlation_to_notification_relation pk_notification_correlation_to_notification_relation; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.notification_correlation_to_notification_relation
    ADD CONSTRAINT pk_notification_correlation_to_notification_relation PRIMARY KEY (notification_correlation_id, notification_id);


--
-- Name: provider_task_properties provider_task_properties_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.provider_task_properties
    ADD CONSTRAINT provider_task_properties_pkey PRIMARY KEY (task_name, property_name);


--
-- Name: raw_notification_content raw_notification_content_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.raw_notification_content
    ADD CONSTRAINT raw_notification_content_key PRIMARY KEY (id);


--
-- Name: registered_descriptors registered_descriptors_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.registered_descriptors
    ADD CONSTRAINT registered_descriptors_key PRIMARY KEY (id);


--
-- Name: roles role_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.roles
    ADD CONSTRAINT role_key PRIMARY KEY (id);


--
-- Name: settings_key settings_key_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.settings_key
    ADD CONSTRAINT settings_key_key PRIMARY KEY (id);


--
-- Name: settings_key settings_key_key_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.settings_key
    ADD CONSTRAINT settings_key_key_key UNIQUE (key);


--
-- Name: slack_job_details slack_job_details_pkey; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.slack_job_details
    ADD CONSTRAINT slack_job_details_pkey PRIMARY KEY (job_id);


--
-- Name: system_messages system_messages_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.system_messages
    ADD CONSTRAINT system_messages_key PRIMARY KEY (id);


--
-- Name: system_status system_status_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.system_status
    ADD CONSTRAINT system_status_key PRIMARY KEY (id);


--
-- Name: users user_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.users
    ADD CONSTRAINT user_key PRIMARY KEY (id);


--
-- Name: user_roles user_roles_pk; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.user_roles
    ADD CONSTRAINT user_roles_pk PRIMARY KEY (user_id, role_id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: databasechangeloglock databasechangeloglock_pkey; Type: CONSTRAINT; Schema: public; Owner: sa
--

ALTER TABLE ONLY public.databasechangeloglock
    ADD CONSTRAINT databasechangeloglock_pkey PRIMARY KEY (id);


--
-- Name: azure_boards_job_details azure_boards_job_details_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.azure_boards_job_details
    ADD CONSTRAINT azure_boards_job_details_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.distribution_jobs(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: blackduck_job_details blackduck_job_details_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.blackduck_job_details
    ADD CONSTRAINT blackduck_job_details_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.distribution_jobs(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: blackduck_job_notification_types blackduck_job_notification_types_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.blackduck_job_notification_types
    ADD CONSTRAINT blackduck_job_notification_types_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.blackduck_job_details(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: blackduck_job_policy_filters blackduck_job_policy_filters_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.blackduck_job_policy_filters
    ADD CONSTRAINT blackduck_job_policy_filters_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.blackduck_job_details(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: blackduck_job_projects blackduck_job_projects_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.blackduck_job_projects
    ADD CONSTRAINT blackduck_job_projects_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.blackduck_job_details(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: blackduck_job_vulnerability_severity_filters blackduck_job_vuln_severity_filters_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.blackduck_job_vulnerability_severity_filters
    ADD CONSTRAINT blackduck_job_vuln_severity_filters_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.blackduck_job_details(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: configuration_email_properties configuration_email_properties_configuration_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.configuration_email_properties
    ADD CONSTRAINT configuration_email_properties_configuration_id_fk FOREIGN KEY (configuration_id) REFERENCES alert.configuration_email(configuration_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: configuration_non_proxy_hosts configuration_non_proxy_hosts_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.configuration_non_proxy_hosts
    ADD CONSTRAINT configuration_non_proxy_hosts_id_fk FOREIGN KEY (configuration_id) REFERENCES alert.configuration_proxy(configuration_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: email_job_additional_email_addresses email_job_additional_email_addresses_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.email_job_additional_email_addresses
    ADD CONSTRAINT email_job_additional_email_addresses_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.email_job_details(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: email_job_details email_job_details_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.email_job_details
    ADD CONSTRAINT email_job_details_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.distribution_jobs(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: audit_notification_relation fk_audit_entry_id; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.audit_notification_relation
    ADD CONSTRAINT fk_audit_entry_id FOREIGN KEY (audit_entry_id) REFERENCES alert.audit_entries(id) ON DELETE CASCADE;


--
-- Name: audit_notification_relation fk_audit_notification_id; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.audit_notification_relation
    ADD CONSTRAINT fk_audit_notification_id FOREIGN KEY (notification_id) REFERENCES alert.raw_notification_content(id) ON DELETE CASCADE;


--
-- Name: users fk_auth_type_id; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.users
    ADD CONSTRAINT fk_auth_type_id FOREIGN KEY (auth_type) REFERENCES alert.authentication_type(id) ON DELETE CASCADE;


--
-- Name: descriptor_configs fk_config_context; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.descriptor_configs
    ADD CONSTRAINT fk_config_context FOREIGN KEY (context_id) REFERENCES alert.config_contexts(id) ON DELETE CASCADE;


--
-- Name: descriptor_configs fk_config_descriptor; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.descriptor_configs
    ADD CONSTRAINT fk_config_descriptor FOREIGN KEY (descriptor_id) REFERENCES alert.registered_descriptors(id) ON DELETE CASCADE;


--
-- Name: field_contexts fk_context_field; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.field_contexts
    ADD CONSTRAINT fk_context_field FOREIGN KEY (field_id) REFERENCES alert.defined_fields(id) ON DELETE CASCADE;


--
-- Name: field_values fk_defined_field_value; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.field_values
    ADD CONSTRAINT fk_defined_field_value FOREIGN KEY (field_id) REFERENCES alert.defined_fields(id) ON DELETE CASCADE;


--
-- Name: field_values fk_descriptor_config_value; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.field_values
    ADD CONSTRAINT fk_descriptor_config_value FOREIGN KEY (config_id) REFERENCES alert.descriptor_configs(id) ON DELETE CASCADE;


--
-- Name: descriptor_fields fk_descriptor_field; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.descriptor_fields
    ADD CONSTRAINT fk_descriptor_field FOREIGN KEY (field_id) REFERENCES alert.defined_fields(id) ON DELETE CASCADE;


--
-- Name: field_contexts fk_field_context; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.field_contexts
    ADD CONSTRAINT fk_field_context FOREIGN KEY (context_id) REFERENCES alert.config_contexts(id) ON DELETE CASCADE;


--
-- Name: descriptor_fields fk_field_descriptor; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.descriptor_fields
    ADD CONSTRAINT fk_field_descriptor FOREIGN KEY (descriptor_id) REFERENCES alert.registered_descriptors(id) ON DELETE CASCADE;


--
-- Name: raw_notification_content fk_notification_provider_config_id; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.raw_notification_content
    ADD CONSTRAINT fk_notification_provider_config_id FOREIGN KEY (provider_config_id) REFERENCES alert.descriptor_configs(id) ON DELETE CASCADE;


--
-- Name: permission_matrix fk_permission_context_id; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.permission_matrix
    ADD CONSTRAINT fk_permission_context_id FOREIGN KEY (context_id) REFERENCES alert.config_contexts(id) ON DELETE CASCADE;


--
-- Name: permission_matrix fk_permission_descriptor_id; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.permission_matrix
    ADD CONSTRAINT fk_permission_descriptor_id FOREIGN KEY (descriptor_id) REFERENCES alert.registered_descriptors(id) ON DELETE CASCADE;


--
-- Name: permission_matrix fk_permission_role; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.permission_matrix
    ADD CONSTRAINT fk_permission_role FOREIGN KEY (role_id) REFERENCES alert.roles(id) ON DELETE CASCADE;


--
-- Name: registered_descriptors fk_registered_descriptors_type_id; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.registered_descriptors
    ADD CONSTRAINT fk_registered_descriptors_type_id FOREIGN KEY (type_id) REFERENCES alert.descriptor_types(id);


--
-- Name: user_roles fk_role_id; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.user_roles
    ADD CONSTRAINT fk_role_id FOREIGN KEY (role_id) REFERENCES alert.roles(id) ON DELETE CASCADE;


--
-- Name: user_roles fk_user_id; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.user_roles
    ADD CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES alert.users(id) ON DELETE CASCADE;


--
-- Name: jira_cloud_job_custom_fields jira_cloud_job_custom_fields_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.jira_cloud_job_custom_fields
    ADD CONSTRAINT jira_cloud_job_custom_fields_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.jira_cloud_job_details(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: jira_cloud_job_details jira_cloud_job_details_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.jira_cloud_job_details
    ADD CONSTRAINT jira_cloud_job_details_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.distribution_jobs(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: jira_server_job_custom_fields jira_server_job_custom_fields_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.jira_server_job_custom_fields
    ADD CONSTRAINT jira_server_job_custom_fields_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.jira_server_job_details(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: jira_server_job_details jira_server_job_details_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.jira_server_job_details
    ADD CONSTRAINT jira_server_job_details_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.distribution_jobs(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: job_notification_relation job_mapping_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.job_notification_relation
    ADD CONSTRAINT job_mapping_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.distribution_jobs(job_id) ON DELETE CASCADE;


--
-- Name: job_notification_relation job_mapping_notification_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.job_notification_relation
    ADD CONSTRAINT job_mapping_notification_id_fk FOREIGN KEY (notification_id) REFERENCES alert.raw_notification_content(id) ON DELETE CASCADE;


--
-- Name: notification_correlation_to_notification_relation job_sub_task_notification_correlation_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.notification_correlation_to_notification_relation
    ADD CONSTRAINT job_sub_task_notification_correlation_id_fk FOREIGN KEY (notification_correlation_id) REFERENCES alert.job_sub_task_status(notification_correlation_id) ON DELETE CASCADE;


--
-- Name: notification_correlation_to_notification_relation job_sub_task_notification_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.notification_correlation_to_notification_relation
    ADD CONSTRAINT job_sub_task_notification_id_fk FOREIGN KEY (notification_id) REFERENCES alert.raw_notification_content(id) ON DELETE CASCADE;


--
-- Name: ms_teams_job_details ms_teams_job_details_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.ms_teams_job_details
    ADD CONSTRAINT ms_teams_job_details_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.distribution_jobs(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: provider_task_properties provider_task_properties_provider_config_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.provider_task_properties
    ADD CONSTRAINT provider_task_properties_provider_config_id_fk FOREIGN KEY (provider_config_id) REFERENCES alert.descriptor_configs(id) ON DELETE CASCADE;


--
-- Name: slack_job_details slack_job_details_job_id_fk; Type: FK CONSTRAINT; Schema: alert; Owner: sa
--

ALTER TABLE ONLY alert.slack_job_details
    ADD CONSTRAINT slack_job_details_job_id_fk FOREIGN KEY (job_id) REFERENCES alert.distribution_jobs(job_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

