--DROP TABLE hipchat_config IF EXISTS;

CREATE TABLE slack_config (
    id INTEGER PRIMARY KEY,
    slack_channel_name VARCHAR,
    username INTEGER,
);