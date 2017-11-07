CREATE TABLE alert.notification_events (
	id INTEGER PRIMARY KEY,
	event_key VARCHAR,
    created_at TIMESTAMP,
    notification_type VARCHAR,
    project_name VARCHAR,
    project_version VARCHAR,
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
	