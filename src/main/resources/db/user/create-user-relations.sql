CREATE TABLE alert.hub_user_email (
    user_config_id INTEGER,
    email_config_id INTEGER
);

CREATE TABLE alert.hub_user_hipchat (
    user_config_id INTEGER,
    slack_config_id INTEGER
);

CREATE TABLE alert.hub_user_slack (
    user_config_id INTEGER,
    slack_config_id INTEGER
);

CREATE TABLE alert.hub_user_project_versions (
    user_config_id INTEGER,
    project_name VARCHAR,
    project_version_name VARCHAR
);

CREATE TABLE alert.hub_user_frequencies (
    user_config_id INTEGER,
    frequency VARCHAR
);