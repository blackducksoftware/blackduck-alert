/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.email;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;

public class EmailProperties {
    // property keys

    private final Map<String, String> javamailConfigProperties = new HashMap<>();
    private String mailSmtpPassword;

    public EmailProperties(final EmailGlobalConfigEntity emailConfigEntity) {
        if (emailConfigEntity == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }
        updateFromConfig(emailConfigEntity);
    }

    public void updateFromConfig(final EmailGlobalConfigEntity emailConfigEntity) {
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_USER_KEY, emailConfigEntity.getMailSmtpUser());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_HOST_KEY, emailConfigEntity.getMailSmtpHost());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_PORT_KEY, emailConfigEntity.getMailSmtpPort());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY, emailConfigEntity.getMailSmtpConnectionTimeout());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY, emailConfigEntity.getMailSmtpTimeout());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY, emailConfigEntity.getMailSmtpWriteTimeout());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_FROM_KEY, emailConfigEntity.getMailSmtpFrom());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY, emailConfigEntity.getMailSmtpLocalhost());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY, emailConfigEntity.getMailSmtpLocalAddress());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY, emailConfigEntity.getMailSmtpLocalPort());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_EHLO_KEY, emailConfigEntity.getMailSmtpEhlo());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_KEY, emailConfigEntity.getMailSmtpAuth());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY, emailConfigEntity.getMailSmtpAuthMechanisms());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY, emailConfigEntity.getMailSmtpAuthLoginDisable());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY, emailConfigEntity.getMailSmtpAuthPlainDisable());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY, emailConfigEntity.getMailSmtpAuthDigestMd5Disable());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY, emailConfigEntity.getMailSmtpAuthNtlmDisable());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY, emailConfigEntity.getMailSmtpAuthNtlmDomain());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY, emailConfigEntity.getMailSmtpAuthNtlmFlags());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY, emailConfigEntity.getMailSmtpAuthXoauth2Disable());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY, emailConfigEntity.getMailSmtpSubmitter());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY, emailConfigEntity.getMailSmtpDnsNotify());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY, emailConfigEntity.getMailSmtpDnsRet());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY, emailConfigEntity.getMailSmtpAllow8bitmime());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY, emailConfigEntity.getMailSmtpSendPartial());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY, emailConfigEntity.getMailSmtpSaslEnable());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY, emailConfigEntity.getMailSmtpSaslMechanisms());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY, emailConfigEntity.getMailSmtpSaslAuthorizationId());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY, emailConfigEntity.getMailSmtpSaslRealm());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY, emailConfigEntity.getMailSmtpSaslUseCanonicalHostname());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY, emailConfigEntity.getMailSmtpQuitwait());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY, emailConfigEntity.getMailSmtpReportSuccess());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY, emailConfigEntity.getMailSmtpSslEnable());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY, emailConfigEntity.getMailSmtpSslCheckServerIdentity());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY, emailConfigEntity.getMailSmtpSslTrust());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY, emailConfigEntity.getMailSmtpSslProtocols());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY, emailConfigEntity.getMailSmtpSslCipherSuites());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY, emailConfigEntity.getMailSmtpStartTlsEnable());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY, emailConfigEntity.getMailSmtpStartTlsRequired());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY, emailConfigEntity.getMailSmtpProxyHost());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY, emailConfigEntity.getMailSmtpProxyPort());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY, emailConfigEntity.getMailSmtpSocksHost());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY, emailConfigEntity.getMailSmtpSocksPort());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY, emailConfigEntity.getMailSmtpMailExtension());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_USERSET_KEY, emailConfigEntity.getMailSmtpUserSet());
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY, emailConfigEntity.getMailSmtpNoopStrict());

        this.mailSmtpPassword = emailConfigEntity.getMailSmtpPassword();
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY, this.mailSmtpPassword);
    }

    private void addJavaMailOption(final EmailPropertyKeys emailPropertyKey, final String value) {
        if (StringUtils.isNotEmpty(value)) {
            javamailConfigProperties.put(emailPropertyKey.getPropertyKey(), value);
        }
    }

    private void addJavaMailOption(final EmailPropertyKeys emailPropertyKey, final Boolean value) {
        if (value != null) {
            javamailConfigProperties.put(emailPropertyKey.getPropertyKey(), String.valueOf(value));
        }
    }

    private void addJavaMailOption(final EmailPropertyKeys emailPropertyKey, final Integer value) {
        if (value != null) {
            javamailConfigProperties.put(emailPropertyKey.getPropertyKey(), String.valueOf(value));
        }
    }

    public Map<String, String> getJavamailConfigProperties() {
        return javamailConfigProperties;
    }

    public String getJavamailOption(final EmailPropertyKeys key) {
        return getJavamailOption(key.getPropertyKey());
    }

    public String getJavamailOption(final String key) {
        return javamailConfigProperties.get(key);
    }

    public String getMailSmtpPassword() {
        return mailSmtpPassword;
    }

}
