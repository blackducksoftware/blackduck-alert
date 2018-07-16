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
package com.blackducksoftware.integration.alert.channel.email;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGlobalConfigEntity;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGlobalConfigRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

@Component
public class EmailGlobalContentConverter extends DatabaseContentConverter {
    private final ContentConverter contentConverter;

    @Autowired
    public EmailGlobalContentConverter(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    @Override
    public ConfigRestModel getRestModelFromJson(final String json) {
        final Optional<EmailGlobalConfigRestModel> restModel = contentConverter.getContent(json, EmailGlobalConfigRestModel.class);
        if (restModel.isPresent()) {
            return restModel.get();
        }
        return null;
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final ConfigRestModel restModel) {
        final EmailGlobalConfigRestModel emailRestModel = (EmailGlobalConfigRestModel) restModel;
        final int smtpPort = Integer.parseInt(emailRestModel.getMailSmtpPort());
        final int smtpConnectionTimeout = Integer.parseInt(emailRestModel.getMailSmtpConnectionTimeout());
        final int smtpTimeout = Integer.parseInt(emailRestModel.getMailSmtpTimeout());
        final int smtpWriteTimeout = Integer.parseInt(emailRestModel.getMailSmtpWriteTimeout());
        final int smtpLocalPort = Integer.parseInt(emailRestModel.getMailSmtpLocalPort());
        final int smtpAuthNtlmFlags = Integer.parseInt(emailRestModel.getMailSmtpAuthNtlmFlags());
        final int smtpProxyPort = Integer.parseInt(emailRestModel.getMailSmtpProxyPort());
        final int smtpSocksPort = Integer.parseInt(emailRestModel.getMailSmtpSocksPort());
        return new EmailGlobalConfigEntity(emailRestModel.getMailSmtpHost(), emailRestModel.getMailSmtpUser(), emailRestModel.getMailSmtpPassword(), smtpPort, smtpConnectionTimeout,
                smtpTimeout, smtpWriteTimeout, emailRestModel.getMailSmtpFrom(), emailRestModel.getMailSmtpLocalhost(),
                emailRestModel.getMailSmtpLocalAddress(), smtpLocalPort, emailRestModel.getMailSmtpEhlo(), emailRestModel.getMailSmtpAuth(), emailRestModel.getMailSmtpAuthMechanisms(),
                emailRestModel.getMailSmtpAuthLoginDisable(), emailRestModel.getMailSmtpAuthPlainDisable(), emailRestModel.getMailSmtpAuthDigestMd5Disable(), emailRestModel.getMailSmtpAuthNtlmDisable(),
                emailRestModel.getMailSmtpAuthNtlmDomain(), smtpAuthNtlmFlags, emailRestModel.getMailSmtpAuthXoauth2Disable(), emailRestModel.getMailSmtpSubmitter(), emailRestModel.getMailSmtpDnsNotify(),
                emailRestModel.getMailSmtpDnsRet(), emailRestModel.getMailSmtpAllow8bitmime(), emailRestModel.getMailSmtpSendPartial(), emailRestModel.getMailSmtpSaslEnable(), emailRestModel.getMailSmtpSaslMechanisms(),
                emailRestModel.getMailSmtpSaslAuthorizationId(), emailRestModel.getMailSmtpSaslRealm(), emailRestModel.getMailSmtpSaslUseCanonicalHostname(), emailRestModel.getMailSmtpQuitwait(), emailRestModel.getMailSmtpReportSuccess(),
                emailRestModel.getMailSmtpSslEnable(), emailRestModel.getMailSmtpSslCheckServerIdentity(), emailRestModel.getMailSmtpSslTrust(), emailRestModel.getMailSmtpSslProtocols(), emailRestModel.getMailSmtpSslCipherSuites(),
                emailRestModel.getMailSmtpStartTlsEnable(), emailRestModel.getMailSmtpStartTlsRequired(), emailRestModel.getMailSmtpProxyHost(), smtpProxyPort, emailRestModel.getMailSmtpSocksHost(),
                smtpSocksPort, emailRestModel.getMailSmtpMailExtension(), emailRestModel.getMailSmtpUserSet(), emailRestModel.getMailSmtpNoopStrict());
    }

    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final EmailGlobalConfigEntity emailEntity = (EmailGlobalConfigEntity) entity;
        final String id = String.valueOf(emailEntity.getId());
        final String smtpPort = String.valueOf(emailEntity.getMailSmtpPort());
        final String smtpConnectionTimeout = String.valueOf(emailEntity.getMailSmtpConnectionTimeout());
        final String smtpTimeout = String.valueOf(emailEntity.getMailSmtpTimeout());
        final String smtpWriteTimeout = String.valueOf(emailEntity.getMailSmtpWriteTimeout());
        final String smtpLocalPort = String.valueOf(emailEntity.getMailSmtpLocalPort());
        final String smtpAuthNtlmFlags = String.valueOf(emailEntity.getMailSmtpAuthNtlmFlags());
        final String smtpProxyPort = String.valueOf(emailEntity.getMailSmtpProxyPort());
        final String smtpSocksPort = String.valueOf(emailEntity.getMailSmtpSocksPort());
        return new EmailGlobalConfigRestModel(id, emailEntity.getMailSmtpHost(), emailEntity.getMailSmtpUser(), emailEntity.getMailSmtpPassword(), StringUtils.isNotBlank(emailEntity.getMailSmtpPassword()), smtpPort,
                smtpConnectionTimeout, smtpTimeout, smtpWriteTimeout, emailEntity.getMailSmtpFrom(), emailEntity.getMailSmtpLocalhost(), emailEntity.getMailSmtpLocalAddress(), smtpLocalPort, emailEntity.getMailSmtpEhlo(),
                emailEntity.getMailSmtpAuth(), emailEntity.getMailSmtpAuthMechanisms(),
                emailEntity.getMailSmtpAuthLoginDisable(), emailEntity.getMailSmtpAuthPlainDisable(), emailEntity.getMailSmtpAuthDigestMd5Disable(), emailEntity.getMailSmtpAuthNtlmDisable(),
                emailEntity.getMailSmtpAuthNtlmDomain(), smtpAuthNtlmFlags, emailEntity.getMailSmtpAuthXoauth2Disable(), emailEntity.getMailSmtpSubmitter(), emailEntity.getMailSmtpDnsNotify(),
                emailEntity.getMailSmtpDnsRet(), emailEntity.getMailSmtpAllow8bitmime(), emailEntity.getMailSmtpSendPartial(), emailEntity.getMailSmtpSaslEnable(), emailEntity.getMailSmtpSaslMechanisms(),
                emailEntity.getMailSmtpSaslAuthorizationId(), emailEntity.getMailSmtpSaslRealm(), emailEntity.getMailSmtpSaslUseCanonicalHostname(), emailEntity.getMailSmtpQuitwait(), emailEntity.getMailSmtpReportSuccess(),
                emailEntity.getMailSmtpSslEnable(), emailEntity.getMailSmtpSslCheckServerIdentity(), emailEntity.getMailSmtpSslTrust(), emailEntity.getMailSmtpSslProtocols(), emailEntity.getMailSmtpSslCipherSuites(),
                emailEntity.getMailSmtpStartTlsEnable(), emailEntity.getMailSmtpStartTlsRequired(), emailEntity.getMailSmtpProxyHost(), smtpProxyPort, emailEntity.getMailSmtpSocksHost(),
                smtpSocksPort, emailEntity.getMailSmtpMailExtension(), emailEntity.getMailSmtpUserSet(), emailEntity.getMailSmtpNoopStrict());
    }

}
