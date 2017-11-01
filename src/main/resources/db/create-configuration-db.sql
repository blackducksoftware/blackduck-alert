--DROP TABLE notification_events IF EXISTS;

CREATE TABLE global_config (
	id INTEGER PRIMARY KEY,
	hub_url VARCHAR,
    hub_timeout INTEGER,
    hub_username VARCHAR,
    hub_password VARCHAR,
    hub_proxy_host VARCHAR,
    hub_proxy_port INTEGER,
    hub_proxy_username VARCHAR,
    hub_proxy_password VARCHAR,
    hub_always_trust_cert BOOLEAN,
    alert_accumulator_cron VARCHAR,
    alert_digest_daily_cron VARCHAR
);