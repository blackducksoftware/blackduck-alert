--DROP TABLE notification_events IF EXISTS;

CREATE TABLE notification_events (
	id INTEGER PRIMARY KEY,
	event_key VARCHAR,
    created_at TIMESTAMP,
    notification_type VARCHAR,
    project_name VARCHAR,
    project_version VARCHAR,
    component_name VARCHAR,
    component_version VARCHAR,
    policy_rule_name VARCHAR,
    vulnerabilty_list VARCHAR,
    vulnerability_operation VARCHAR
);