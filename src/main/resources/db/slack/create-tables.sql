--DROP TABLE hipchat_config IF EXISTS;

CREATE TABLE slack_config (
    id INTEGER PRIMARY KEY,
    channel_name VARCHAR,
    username INTEGER,
    webhook VARCHAR
);