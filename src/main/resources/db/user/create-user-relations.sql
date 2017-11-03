--DROP TABLE user_email IF EXISTS;
--DROP TABLE user_hipchat IF EXISTS;
--DROP TABLE user_slack IF EXISTS;
--DROP TABLE user_project_version IF EXISTS;
--DROP TABLE user_frequency IF EXISTS;

CREATE TABLE user_email (
    user_config_id INTEGER,
    email_config_id INTEGER
);

CREATE TABLE user_hipchat (
    user_config_id INTEGER,
    slack_config_id INTEGER
);

CREATE TABLE user_slack (
    user_config_id INTEGER,
    slack_config_id INTEGER
);

CREATE TABLE user_project_version (
    user_config_id INTEGER,
    project_name VARCHAR,
    project_version_name VARCHAR
);

CREATE TABLE user_frequency (
    user_config_id INTEGER,
    frequency_id INTEGER
);