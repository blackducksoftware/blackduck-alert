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
import com.synopsys.integration.alert.common.enumeration.FieldGroup;

@Component
public class EmailGlobalUIConfig extends UIConfig {
    public static final String KEY_HOST = "mail.smtp.host";
    public static final String KEY_FROM = "mail.smtp.from";
    public static final String KEY_AUTH = "mail.smtp.auth";
    public static final String KEY_USER = "mail.smtp.user";
    public static final String KEY_PASSWORD = "mail.smtp.password";
    public static final String KEY_PORT = "mail.smtp.port";
    public static final String KEY_CONNECTION_TIMEOUT = "mail.smtp.connection.timeout";
    public static final String KEY_TIMEOUT = "mail.smtp.timeout";
    public static final String KEY_WRITE_TIMEOUT = "mail.smtp.write.timeout";
    public static final String KEY_LOCALHOST = "mail.smtp.localhost";
    public static final String KEY_LOCAL_ADDRESS = "mail.smtp.local.address";
    public static final String KEY_LOCAL_PORT = "mail.smtp.local.port";
    public static final String KEY_EHLO = "mail.smtp.ehlo";
    public static final String KEY_AUTH_MECHANISMS = "mail.smtp.auth.mechanisms";
    public static final String KEY_AUTH_LOGIN_DISABLE = "mail.smtp.auth.login.disable";
    public static final String KEY_AUTH_PLAIN_DISABLE = "mail.smtp.auth.plain.disable";
    public static final String KEY_AUTH_DIGEST_MD5_DISABLE = "mail.smtp.auth.digest.md5.disable";
    public static final String KEY_AUTH_NTLM_DISABLE = "mail.smtp.auth.ntlm.disable";
    public static final String KEY_AUTH_NTLM_DOMAIN = "mail.smtp.auth.ntlm.domain";
    public static final String KEY_AUTH_NTLM_FLAGS = "mail.smtp.ntlm.flags";
    public static final String KEY_AUTH_XOAUTH2_DISABLE = "mail.smtp.xoauth2.disable";
    public static final String KEY_SUBMITTER = "mail.smtp.submitter";
    public static final String KEY_DNS_NOTIFY = "mail.smtp.dns.notify";
    public static final String KEY_DNS_RET = "mail.smtp.dns.ret";
    public static final String KEY_ALLOW_8BIT_MIME = "mail.smtp.allow.8bit.mime";
    public static final String KEY_SEND_PARTIAL = "mail.smtp.send.partial";
    public static final String KEY_SASL_ENABLE = "mail.smtp.sasl.enable";
    public static final String KEY_SASL_MECHANISMS = "mail.smtp.sasl.mechanisms";
    public static final String KEY_SASL_AUTHORIZATION_ID = "mail.smtp.sasl.authorication.id";
    public static final String KEY_SASL_REALM = "mail.smtp.sasl.realm";
    public static final String KEY_SASL_USE_CANONICAL_HOSTNAME = "mail.smtp.sasl.use.canonical.hostname";
    public static final String KEY_QUIT_WAIT = "mail.smtp.quit.wait";
    public static final String KEY_REPORT_SUCCESS = "mail.smtp.report.success";
    public static final String KEY_SSL_ENABLE = "mail.smtp.ssl.enable";
    public static final String KEY_SSL_CHECK_SERVER_IDENTITY = "mail.smtp.ssl.check.server.identity";
    public static final String KEY_SSL_TRUST = "mail.smtp.ssl.trust";
    public static final String KEY_SSL_PROTOCOLS = "mail.smtp.ssl.protocols";
    public static final String KEY_SSL_CIPHER_SUITES = "mail.smtp.ssl.cipher.suites";
    public static final String KEY_START_TLS_ENABLE = "mail.smtp.start.tls.enable";
    public static final String KEY_START_TLS_REQUIRED = "mail.smtp.start.tls.required";
    public static final String KEY_PROXY_HOST = "mail.smtp.proxy.host";
    public static final String KEY_PROXY_PORT = "mail.smtp.proxy.port";
    public static final String KEY_SOCKS_HOST = "mail.smtp.socks.host";
    public static final String KEY_SOCKS_PORT = "mail.smtp.socks.port";
    public static final String KEY_MAIL_EXTENSION = "mail.smtp.mail.extension";
    public static final String KEY_USER_SET = "mail.smtp.user.set";
    public static final String KEY_NOOP_STRICT = "mail.smtp.noop.strict";

    @Override
    public UIComponent generateUIComponent() {
        return new UIComponent("Email", "email", EmailGroupChannel.COMPONENT_NAME, "envelope", setupFields());
    }

