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
import com.synopsys.integration.alert.common.descriptor.config.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorConfig;
import com.synopsys.integration.alert.common.descriptor.config.UIComponent;
import com.synopsys.integration.alert.common.enumeration.FieldGroup;
import com.synopsys.integration.alert.common.enumeration.FieldType;
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
        final ConfigField mailSmtpHost = new ConfigField("mailSmtpHost", "Smtp Host", FieldType.TEXT_INPUT, true, false, FieldGroup.DEFAULT);
        final ConfigField mailSmtpFrom = new ConfigField("mailSmtpFrom", "Smtp From", FieldType.TEXT_INPUT, true, false, FieldGroup.DEFAULT);
        final ConfigField mailSmtpAuth = new ConfigField("mailSmtpAuth", "Smtp Auth", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.DEFAULT);
        final ConfigField mailSmtpUser = new ConfigField("mailSmtpUser", "Smtp User", FieldType.TEXT_INPUT, false, false, FieldGroup.DEFAULT);
        final ConfigField mailSmtpPassword = new ConfigField("mailSmtpPassword", "Smtp Password", FieldType.PASSWORD_INPUT, false, true, FieldGroup.DEFAULT);
        fields.add(mailSmtpHost);
        fields.add(mailSmtpFrom);
        fields.add(mailSmtpAuth);
        fields.add(mailSmtpUser);
        fields.add(mailSmtpPassword);

        // Advanced fields
        final ConfigField mailSmtpPort = new ConfigField("mailSmtpPort", "Smtp Port", FieldType.NUMBER_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpConnectionTimeout = new ConfigField("mailSmtpConnectionTimeout", "Smtp Connection Timeout", FieldType.NUMBER_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpTimeout = new ConfigField("mailSmtpTimeout", "Smtp Timeout", FieldType.NUMBER_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpWriteTimeout = new ConfigField("mailSmtpWriteTimeout", "Smtp Write Timeout", FieldType.NUMBER_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalhost = new ConfigField("mailSmtpLocalhost", "Smtp Localhost", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalAddress = new ConfigField("mailSmtpLocalAddress", "Smtp Local Address", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpLocalPort = new ConfigField("mailSmtpLocalPort", "Smtp Local Port", FieldType.NUMBER_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpEhlo = new ConfigField("mailSmtpEhlo", "Smtp Ehlo", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthMechanisms = new ConfigField("mailSmtpAuthMechanisms", "Smtp Auth Mechanisms", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthLoginDisable = new ConfigField("mailSmtpAuthLoginDisable", "Smtp Auth Login Disable", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthPlainDisable = new ConfigField("mailSmtpAuthPlainDisable", "Smtp Auth Plain Disable", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthDigestMd5Disable = new ConfigField("mailSmtpAuthDigestMd5Disable", "Smtp Auth Digest MD5 Disable", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmDisable = new ConfigField("mailSmtpAuthNtlmDisable", "Smtp Auth Ntlm Disable", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmDomain = new ConfigField("mailSmtpAuthNtlmDomain", "Smtp Auth Ntlm Domain", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthNtlmFlags = new ConfigField("mailSmtpAuthNtlmFlags", "Smtp Auth Ntlm Flags", FieldType.NUMBER_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAuthXoauth2Disable = new ConfigField("mailSmtpAuthXoauth2Disable", "SMTP Auth XOAuth2 Disable", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSubmitter = new ConfigField("mailSmtpSubmitter", "Smtp Submitter", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpDnsNotify = new ConfigField("mailSmtpDnsNotify", "Smtp DNS Notify", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpDnsRet = new ConfigField("mailSmtpDnsRet", "Smtp DNS Ret", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpAllow8bitmime = new ConfigField("mailSmtpAllow8bitmime", "Smtp Allow 8-bit Mime", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSendPartial = new ConfigField("mailSmtpSendPartial", "Smtp Send Partial", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslEnable = new ConfigField("mailSmtpSaslEnable", "Smtp SASL Enable", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslMechanisms = new ConfigField("mailSmtpSaslMechanisms", "Smtp SASL Mechanisms", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslAuthorizationId = new ConfigField("mailSmtpSaslAuthorizationId", "Smtp SASL Authorization ID", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslRealm = new ConfigField("mailSmtpSaslRealm", "Smtp SASL Realm", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSaslUseCanonicalHostname = new ConfigField("mailSmtpSaslUseCanonicalHostname", "Smtp SASL Use Canonical Hostname", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpQuitwait = new ConfigField("mailSmtpQuitwait", "Smtp Quit Wait", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpReportSuccess = new ConfigField("mailSmtpReportSuccess", "Smtp Report Success", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslEnable = new ConfigField("mailSmtpSslEnable", "Smtp SSL Enable", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslCheckServerIdentity = new ConfigField("mailSmtpSslCheckServerIdentity", "Smtp SSL Check Server Identity", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslTrust = new ConfigField("mailSmtpSslTrust", "Smtp SSL Trust", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslProtocols = new ConfigField("mailSmtpSslProtocols", "Smtp SSL Protocols", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSslCipherSuites = new ConfigField("mailSmtpSslCipherSuites", "Smtp SSL Cipher Suites", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpStartTlsEnable = new ConfigField("mailSmtpStartTlsEnable", "Smtp Start TLS Enabled", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpStartTlsRequired = new ConfigField("mailSmtpStartTlsRequired", "Smtp Start TLS Required", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpProxyHost = new ConfigField("mailSmtpProxyHost", "Smtp Proxy Host", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpProxyPort = new ConfigField("mailSmtpProxyPort", "Smtp Proxy Port", FieldType.NUMBER_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSocksHost = new ConfigField("mailSmtpSocksHost", "Smtp Socks Host", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpSocksPort = new ConfigField("mailSmtpSocksPort", "Smtp Socks Port", FieldType.NUMBER_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpMailExtension = new ConfigField("mailSmtpMailExtension", "Smtp Mail Extension", FieldType.TEXT_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpUserSet = new ConfigField("mailSmtpUserSet", "Smtp User Set", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
        final ConfigField mailSmtpNoopStrict = new ConfigField("mailSmtpNoopStrict", "Smtp NoOp Strict", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.ADVANCED);
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
