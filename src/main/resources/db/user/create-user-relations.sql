--DROP TABLE email_user IF EXISTS;
--DROP TABLE hipchat_user IF EXISTS;
--DROP TABLE slack_user IF EXISTS;
--DROP TABLE project_version_user IF EXISTS;

CREATE TABLE email_user (
    user_config_id INTEGER,
    email_config_id INTEGER
);

CREATE TABLE hipchat_user (
    user_config_id INTEGER,
    slack_config_id INTEGER
);

CREATE TABLE slack_user (
    user_config_id INTEGER,
    slack_config_id INTEGER
);

CREATE TABLE project_version_user (
    user_config_id INTEGER,
    project_name VARCHAR,
    project_version_name VARCHAR
);