/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIComponent;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.enumeration.FieldGroup;

@Component
public class EmailGlobalUIConfig extends UIConfig {

    @Override
    public UIComponent generateUIComponent() {
        return new UIComponent("Email", "email", EmailGroupChannel.COMPONENT_NAME, "envelope", setupFields());
    }

    public List<ConfigField> setupFields() {
        // Default fields
        final ConfigField mailSmtpHost = TextInputConfigField.createRequired(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), "Smtp Host");
        final ConfigField mailSmtpFrom = TextInputConfigField.createRequired(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), "Smtp From");
        final ConfigField mailSmtpAuth = CheckboxConfigField.create(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), "Smtp Auth");
        final ConfigField mailSmtpUser = TextInputConfigField.create(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), "Smtp User");
        final ConfigField mailSmtpPassword = PasswordConfigField.create(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), "Smtp Password");

        // Advanced fields
        final ConfigField mailSmtpPort = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "Smtp Port", FieldGroup.ADVANCED);
        final ConfigField mailSmtpConnectionTimeout = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "Smtp Connection Timeout", FieldGroup.ADVANCED);
        final ConfigField mailSmtpTimeout = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "Smtp Timeout", FieldGroup.ADVANCED);
        final ConfigField mailSmtpWriteTimeout = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey(), "Smtp Write Timeout", FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalhost = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY.getPropertyKey(), "Smtp Localhost", FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalAddress = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY.getPropertyKey(), "Smtp Local Address", FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalPort = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey(), "Smtp Local Port", FieldGroup.ADVANCED);
        final ConfigField mailSmtpEhlo = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), "Smtp Ehlo", FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthMechanisms = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY.getPropertyKey(), "Smtp Auth Mechanisms", FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthLoginDisable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY.getPropertyKey(), "Smtp Auth Login Disable", FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthPlainDisable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY.getPropertyKey(), "Smtp Auth Plain Disable", FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthDigestMd5Disable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY.getPropertyKey(), "Smtp Auth Digest MD5 Disable", FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmDisable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY.getPropertyKey(), "Smtp Auth Ntlm Disable", FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmDomain = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY.getPropertyKey(), "Smtp Auth Ntlm Domain", FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmFlags = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey(), "Smtp Auth Ntlm Flags", FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthXoauth2Disable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY.getPropertyKey(), "SMTP Auth XOAuth2 Disable", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSubmitter = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY.getPropertyKey(), "Smtp Submitter", FieldGroup.ADVANCED);
        final ConfigField mailSmtpDnsNotify = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY.getPropertyKey(), "Smtp DNS Notify", FieldGroup.ADVANCED);
        final ConfigField mailSmtpDnsRet = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY.getPropertyKey(), "Smtp DNS Ret", FieldGroup.ADVANCED);
        final ConfigField mailSmtpAllow8bitmime = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY.getPropertyKey(), "Smtp Allow 8-bit Mime", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSendPartial = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY.getPropertyKey(), "Smtp Send Partial", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslEnable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY.getPropertyKey(), "Smtp SASL Enable", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslMechanisms = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY.getPropertyKey(), "Smtp SASL Mechanisms", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslAuthorizationId = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY.getPropertyKey(), "Smtp SASL Authorization ID", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslRealm = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY.getPropertyKey(), "Smtp SASL Realm", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslUseCanonicalHostname = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY.getPropertyKey(), "Smtp SASL Use Canonical Hostname", FieldGroup.ADVANCED);
        final ConfigField mailSmtpQuitwait = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY.getPropertyKey(), "Smtp Quit Wait", FieldGroup.ADVANCED);
        final ConfigField mailSmtpReportSuccess = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY.getPropertyKey(), "Smtp Report Success", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslEnable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY.getPropertyKey(), "Smtp SSL Enable", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslCheckServerIdentity = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY.getPropertyKey(), "Smtp SSL Check Server Identity", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslTrust = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY.getPropertyKey(), "Smtp SSL Trust", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslProtocols = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY.getPropertyKey(), "Smtp SSL Protocols", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslCipherSuites = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY.getPropertyKey(), "Smtp SSL Cipher Suites", FieldGroup.ADVANCED);
        final ConfigField mailSmtpStartTlsEnable = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY.getPropertyKey(), "Smtp Start TLS Enabled", FieldGroup.ADVANCED);
        final ConfigField mailSmtpStartTlsRequired = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY.getPropertyKey(), "Smtp Start TLS Required", FieldGroup.ADVANCED);
        final ConfigField mailSmtpProxyHost = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY.getPropertyKey(), "Smtp Proxy Host", FieldGroup.ADVANCED);
        final ConfigField mailSmtpProxyPort = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey(), "Smtp Proxy Port", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSocksHost = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY.getPropertyKey(), "Smtp Socks Host", FieldGroup.ADVANCED);
        final ConfigField mailSmtpSocksPort = NumberConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey(), "Smtp Socks Port", FieldGroup.ADVANCED);
        final ConfigField mailSmtpMailExtension = TextInputConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY.getPropertyKey(), "Smtp Mail Extension", FieldGroup.ADVANCED);
        final ConfigField mailSmtpUserSet = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_USERSET_KEY.getPropertyKey(), "Smtp User Set", FieldGroup.ADVANCED);
        final ConfigField mailSmtpNoopStrict = CheckboxConfigField.createGrouped(EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY.getPropertyKey(), "Smtp NoOp Strict", FieldGroup.ADVANCED);

        final List fields = List.of(mailSmtpHost, mailSmtpFrom, mailSmtpAuth, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpWriteTimeout, mailSmtpLocalhost, mailSmtpLocalAddress,
            mailSmtpLocalPort, mailSmtpEhlo, mailSmtpAuthMechanisms, mailSmtpAuthLoginDisable, mailSmtpAuthPlainDisable, mailSmtpAuthDigestMd5Disable, mailSmtpAuthNtlmDisable, mailSmtpAuthNtlmDomain, mailSmtpAuthNtlmFlags,
            mailSmtpAuthXoauth2Disable, mailSmtpSubmitter, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, mailSmtpSaslEnable, mailSmtpSaslMechanisms, mailSmtpSaslAuthorizationId, mailSmtpSaslRealm,
            mailSmtpSaslUseCanonicalHostname, mailSmtpQuitwait, mailSmtpReportSuccess, mailSmtpSslEnable, mailSmtpSslCheckServerIdentity, mailSmtpSslTrust, mailSmtpSslProtocols, mailSmtpSslCipherSuites, mailSmtpStartTlsEnable,
            mailSmtpStartTlsRequired, mailSmtpProxyHost, mailSmtpProxyPort, mailSmtpSocksHost, mailSmtpSocksPort, mailSmtpMailExtension, mailSmtpUserSet, mailSmtpNoopStrict
        );

        return fields;
    }

}
