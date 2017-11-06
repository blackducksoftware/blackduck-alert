--DROP TABLE hub_user_email IF EXISTS;
--DROP TABLE hub_user_hipchat IF EXISTS;
--DROP TABLE hub_user_slack IF EXISTS;
--DROP TABLE hub_user_project_versions IF EXISTS;
--DROP TABLE hub_user_frequencies IF EXISTS;

CREATE TABLE hub_user_email (
    user_config_id INTEGER,
    email_config_id INTEGER
);

CREATE TABLE hub_user_hipchat (
    user_config_id INTEGER,
    slack_config_id INTEGER
);

CREATE TABLE hub_user_slack (
    user_config_id INTEGER,
    slack_config_id INTEGER
);

CREATE TABLE hub_user_project_versions (
    user_config_id INTEGER,
    project_name VARCHAR,
    project_version_name VARCHAR
);

CREATE TABLE hub_user_frequencies (
    user_config_id INTEGER,
    frequency_id INTEGER
);