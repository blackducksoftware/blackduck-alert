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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.channel.model.EmailGlobalConfig;
import com.blackducksoftware.integration.alert.web.model.Config;

@Component
public class EmailGlobalContentConverter extends DatabaseContentConverter {

    @Autowired
    public EmailGlobalContentConverter(final ContentConverter contentConverter) {
        super(contentConverter);
    }

    @Override
    public Config getRestModelFromJson(final String json) {
        return getContentConverter().getJsonContent(json, EmailGlobalConfig.class);
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final Config restModel) {
        final EmailGlobalConfig emailRestModel = (EmailGlobalConfig) restModel;
        final Integer smtpPort = getContentConverter().getIntegerValue(emailRestModel.getMailSmtpPort());
        final Integer smtpConnectionTimeout = getContentConverter().getIntegerValue(emailRestModel.getMailSmtpConnectionTimeout());
        final Integer smtpTimeout = getContentConverter().getIntegerValue(emailRestModel.getMailSmtpTimeout());
        final Integer smtpWriteTimeout = getContentConverter().getIntegerValue(emailRestModel.getMailSmtpWriteTimeout());
        final Integer smtpLocalPort = getContentConverter().getIntegerValue(emailRestModel.getMailSmtpLocalPort());
        final Integer smtpAuthNtlmFlags = getContentConverter().getIntegerValue(emailRestModel.getMailSmtpAuthNtlmFlags());
        final Integer smtpProxyPort = getContentConverter().getIntegerValue(emailRestModel.getMailSmtpProxyPort());
        final Integer smtpSocksPort = getContentConverter().getIntegerValue(emailRestModel.getMailSmtpSocksPort());
        final EmailGlobalConfigEntity emailEntity = new EmailGlobalConfigEntity(emailRestModel.getMailSmtpHost(), emailRestModel.getMailSmtpUser(), emailRestModel.getMailSmtpPassword(), smtpPort, smtpConnectionTimeout,
                smtpTimeout, smtpWriteTimeout, emailRestModel.getMailSmtpFrom(), emailRestModel.getMailSmtpLocalhost(),
                emailRestModel.getMailSmtpLocalAddress(), smtpLocalPort, emailRestModel.getMailSmtpEhlo(), emailRestModel.getMailSmtpAuth(), emailRestModel.getMailSmtpAuthMechanisms(),
                emailRestModel.getMailSmtpAuthLoginDisable(), emailRestModel.getMailSmtpAuthPlainDisable(), emailRestModel.getMailSmtpAuthDigestMd5Disable(), emailRestModel.getMailSmtpAuthNtlmDisable(),
                emailRestModel.getMailSmtpAuthNtlmDomain(), smtpAuthNtlmFlags, emailRestModel.getMailSmtpAuthXoauth2Disable(), emailRestModel.getMailSmtpSubmitter(), emailRestModel.getMailSmtpDnsNotify(),
                emailRestModel.getMailSmtpDnsRet(), emailRestModel.getMailSmtpAllow8bitmime(), emailRestModel.getMailSmtpSendPartial(), emailRestModel.getMailSmtpSaslEnable(), emailRestModel.getMailSmtpSaslMechanisms(),
                emailRestModel.getMailSmtpSaslAuthorizationId(), emailRestModel.getMailSmtpSaslRealm(), emailRestModel.getMailSmtpSaslUseCanonicalHostname(), emailRestModel.getMailSmtpQuitwait(), emailRestModel.getMailSmtpReportSuccess(),
                emailRestModel.getMailSmtpSslEnable(), emailRestModel.getMailSmtpSslCheckServerIdentity(), emailRestModel.getMailSmtpSslTrust(), emailRestModel.getMailSmtpSslProtocols(), emailRestModel.getMailSmtpSslCipherSuites(),
                emailRestModel.getMailSmtpStartTlsEnable(), emailRestModel.getMailSmtpStartTlsRequired(), emailRestModel.getMailSmtpProxyHost(), smtpProxyPort, emailRestModel.getMailSmtpSocksHost(),
                smtpSocksPort, emailRestModel.getMailSmtpMailExtension(), emailRestModel.getMailSmtpUserSet(), emailRestModel.getMailSmtpNoopStrict());
        addIdToEntityPK(emailRestModel.getId(), emailEntity);
        return emailEntity;
    }

    @Override
    public Config populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final EmailGlobalConfigEntity emailEntity = (EmailGlobalConfigEntity) entity;
        final String id = getContentConverter().getStringValue(emailEntity.getId());
        final String smtpPort = getContentConverter().getStringValue(emailEntity.getMailSmtpPort());
        final String smtpConnectionTimeout = getContentConverter().getStringValue(emailEntity.getMailSmtpConnectionTimeout());
        final String smtpTimeout = getContentConverter().getStringValue(emailEntity.getMailSmtpTimeout());
        final String smtpWriteTimeout = getContentConverter().getStringValue(emailEntity.getMailSmtpWriteTimeout());
        final String smtpLocalPort = getContentConverter().getStringValue(emailEntity.getMailSmtpLocalPort());
        final String smtpAuthNtlmFlags = getContentConverter().getStringValue(emailEntity.getMailSmtpAuthNtlmFlags());
        final String smtpProxyPort = getContentConverter().getStringValue(emailEntity.getMailSmtpProxyPort());
        final String smtpSocksPort = getContentConverter().getStringValue(emailEntity.getMailSmtpSocksPort());
        return new EmailGlobalConfig(id, emailEntity.getMailSmtpHost(), emailEntity.getMailSmtpUser(), emailEntity.getMailSmtpPassword(), StringUtils.isNotBlank(emailEntity.getMailSmtpPassword()), smtpPort,
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
