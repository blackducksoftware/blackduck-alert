--DROP TABLE user_config IF EXISTS;

CREATE TABLE user_config (
    id INTEGER PRIMARY KEY,
    hub_usernames VARCHAR,
    email_config_id INTEGER,
    hipchat_config_id INTEGER,
    slack_config_id INTEGER
);