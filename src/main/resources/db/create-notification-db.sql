CREATE TABLE alert.notification_events (
	id INTEGER PRIMARY KEY,
	hub_user VARCHAR,
	event_key VARCHAR,
    created_at TIMESTAMP,
    notification_type VARCHAR,
    project_name VARCHAR,
    project_url VARCHAR,
    project_version VARCHAR,
    project_version_url VARCHAR,
    component_name VARCHAR,
    component_version VARCHAR,
    person VARCHAR,
    policy_rule_name VARCHAR
);

CREATE TABLE alert.vulnerabilities (
	id INTEGER PRIMARY KEY,
	vulnerability_id VARCHAR,
	vulnerability_operation VARCHAR
);
	