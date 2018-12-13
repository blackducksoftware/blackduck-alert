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
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.ArrayList;
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
        final List<ConfigField> fields = new ArrayList<>();

        // Default fields
        final ConfigField mailSmtpHost = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), "Smtp Host", true, false);
        final ConfigField mailSmtpFrom = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), "Smtp From", true, false);
        final ConfigField mailSmtpAuth = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), "Smtp Auth", false, false);
        final ConfigField mailSmtpUser = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), "Smtp User", false, false);
        final ConfigField mailSmtpPassword = new PasswordConfigField(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), "Smtp Password", false);
        fields.add(mailSmtpHost);
        fields.add(mailSmtpFrom);
        fields.add(mailSmtpAuth);
        fields.add(mailSmtpUser);
        fields.add(mailSmtpPassword);

        // Advanced fields
        final ConfigField mailSmtpPort = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "Smtp Port", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpConnectionTimeout = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "Smtp Connection Timeout", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpTimeout = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "Smtp Timeout", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpWriteTimeout = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey(), "Smtp Write Timeout", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalhost = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY.getPropertyKey(), "Smtp Localhost", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalAddress = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY.getPropertyKey(), "Smtp Local Address", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalPort = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey(), "Smtp Local Port", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpEhlo = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), "Smtp Ehlo", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthMechanisms = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY.getPropertyKey(), "Smtp Auth Mechanisms", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthLoginDisable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY.getPropertyKey(), "Smtp Auth Login Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthPlainDisable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY.getPropertyKey(), "Smtp Auth Plain Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthDigestMd5Disable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY.getPropertyKey(), "Smtp Auth Digest MD5 Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmDisable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY.getPropertyKey(), "Smtp Auth Ntlm Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmDomain = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY.getPropertyKey(), "Smtp Auth Ntlm Domain", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmFlags = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey(), "Smtp Auth Ntlm Flags", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthXoauth2Disable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY.getPropertyKey(), "SMTP Auth XOAuth2 Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSubmitter = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY.getPropertyKey(), "Smtp Submitter", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpDnsNotify = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY.getPropertyKey(), "Smtp DNS Notify", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpDnsRet = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY.getPropertyKey(), "Smtp DNS Ret", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAllow8bitmime = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY.getPropertyKey(), "Smtp Allow 8-bit Mime", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSendPartial = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY.getPropertyKey(), "Smtp Send Partial", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslEnable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY.getPropertyKey(), "Smtp SASL Enable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslMechanisms = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY.getPropertyKey(), "Smtp SASL Mechanisms", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslAuthorizationId = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY.getPropertyKey(), "Smtp SASL Authorization ID", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslRealm = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY.getPropertyKey(), "Smtp SASL Realm", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslUseCanonicalHostname = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY.getPropertyKey(), "Smtp SASL Use Canonical Hostname", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpQuitwait = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY.getPropertyKey(), "Smtp Quit Wait", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpReportSuccess = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY.getPropertyKey(), "Smtp Report Success", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslEnable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY.getPropertyKey(), "Smtp SSL Enable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslCheckServerIdentity = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY.getPropertyKey(), "Smtp SSL Check Server Identity", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslTrust = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY.getPropertyKey(), "Smtp SSL Trust", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslProtocols = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY.getPropertyKey(), "Smtp SSL Protocols", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslCipherSuites = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY.getPropertyKey(), "Smtp SSL Cipher Suites", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpStartTlsEnable = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY.getPropertyKey(), "Smtp Start TLS Enabled", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpStartTlsRequired = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY.getPropertyKey(), "Smtp Start TLS Required", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpProxyHost = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY.getPropertyKey(), "Smtp Proxy Host", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpProxyPort = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey(), "Smtp Proxy Port", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSocksHost = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY.getPropertyKey(), "Smtp Socks Host", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSocksPort = new NumberConfigField(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey(), "Smtp Socks Port", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpMailExtension = new TextInputConfigField(EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY.getPropertyKey(), "Smtp Mail Extension", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpUserSet = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_USERSET_KEY.getPropertyKey(), "Smtp User Set", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpNoopStrict = new CheckboxConfigField(EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY.getPropertyKey(), "Smtp NoOp Strict", false, false, FieldGroup.ADVANCED);
        fields.add(mailSmtpPort);
        fields.add(mailSmtpConnectionTimeout);
        fields.add(mailSmtpTimeout);
        fields.add(mailSmtpWriteTimeout);
        fields.add(mailSmtpLocalhost);
        fields.add(mailSmtpLocalAddress);
        fields.add(mailSmtpLocalPort);
        fields.add(mailSmtpEhlo);
        fields.add(mailSmtpAuthMechanisms);
        fields.add(mailSmtpAuthLoginDisable);
        fields.add(mailSmtpAuthPlainDisable);
        fields.add(mailSmtpAuthDigestMd5Disable);
        fields.add(mailSmtpAuthNtlmDisable);
        fields.add(mailSmtpAuthNtlmDomain);
        fields.add(mailSmtpAuthNtlmFlags);
        fields.add(mailSmtpAuthXoauth2Disable);
        fields.add(mailSmtpSubmitter);
        fields.add(mailSmtpDnsNotify);
        fields.add(mailSmtpDnsRet);
        fields.add(mailSmtpAllow8bitmime);
        fields.add(mailSmtpSendPartial);
        fields.add(mailSmtpSaslEnable);
        fields.add(mailSmtpSaslMechanisms);
        fields.add(mailSmtpSaslAuthorizationId);
        fields.add(mailSmtpSaslRealm);
        fields.add(mailSmtpSaslUseCanonicalHostname);
        fields.add(mailSmtpQuitwait);
        fields.add(mailSmtpReportSuccess);
        fields.add(mailSmtpSslEnable);
        fields.add(mailSmtpSslCheckServerIdentity);
        fields.add(mailSmtpSslTrust);
        fields.add(mailSmtpSslProtocols);
        fields.add(mailSmtpSslCipherSuites);
        fields.add(mailSmtpStartTlsEnable);
        fields.add(mailSmtpStartTlsRequired);
        fields.add(mailSmtpProxyHost);
        fields.add(mailSmtpProxyPort);
        fields.add(mailSmtpSocksHost);
        fields.add(mailSmtpSocksPort);
        fields.add(mailSmtpMailExtension);
        fields.add(mailSmtpUserSet);
        fields.add(mailSmtpNoopStrict);

        return fields;
    }

}