    public List<ConfigField> setupFields() {
        final List<ConfigField> fields = new ArrayList<>();

        // Default fields
        final ConfigField mailSmtpHost = new TextInputConfigField(KEY_HOST, "Smtp Host", true, false);
        final ConfigField mailSmtpFrom = new TextInputConfigField(KEY_FROM, "Smtp From", true, false);
        final ConfigField mailSmtpAuth = new CheckboxConfigField(KEY_AUTH, "Smtp Auth", false, false);
        final ConfigField mailSmtpUser = new TextInputConfigField(KEY_USER, "Smtp User", false, false);
        final ConfigField mailSmtpPassword = new PasswordConfigField(KEY_PASSWORD, "Smtp Password", false);
        fields.add(mailSmtpHost);
        fields.add(mailSmtpFrom);
        fields.add(mailSmtpAuth);
        fields.add(mailSmtpUser);
        fields.add(mailSmtpPassword);

        // Advanced fields
        final ConfigField mailSmtpPort = new NumberConfigField(KEY_PORT, "Smtp Port", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpConnectionTimeout = new NumberConfigField(KEY_CONNECTION_TIMEOUT, "Smtp Connection Timeout", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpTimeout = new NumberConfigField(KEY_TIMEOUT, "Smtp Timeout", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpWriteTimeout = new NumberConfigField(KEY_WRITE_TIMEOUT, "Smtp Write Timeout", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalhost = new TextInputConfigField(KEY_LOCALHOST, "Smtp Localhost", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalAddress = new TextInputConfigField(KEY_LOCAL_ADDRESS, "Smtp Local Address", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalPort = new NumberConfigField(KEY_LOCAL_PORT, "Smtp Local Port", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpEhlo = new CheckboxConfigField(KEY_EHLO, "Smtp Ehlo", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthMechanisms = new TextInputConfigField(KEY_AUTH_MECHANISMS, "Smtp Auth Mechanisms", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthLoginDisable = new CheckboxConfigField(KEY_AUTH_LOGIN_DISABLE, "Smtp Auth Login Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthPlainDisable = new CheckboxConfigField(KEY_AUTH_PLAIN_DISABLE, "Smtp Auth Plain Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthDigestMd5Disable = new CheckboxConfigField(KEY_AUTH_DIGEST_MD5_DISABLE, "Smtp Auth Digest MD5 Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmDisable = new CheckboxConfigField(KEY_AUTH_NTLM_DISABLE, "Smtp Auth Ntlm Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmDomain = new TextInputConfigField(KEY_AUTH_NTLM_DOMAIN, "Smtp Auth Ntlm Domain", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmFlags = new NumberConfigField(KEY_AUTH_NTLM_FLAGS, "Smtp Auth Ntlm Flags", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthXoauth2Disable = new CheckboxConfigField(KEY_AUTH_XOAUTH2_DISABLE, "SMTP Auth XOAuth2 Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSubmitter = new TextInputConfigField(KEY_SUBMITTER, "Smtp Submitter", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpDnsNotify = new TextInputConfigField(KEY_DNS_NOTIFY, "Smtp DNS Notify", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpDnsRet = new TextInputConfigField(KEY_DNS_RET, "Smtp DNS Ret", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAllow8bitmime = new CheckboxConfigField(KEY_ALLOW_8BIT_MIME, "Smtp Allow 8-bit Mime", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSendPartial = new CheckboxConfigField(KEY_SEND_PARTIAL, "Smtp Send Partial", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslEnable = new CheckboxConfigField(KEY_SASL_ENABLE, "Smtp SASL Enable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslMechanisms = new TextInputConfigField(KEY_SASL_MECHANISMS, "Smtp SASL Mechanisms", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslAuthorizationId = new TextInputConfigField(KEY_SASL_AUTHORIZATION_ID, "Smtp SASL Authorization ID", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslRealm = new TextInputConfigField(KEY_SASL_REALM, "Smtp SASL Realm", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslUseCanonicalHostname = new CheckboxConfigField(KEY_SASL_USE_CANONICAL_HOSTNAME, "Smtp SASL Use Canonical Hostname", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpQuitwait = new CheckboxConfigField(KEY_QUIT_WAIT, "Smtp Quit Wait", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpReportSuccess = new CheckboxConfigField(KEY_REPORT_SUCCESS, "Smtp Report Success", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslEnable = new CheckboxConfigField(KEY_SSL_ENABLE, "Smtp SSL Enable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslCheckServerIdentity = new CheckboxConfigField(KEY_SSL_CHECK_SERVER_IDENTITY, "Smtp SSL Check Server Identity", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslTrust = new TextInputConfigField(KEY_SSL_TRUST, "Smtp SSL Trust", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslProtocols = new TextInputConfigField(KEY_SSL_PROTOCOLS, "Smtp SSL Protocols", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslCipherSuites = new TextInputConfigField(KEY_SSL_CIPHER_SUITES, "Smtp SSL Cipher Suites", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpStartTlsEnable = new CheckboxConfigField(KEY_START_TLS_ENABLE, "Smtp Start TLS Enabled", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpStartTlsRequired = new CheckboxConfigField(KEY_START_TLS_REQUIRED, "Smtp Start TLS Required", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpProxyHost = new TextInputConfigField(KEY_PROXY_HOST, "Smtp Proxy Host", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpProxyPort = new NumberConfigField(KEY_PROXY_PORT, "Smtp Proxy Port", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSocksHost = new TextInputConfigField(KEY_SOCKS_HOST, "Smtp Socks Host", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSocksPort = new NumberConfigField(KEY_SOCKS_PORT, "Smtp Socks Port", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpMailExtension = new TextInputConfigField(KEY_MAIL_EXTENSION, "Smtp Mail Extension", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpUserSet = new CheckboxConfigField(KEY_USER_SET, "Smtp User Set", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpNoopStrict = new CheckboxConfigField(KEY_NOOP_STRICT, "Smtp NoOp Strict", false, false, FieldGroup.ADVANCED);
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
