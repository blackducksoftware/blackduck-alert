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
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class JavamailPropertiesFactory {
    public Properties createJavaMailProperties(FieldUtility fieldUtility) {
        if (fieldUtility == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }

        Properties javaMailProperties = new Properties();
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_USER_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_HOST_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_PORT_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_CONNECTION_TIMEOUT_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_TIMEOUT_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_WRITETIMEOUT_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_FROM_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_LOCALHOST_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_LOCALHOST_ADDRESS_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_LOCALHOST_PORT_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_EHLO_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_MECHANISMS_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_LOGIN_DISABLE_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_NTLM_DISABLE_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_NTLM_DOMAIN_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_NTLM_FLAGS_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SUBMITTER_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_DSN_NOTIFY_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_DSN_RET_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_ALLOW_8_BITMIME_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SEND_PARTIAL_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SASL_ENABLE_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SASL_MECHANISMS_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SASL_AUTHORIZATION_ID_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SASL_REALM_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_QUITWAIT_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_REPORT_SUCCESS_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SSL_ENABLE_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SSL_TRUST_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SSL_PROTOCOLS_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SSL_CIPHERSUITES_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_STARTTLS_ENABLE_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_STARTTLS_REQUIRED_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_PROXY_HOST_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_PROXY_PORT_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SOCKS_HOST_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SOCKS_PORT_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_MAILEXTENSION_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_USERSET_KEY, fieldUtility);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_NOOP_STRICT_KEY, fieldUtility);

        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_PASSWORD_KEY, fieldUtility);

        return javaMailProperties;
    }

    public Properties createJavaMailProperties(ConfigurationModel emailGlobalConfigurationModel) {
        return createJavaMailProperties(new FieldUtility(emailGlobalConfigurationModel.getCopyOfKeyToFieldMap()));
    }

    public Properties createJavaMailProperties(EmailGlobalConfigModel globalConfiguration) {
        if (globalConfiguration == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }

        Properties javaMailProperties = new Properties();
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_USER_KEY, globalConfiguration.user);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_HOST_KEY, globalConfiguration.host);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_PORT_KEY, String.valueOf(globalConfiguration.port));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_CONNECTION_TIMEOUT_KEY, String.valueOf(globalConfiguration.connectionTimeout));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_TIMEOUT_KEY, String.valueOf(globalConfiguration.timeout));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_WRITETIMEOUT_KEY, String.valueOf(globalConfiguration.writeTimeout));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_FROM_KEY, globalConfiguration.from);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_LOCALHOST_KEY, globalConfiguration.localhost);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_LOCALHOST_ADDRESS_KEY, globalConfiguration.localaddress);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_LOCALHOST_PORT_KEY, String.valueOf(globalConfiguration.localport));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_EHLO_KEY, globalConfiguration.ehlo);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_KEY, String.valueOf(globalConfiguration.auth));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_MECHANISMS_KEY, globalConfiguration.authMechanisms);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_LOGIN_DISABLE_KEY, String.valueOf(globalConfiguration.authLoginDisable));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY, String.valueOf(globalConfiguration.authLoginDisable));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY, String.valueOf(globalConfiguration.authDigestMd5Disable));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_NTLM_DISABLE_KEY, String.valueOf(globalConfiguration.authNtlmDisable));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_NTLM_DOMAIN_KEY, globalConfiguration.authNtlmDomain);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_NTLM_FLAGS_KEY, String.valueOf(globalConfiguration.authNtlmFlags));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY, String.valueOf(globalConfiguration.authXoauth2Disable));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SUBMITTER_KEY, globalConfiguration.submitter);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_DSN_NOTIFY_KEY, globalConfiguration.dsnNotify);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_DSN_RET_KEY, globalConfiguration.dsnRet);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_ALLOW_8_BITMIME_KEY, String.valueOf(globalConfiguration.allow8bitmime));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SEND_PARTIAL_KEY, String.valueOf(globalConfiguration.sendPartial));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SASL_ENABLE_KEY, String.valueOf(globalConfiguration.saslEnabled));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SASL_MECHANISMS_KEY, globalConfiguration.saslMechanisms);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SASL_AUTHORIZATION_ID_KEY, globalConfiguration.saslAuthorizationId);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SASL_REALM_KEY, globalConfiguration.saslRealm);
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY, String.valueOf(globalConfiguration.saslUseCanonicalHostname));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_QUITWAIT_KEY, String.valueOf(globalConfiguration.quitwait));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_REPORT_SUCCESS_KEY, String.valueOf(globalConfiguration.reportSuccess));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SSL_ENABLE_KEY, String.valueOf(globalConfiguration.sslEnable));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY, String.valueOf(globalConfiguration.sslCheckServerIdentity));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SSL_TRUST_KEY, String.valueOf(globalConfiguration.sslTrust));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SSL_PROTOCOLS_KEY, String.valueOf(globalConfiguration.sslProtocols));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SSL_CIPHERSUITES_KEY, String.valueOf(globalConfiguration.sslCipherSuites));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_STARTTLS_ENABLE_KEY, String.valueOf(globalConfiguration.startTlsEnable));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_STARTTLS_REQUIRED_KEY, String.valueOf(globalConfiguration.startTlsRequired));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_PROXY_HOST_KEY, String.valueOf(globalConfiguration.proxyHost));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_PROXY_PORT_KEY, String.valueOf(globalConfiguration.proxyPort));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SOCKS_HOST_KEY, String.valueOf(globalConfiguration.socksHost));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_SOCKS_PORT_KEY, String.valueOf(globalConfiguration.socksPort));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_MAILEXTENSION_KEY, String.valueOf(globalConfiguration.mailExtension));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_USERSET_KEY, String.valueOf(globalConfiguration.userSet));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_NOOP_STRICT_KEY, String.valueOf(globalConfiguration.noopStrict));

        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_PASSWORD_KEY, globalConfiguration.password);

        return javaMailProperties;
    }

    private void putIfNotEmpty(BiConsumer<String, String> setter, EmailPropertyKeys emailPropertyKey, FieldUtility fieldUtility) {
        String keyString = emailPropertyKey.getPropertyKey();
        putIfNotEmpty(setter, keyString, fieldUtility.getStringOrNull(keyString));
    }

    private void putIfNotEmpty(BiConsumer<String, String> setter, EmailPropertyKeys emailPropertyKey, String value) {
        String keyString = emailPropertyKey.getPropertyKey();
        putIfNotEmpty(setter, keyString, value);
    }

    private void putIfNotEmpty(BiConsumer<String, String> setter, String key, String value) {
        if (StringUtils.isNotEmpty(value)) {
            setter.accept(key, value);
        }
    }

}
