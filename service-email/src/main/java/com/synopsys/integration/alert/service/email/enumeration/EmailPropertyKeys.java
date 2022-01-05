/*
 * service-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email.enumeration;

public enum EmailPropertyKeys {
    EMAIL_CATEGORY("emailCategory"),
    EMAIL_CONTENT("content"),
    EMAIL_LOGO_IMAGE("logo.image"),
    // descriptor keys
    TEMPLATE_KEY_PROVIDER_URL("provider_url"),
    TEMPLATE_KEY_PROVIDER_NAME("provider_name"),
    TEMPLATE_KEY_PROVIDER_PROJECT_NAME("provider_project_name"),
    TEMPLATE_KEY_SUBJECT_LINE("subject_line"),
    TEMPLATE_KEY_TOPIC("topicsList"),
    TEMPLATE_KEY_START_DATE("startDate"),
    TEMPLATE_KEY_END_DATE("endDate"),
    TEMPLATE_KEY_EMAIL_CATEGORY("emailCategory"),

    // common javamail properties
    // keeping the same order as in the API documentation table located here:
    // https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html
    JAVAMAIL_USER_KEY("mail.smtp.user"),
    JAVAMAIL_HOST_KEY("mail.smtp.host"),
    JAVAMAIL_PORT_KEY("mail.smtp.port"),
    JAVAMAIL_CONNECTION_TIMEOUT_KEY("mail.smtp.connectiontimeout"),
    JAVAMAIL_TIMEOUT_KEY("mail.smtp.timeout"),
    JAVAMAIL_WRITETIMEOUT_KEY("mail.smtp.writetimeout"),
    JAVAMAIL_FROM_KEY("mail.smtp.from"),
    JAVAMAIL_LOCALHOST_KEY("mail.smtp.localhost"),
    JAVAMAIL_LOCALHOST_ADDRESS_KEY("mail.smtp.localaddress"),
    JAVAMAIL_LOCALHOST_PORT_KEY("mail.smtp.localport"),
    JAVAMAIL_EHLO_KEY("mail.smtp.ehlo"),
    JAVAMAIL_AUTH_KEY("mail.smtp.auth"),
    JAVAMAIL_AUTH_MECHANISMS_KEY("mail.smtp.auth.mechanisms"),
    JAVAMAIL_AUTH_LOGIN_DISABLE_KEY("mail.smtp.auth.login.disable"),
    JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY("mail.smtp.auth.plain.disable"),
    JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY("mail.smtp.auth.digest-md5.disable"),
    JAVAMAIL_AUTH_NTLM_DISABLE_KEY("mail.smtp.auth.ntlm.disable"),
    JAVAMAIL_AUTH_NTLM_DOMAIN_KEY("mail.smtp.auth.ntlm.domain"),
    JAVAMAIL_AUTH_NTLM_FLAGS_KEY("mail.smtp.auth.ntlm.flags"),
    JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY("mail.smtp.auth.xoauth2.disable"),
    JAVAMAIL_SUBMITTER_KEY("mail.smtp.submitter"),
    JAVAMAIL_DSN_NOTIFY_KEY("mail.smtp.dsn.notify"),
    JAVAMAIL_DSN_RET_KEY("mail.smtp.dsn.ret"),
    JAVAMAIL_ALLOW_8_BITMIME_KEY("mail.smtp.allow8bitmime"),
    JAVAMAIL_SEND_PARTIAL_KEY("mail.smtp.sendpartial"),
    JAVAMAIL_SASL_ENABLE_KEY("mail.smtp.sasl.enable"),
    JAVAMAIL_SASL_MECHANISMS_KEY("mail.smtp.sasl.mechanisms"),
    JAVAMAIL_SASL_AUTHORIZATION_ID_KEY("mail.smtp.sasl.authorizationid"),
    JAVAMAIL_SASL_REALM_KEY("mail.smtp.sasl.realm"),
    JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY("mail.smtp.sasl.usecanonicalhostname"),
    JAVAMAIL_QUITWAIT_KEY("mail.smtp.quitwait"),
    JAVAMAIL_REPORT_SUCCESS_KEY("mail.smtp.reportsuccess"),
    JAVAMAIL_SSL_ENABLE_KEY("mail.smtp.ssl.enable"),
    JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY("mail.smtp.ssl.checkserveridentity"),
    JAVAMAIL_SSL_TRUST_KEY("mail.smtp.ssl.trust"),
    JAVAMAIL_SSL_PROTOCOLS_KEY("mail.smtp.ssl.protocols"),
    JAVAMAIL_SSL_CIPHERSUITES_KEY("mail.smtp.ssl.ciphersuites"),
    JAVAMAIL_STARTTLS_ENABLE_KEY("mail.smtp.starttls.enable"),
    JAVAMAIL_STARTTLS_REQUIRED_KEY("mail.smtp.starttls.required"),
    JAVAMAIL_PROXY_HOST_KEY("mail.smtp.proxy.host"),
    JAVAMAIL_PROXY_PORT_KEY("mail.smtp.proxy.port"),
    JAVAMAIL_SOCKS_HOST_KEY("mail.smtp.socks.host"),
    JAVAMAIL_SOCKS_PORT_KEY("mail.smtp.socks.port"),
    JAVAMAIL_MAILEXTENSION_KEY("mail.smtp.mailextension"),
    JAVAMAIL_USERSET_KEY("mail.smtp.userset"),
    JAVAMAIL_NOOP_STRICT_KEY("mail.smtp.noop.strict"),

    // not a javamail property, but we are going to piggy-back to get teh smtp password
    JAVAMAIL_PASSWORD_KEY("mail.smtp.password");

    private final String propertyKey;

    EmailPropertyKeys(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }
}
