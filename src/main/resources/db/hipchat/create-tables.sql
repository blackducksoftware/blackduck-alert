CREATE TABLE hipchat_config (
    id INTEGER PRIMARY KEY,
    api_key VARCHAR,
    room_id INTEGER,
    notify BOOLEAN,
    color VARCHAR
);