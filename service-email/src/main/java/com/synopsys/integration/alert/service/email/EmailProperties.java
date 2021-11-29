/*
 * service-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;

// TODO create a factory for this class
public class EmailProperties {
    // property keys

    private final Map<String, String> javamailConfigProperties = new HashMap<>();
    private final String mailSmtpPassword;

    public EmailProperties(FieldUtility fieldUtility) {
        if (fieldUtility == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }

        updateFromConfig(fieldUtility);
        mailSmtpPassword = fieldUtility.getStringOrNull(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY, mailSmtpPassword);
    }

    public EmailProperties(ConfigurationModel emailGlobalConfigurationModel) {
        this(new FieldUtility(emailGlobalConfigurationModel.getCopyOfKeyToFieldMap()));
    }

    public void updateFromConfig(FieldUtility fieldUtility) {
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_USER_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_HOST_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_PORT_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_FROM_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_EHLO_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_AUTH_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_USERSET_KEY);
        addJavaMailOption(fieldUtility, EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY);
    }

    public Map<String, String> getJavamailConfigProperties() {
        return javamailConfigProperties;
    }

    public String getJavamailOption(EmailPropertyKeys key) {
        return getJavamailOption(key.getPropertyKey());
    }

    public String getJavamailOption(String key) {
        return javamailConfigProperties.get(key);
    }

    public String getMailSmtpPassword() {
        return mailSmtpPassword;
    }

    private void addJavaMailOption(FieldUtility fieldUtility, EmailPropertyKeys emailPropertyKeys) {
        addJavaMailOption(emailPropertyKeys, fieldUtility.getStringOrNull(emailPropertyKeys.getPropertyKey()));
    }

    private void addJavaMailOption(EmailPropertyKeys emailPropertyKey, String value) {
        if (StringUtils.isNotEmpty(value)) {
            javamailConfigProperties.put(emailPropertyKey.getPropertyKey(), value);
        }
    }

}
