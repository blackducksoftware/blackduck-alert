/*
 * service-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email;

import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_AUTH_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_EHLO_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_FROM_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_HOST_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_PORT_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_USERSET_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_USER_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.channel.email.web.EmailGlobalConfigModel;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;

// TODO create a factory for this class
public class EmailProperties {
    // property keys

    private final Properties javamailProperties = new Properties();
    private final String mailSmtpPassword;

    public EmailProperties(FieldUtility fieldUtility) {
        if (fieldUtility == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }

        addJavaMailOption(JAVAMAIL_USER_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_HOST_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_PORT_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_CONNECTION_TIMEOUT_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_TIMEOUT_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_WRITETIMEOUT_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_FROM_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_LOCALHOST_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_LOCALHOST_ADDRESS_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_LOCALHOST_PORT_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_EHLO_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_AUTH_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_AUTH_MECHANISMS_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_AUTH_LOGIN_DISABLE_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_AUTH_NTLM_DISABLE_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_AUTH_NTLM_DOMAIN_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_AUTH_NTLM_FLAGS_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SUBMITTER_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_DSN_NOTIFY_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_DSN_RET_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_ALLOW_8_BITMIME_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SEND_PARTIAL_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SASL_ENABLE_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SASL_MECHANISMS_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SASL_AUTHORIZATION_ID_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SASL_REALM_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_QUITWAIT_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_REPORT_SUCCESS_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SSL_ENABLE_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SSL_TRUST_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SSL_PROTOCOLS_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SSL_CIPHERSUITES_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_STARTTLS_ENABLE_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_STARTTLS_REQUIRED_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_PROXY_HOST_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_PROXY_PORT_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SOCKS_HOST_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_SOCKS_PORT_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_MAILEXTENSION_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_USERSET_KEY, fieldUtility);
        addJavaMailOption(JAVAMAIL_NOOP_STRICT_KEY, fieldUtility);

        mailSmtpPassword = fieldUtility.getStringOrNull(JAVAMAIL_PASSWORD_KEY.getPropertyKey());
    }

    public EmailProperties(ConfigurationModel emailGlobalConfigurationModel) {
        this(new FieldUtility(emailGlobalConfigurationModel.getCopyOfKeyToFieldMap()));
    }

    public EmailProperties(EmailGlobalConfigModel globalConfiguration) {
        if (globalConfiguration == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }

        addJavaMailOption(JAVAMAIL_USER_KEY, globalConfiguration.user);
        addJavaMailOption(JAVAMAIL_HOST_KEY, globalConfiguration.host);
        addJavaMailOption(JAVAMAIL_PORT_KEY, String.valueOf(globalConfiguration.port));
        addJavaMailOption(JAVAMAIL_CONNECTION_TIMEOUT_KEY, String.valueOf(globalConfiguration.connectionTimeout));
        addJavaMailOption(JAVAMAIL_TIMEOUT_KEY, String.valueOf(globalConfiguration.timeout));
        addJavaMailOption(JAVAMAIL_WRITETIMEOUT_KEY, String.valueOf(globalConfiguration.writeTimeout));
        addJavaMailOption(JAVAMAIL_FROM_KEY, globalConfiguration.from);
        addJavaMailOption(JAVAMAIL_LOCALHOST_KEY, globalConfiguration.localhost);
        addJavaMailOption(JAVAMAIL_LOCALHOST_ADDRESS_KEY, globalConfiguration.localaddress);
        addJavaMailOption(JAVAMAIL_LOCALHOST_PORT_KEY, String.valueOf(globalConfiguration.localport));
        addJavaMailOption(JAVAMAIL_EHLO_KEY, globalConfiguration.ehlo);
        addJavaMailOption(JAVAMAIL_AUTH_KEY, String.valueOf(globalConfiguration.auth));
        addJavaMailOption(JAVAMAIL_AUTH_MECHANISMS_KEY, globalConfiguration.authMechanisms);
        addJavaMailOption(JAVAMAIL_AUTH_LOGIN_DISABLE_KEY, String.valueOf(globalConfiguration.authLoginDisable));
        addJavaMailOption(JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY, String.valueOf(globalConfiguration.authLoginDisable));
        addJavaMailOption(JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY, String.valueOf(globalConfiguration.authDigestMd5Disable));
        addJavaMailOption(JAVAMAIL_AUTH_NTLM_DISABLE_KEY, String.valueOf(globalConfiguration.authNtlmDisable));
        addJavaMailOption(JAVAMAIL_AUTH_NTLM_DOMAIN_KEY, globalConfiguration.authNtlmDomain);
        addJavaMailOption(JAVAMAIL_AUTH_NTLM_FLAGS_KEY, String.valueOf(globalConfiguration.authNtlmFlags));
        addJavaMailOption(JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY, String.valueOf(globalConfiguration.authXoauth2Disable));
        addJavaMailOption(JAVAMAIL_SUBMITTER_KEY, globalConfiguration.submitter);
        addJavaMailOption(JAVAMAIL_DSN_NOTIFY_KEY, globalConfiguration.dsnNotify);
        addJavaMailOption(JAVAMAIL_DSN_RET_KEY, globalConfiguration.dsnRet);
        addJavaMailOption(JAVAMAIL_ALLOW_8_BITMIME_KEY, String.valueOf(globalConfiguration.allow8bitmime));
        addJavaMailOption(JAVAMAIL_SEND_PARTIAL_KEY, String.valueOf(globalConfiguration.sendPartial));
        addJavaMailOption(JAVAMAIL_SASL_ENABLE_KEY, String.valueOf(globalConfiguration.saslEnabled));
        addJavaMailOption(JAVAMAIL_SASL_MECHANISMS_KEY, globalConfiguration.saslMechanisms);
        addJavaMailOption(JAVAMAIL_SASL_AUTHORIZATION_ID_KEY, globalConfiguration.saslAuthorizationId);
        addJavaMailOption(JAVAMAIL_SASL_REALM_KEY, globalConfiguration.saslRealm);
        addJavaMailOption(JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY, String.valueOf(globalConfiguration.saslUseCanonicalHostname));
        addJavaMailOption(JAVAMAIL_QUITWAIT_KEY, String.valueOf(globalConfiguration.quitwait));
        addJavaMailOption(JAVAMAIL_REPORT_SUCCESS_KEY, String.valueOf(globalConfiguration.reportSuccess));
        addJavaMailOption(JAVAMAIL_SSL_ENABLE_KEY, String.valueOf(globalConfiguration.sslEnable));
        addJavaMailOption(JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY, String.valueOf(globalConfiguration.sslCheckServerIdentity));
        addJavaMailOption(JAVAMAIL_SSL_TRUST_KEY, String.valueOf(globalConfiguration.sslTrust));
        addJavaMailOption(JAVAMAIL_SSL_PROTOCOLS_KEY, String.valueOf(globalConfiguration.sslProtocols));
        addJavaMailOption(JAVAMAIL_SSL_CIPHERSUITES_KEY, String.valueOf(globalConfiguration.sslCipherSuites));
        addJavaMailOption(JAVAMAIL_STARTTLS_ENABLE_KEY, String.valueOf(globalConfiguration.startTlsEnable));
        addJavaMailOption(JAVAMAIL_STARTTLS_REQUIRED_KEY, String.valueOf(globalConfiguration.startTlsRequired));
        addJavaMailOption(JAVAMAIL_PROXY_HOST_KEY, String.valueOf(globalConfiguration.proxyHost));
        addJavaMailOption(JAVAMAIL_PROXY_PORT_KEY, String.valueOf(globalConfiguration.proxyPort));
        addJavaMailOption(JAVAMAIL_SOCKS_HOST_KEY, String.valueOf(globalConfiguration.socksHost));
        addJavaMailOption(JAVAMAIL_SOCKS_PORT_KEY, String.valueOf(globalConfiguration.socksPort));
        addJavaMailOption(JAVAMAIL_MAILEXTENSION_KEY, String.valueOf(globalConfiguration.mailExtension));
        addJavaMailOption(JAVAMAIL_USERSET_KEY, String.valueOf(globalConfiguration.userSet));
        addJavaMailOption(JAVAMAIL_NOOP_STRICT_KEY, String.valueOf(globalConfiguration.noopStrict));

        mailSmtpPassword = globalConfiguration.password;
    }

    public Properties getJavamailProperties() {
        return javamailProperties;
    }

    public String getJavamailOption(EmailPropertyKeys key) {
        return getJavamailOption(key.getPropertyKey());
    }

    public String getJavamailOption(String key) {
        return javamailProperties.getProperty(key);
    }

    public String getMailSmtpPassword() {
        return mailSmtpPassword;
    }

    private void addJavaMailOption(EmailPropertyKeys emailPropertyKeys, FieldUtility fieldUtility) {
        addJavaMailOption(emailPropertyKeys, fieldUtility.getStringOrNull(emailPropertyKeys.getPropertyKey()));
    }

    private void addJavaMailOption(EmailPropertyKeys emailPropertyKey, String value) {
        if (StringUtils.isNotEmpty(value)) {
            javamailProperties.setProperty(emailPropertyKey.getPropertyKey(), value);
        }
    }

}
