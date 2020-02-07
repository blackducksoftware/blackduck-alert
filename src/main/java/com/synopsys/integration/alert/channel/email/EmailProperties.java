/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;

public class EmailProperties {
    // property keys

    private final Map<String, String> javamailConfigProperties = new HashMap<>();
    private String mailSmtpPassword;
    private final FieldAccessor fieldAccessor;

    public EmailProperties(final FieldAccessor fieldAccessor) {
        if (fieldAccessor == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }
        this.fieldAccessor = fieldAccessor;
        updateFromConfig();
    }

    public void updateFromConfig() {
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_USER_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_HOST_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_PORT_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_FROM_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_EHLO_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_USERSET_KEY);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY);

        mailSmtpPassword = fieldAccessor.getString(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey()).orElse(null);
        addJavaMailOption(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY, mailSmtpPassword);
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

    private void addJavaMailOption(final EmailPropertyKeys emailPropertyKeys) {
        addJavaMailOption(emailPropertyKeys, fieldAccessor.getString(emailPropertyKeys.getPropertyKey()).orElse(null));
    }

    private void addJavaMailOption(final EmailPropertyKeys emailPropertyKey, final String value) {
        if (StringUtils.isNotEmpty(value)) {
            javamailConfigProperties.put(emailPropertyKey.getPropertyKey(), value);
        }
    }

}
