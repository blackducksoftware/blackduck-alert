--DROP TABLE hipchat_config IF EXISTS;

CREATE TABLE email_config (
    id INTEGER PRIMARY KEY,
    api_key VARCHAR,
    room_id INTEGER,
    notify BOOLEAN,
    color VARCHAR
); 