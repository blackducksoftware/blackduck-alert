--DROP TABLE email_config IF EXISTS;

CREATE TABLE email_config (
    id INTEGER PRIMARY KEY,
    mail_smtp_host VARCHAR,
    mail_smtp_user VARCHAR,
    mail_smtp_password VARCHAR,
    mail_smtp_port INTEGER,
    mail_smtp_connection_timeout INTEGER,
    mail_smtp_timeout INTEGER,
    mail_smtp_from VARCHAR,
    mail_smtp_localhost VARCHAR,
    mail_smtp_ehlo BOOLEAN,
    mail_smtp_auth BOOLEAN,
    mail_smtp_dsn_notify VARCHAR,
    mail_smtp_dsn_ret VARCHAR,
    mail_smtp_allow_8_bitmime BOOLEAN,
    mail_smtp_send_partial BOOLEAN,
    email_template_directory VARCHAR,
    email_template_logo_image VARCHAR
); 