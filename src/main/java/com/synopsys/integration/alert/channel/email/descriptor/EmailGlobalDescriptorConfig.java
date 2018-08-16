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
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorConfig;
import com.synopsys.integration.alert.common.descriptor.config.UIComponent;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.enumeration.FieldGroup;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.channel.model.EmailGlobalConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailGlobalDescriptorConfig extends DescriptorConfig {
    public static final String NOT_AN_INTEGER = "Not an Integer.";

    private final EmailGroupChannel emailGroupChannel;

    @Autowired
    public EmailGlobalDescriptorConfig(final EmailGlobalTypeConverter databaseContentConverter, final EmailGlobalRepositoryAccessor repositoryAccessor, final EmailGlobalStartupComponent startupComponent,
            final EmailGroupChannel emailGroupChannel) {
        super(databaseContentConverter, repositoryAccessor, startupComponent);
        this.emailGroupChannel = emailGroupChannel;
    }

    // TODO Global email config doesn't validate properly or give any indication that saving was successful
    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {
        final EmailGlobalConfig emailRestModel = (EmailGlobalConfig) restModel;

        if (StringUtils.isNotBlank(emailRestModel.getMailSmtpPort()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpPort())) {
            fieldErrors.put("mailSmtpPort", NOT_AN_INTEGER);
        }
        if (StringUtils.isNotBlank(emailRestModel.getMailSmtpConnectionTimeout()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpConnectionTimeout())) {
            fieldErrors.put("mailSmtpConnectionTimeout", NOT_AN_INTEGER);
        }
        if (StringUtils.isNotBlank(emailRestModel.getMailSmtpTimeout()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpTimeout())) {
            fieldErrors.put("mailSmtpTimeout", NOT_AN_INTEGER);
        }
    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {
        final EmailGlobalConfigEntity emailEntity = (EmailGlobalConfigEntity) entity;
        emailGroupChannel.testGlobalConfig(emailEntity);
    }

    @Override
    public UIComponent getUiComponent() {
        final List<ConfigField> fields = new ArrayList<>();

        // Default fields
        final ConfigField mailSmtpHost = new TextInputConfigField("mailSmtpHost", "Smtp Host", true, false);
        final ConfigField mailSmtpFrom = new TextInputConfigField("mailSmtpFrom", "Smtp From", true, false);
        final ConfigField mailSmtpAuth = new CheckboxConfigField("mailSmtpAuth", "Smtp Auth", false, false);
        final ConfigField mailSmtpUser = new TextInputConfigField("mailSmtpUser", "Smtp User", false, false);
        final ConfigField mailSmtpPassword = new PasswordConfigField("mailSmtpPassword", "Smtp Password", false);
        fields.add(mailSmtpHost);
        fields.add(mailSmtpFrom);
        fields.add(mailSmtpAuth);
        fields.add(mailSmtpUser);
        fields.add(mailSmtpPassword);

        // Advanced fields
        final ConfigField mailSmtpPort = new NumberConfigField("mailSmtpPort", "Smtp Port", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpConnectionTimeout = new NumberConfigField("mailSmtpConnectionTimeout", "Smtp Connection Timeout", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpTimeout = new NumberConfigField("mailSmtpTimeout", "Smtp Timeout", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpWriteTimeout = new NumberConfigField("mailSmtpWriteTimeout", "Smtp Write Timeout", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalhost = new TextInputConfigField("mailSmtpLocalhost", "Smtp Localhost", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalAddress = new TextInputConfigField("mailSmtpLocalAddress", "Smtp Local Address", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalPort = new NumberConfigField("mailSmtpLocalPort", "Smtp Local Port", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpEhlo = new CheckboxConfigField("mailSmtpEhlo", "Smtp Ehlo", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthMechanisms = new TextInputConfigField("mailSmtpAuthMechanisms", "Smtp Auth Mechanisms", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthLoginDisable = new CheckboxConfigField("mailSmtpAuthLoginDisable", "Smtp Auth Login Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthPlainDisable = new CheckboxConfigField("mailSmtpAuthPlainDisable", "Smtp Auth Plain Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthDigestMd5Disable = new CheckboxConfigField("mailSmtpAuthDigestMd5Disable", "Smtp Auth Digest MD5 Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmDisable = new CheckboxConfigField("mailSmtpAuthNtlmDisable", "Smtp Auth Ntlm Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmDomain = new TextInputConfigField("mailSmtpAuthNtlmDomain", "Smtp Auth Ntlm Domain", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmFlags = new NumberConfigField("mailSmtpAuthNtlmFlags", "Smtp Auth Ntlm Flags", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthXoauth2Disable = new CheckboxConfigField("mailSmtpAuthXoauth2Disable", "SMTP Auth XOAuth2 Disable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSubmitter = new TextInputConfigField("mailSmtpSubmitter", "Smtp Submitter", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpDnsNotify = new TextInputConfigField("mailSmtpDnsNotify", "Smtp DNS Notify", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpDnsRet = new TextInputConfigField("mailSmtpDnsRet", "Smtp DNS Ret", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAllow8bitmime = new CheckboxConfigField("mailSmtpAllow8bitmime", "Smtp Allow 8-bit Mime", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSendPartial = new CheckboxConfigField("mailSmtpSendPartial", "Smtp Send Partial", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslEnable = new CheckboxConfigField("mailSmtpSaslEnable", "Smtp SASL Enable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslMechanisms = new TextInputConfigField("mailSmtpSaslMechanisms", "Smtp SASL Mechanisms", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslAuthorizationId = new TextInputConfigField("mailSmtpSaslAuthorizationId", "Smtp SASL Authorization ID", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslRealm = new TextInputConfigField("mailSmtpSaslRealm", "Smtp SASL Realm", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslUseCanonicalHostname = new CheckboxConfigField("mailSmtpSaslUseCanonicalHostname", "Smtp SASL Use Canonical Hostname", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpQuitwait = new CheckboxConfigField("mailSmtpQuitwait", "Smtp Quit Wait", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpReportSuccess = new CheckboxConfigField("mailSmtpReportSuccess", "Smtp Report Success", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslEnable = new CheckboxConfigField("mailSmtpSslEnable", "Smtp SSL Enable", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslCheckServerIdentity = new CheckboxConfigField("mailSmtpSslCheckServerIdentity", "Smtp SSL Check Server Identity", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslTrust = new TextInputConfigField("mailSmtpSslTrust", "Smtp SSL Trust", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslProtocols = new TextInputConfigField("mailSmtpSslProtocols", "Smtp SSL Protocols", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslCipherSuites = new TextInputConfigField("mailSmtpSslCipherSuites", "Smtp SSL Cipher Suites", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpStartTlsEnable = new CheckboxConfigField("mailSmtpStartTlsEnable", "Smtp Start TLS Enabled", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpStartTlsRequired = new CheckboxConfigField("mailSmtpStartTlsRequired", "Smtp Start TLS Required", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpProxyHost = new TextInputConfigField("mailSmtpProxyHost", "Smtp Proxy Host", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpProxyPort = new NumberConfigField("mailSmtpProxyPort", "Smtp Proxy Port", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSocksHost = new TextInputConfigField("mailSmtpSocksHost", "Smtp Socks Host", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSocksPort = new NumberConfigField("mailSmtpSocksPort", "Smtp Socks Port", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpMailExtension = new TextInputConfigField("mailSmtpMailExtension", "Smtp Mail Extension", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpUserSet = new CheckboxConfigField("mailSmtpUserSet", "Smtp User Set", false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpNoopStrict = new CheckboxConfigField("mailSmtpNoopStrict", "Smtp NoOp Strict", false, false, FieldGroup.ADVANCED);
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

        return new UIComponent("Email", "email", "envelope", fields);
    }

}
