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
package com.synopsys.integration.alert.database.channel.email;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.security.EncryptedRepositoryAccessor;
import com.synopsys.integration.alert.database.security.EncryptionUtility;

@Component
public class EmailGlobalRepositoryAccessor extends EncryptedRepositoryAccessor {
    public static final String SMTP_PASSWORD_FIELD_PROPERTY_KEY = "email_channel_smtp_password";
    private final EmailGlobalRepository repository;

    @Autowired
    public EmailGlobalRepositoryAccessor(final EmailGlobalRepository repository, final EncryptionUtility encryptionUtility) {
        super(repository, encryptionUtility);
        this.repository = repository;
    }

    @Override
    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        final EmailGlobalConfigEntity emailEntity = (EmailGlobalConfigEntity) entity;
        final EmailGlobalConfigEntity newEntity = new EmailGlobalConfigEntity(
            emailEntity.getMailSmtpHost(),
            emailEntity.getMailSmtpUser(),
            encryptValue(SMTP_PASSWORD_FIELD_PROPERTY_KEY, emailEntity.getMailSmtpPassword()),
            emailEntity.getMailSmtpPort(),
            emailEntity.getMailSmtpConnectionTimeout(),
            emailEntity.getMailSmtpTimeout(),
            emailEntity.getMailSmtpWriteTimeout(),
            emailEntity.getMailSmtpFrom(),
            emailEntity.getMailSmtpLocalhost(),
            emailEntity.getMailSmtpLocalAddress(),
            emailEntity.getMailSmtpLocalPort(),
            emailEntity.getMailSmtpEhlo(),
            emailEntity.getMailSmtpAuth(),
            emailEntity.getMailSmtpAuthMechanisms(),
            emailEntity.getMailSmtpAuthLoginDisable(),
            emailEntity.getMailSmtpAuthPlainDisable(),
            emailEntity.getMailSmtpAuthDigestMd5Disable(),
            emailEntity.getMailSmtpAuthNtlmDisable(),
            emailEntity.getMailSmtpAuthNtlmDomain(),
            emailEntity.getMailSmtpAuthNtlmFlags(),
            emailEntity.getMailSmtpAuthXoauth2Disable(),
            emailEntity.getMailSmtpSubmitter(),
            emailEntity.getMailSmtpDnsNotify(),
            emailEntity.getMailSmtpDnsRet(),
            emailEntity.getMailSmtpAllow8bitmime(),
            emailEntity.getMailSmtpSendPartial(),
            emailEntity.getMailSmtpSaslEnable(),
            emailEntity.getMailSmtpSaslMechanisms(),
            emailEntity.getMailSmtpSaslAuthorizationId(),
            emailEntity.getMailSmtpSaslRealm(),
            emailEntity.getMailSmtpSaslUseCanonicalHostname(),
            emailEntity.getMailSmtpQuitwait(),
            emailEntity.getMailSmtpReportSuccess(),
            emailEntity.getMailSmtpSslEnable(),
            emailEntity.getMailSmtpSslCheckServerIdentity(),
            emailEntity.getMailSmtpSslTrust(),
            emailEntity.getMailSmtpSslProtocols(),
            emailEntity.getMailSmtpSslCipherSuites(),
            emailEntity.getMailSmtpStartTlsEnable(),
            emailEntity.getMailSmtpStartTlsRequired(),
            emailEntity.getMailSmtpProxyHost(),
            emailEntity.getMailSmtpProxyPort(),
            emailEntity.getMailSmtpSocksHost(),
            emailEntity.getMailSmtpSocksPort(),
            emailEntity.getMailSmtpMailExtension(),
            emailEntity.getMailSmtpUserSet(),
            emailEntity.getMailSmtpNoopStrict());
        newEntity.setId(emailEntity.getId());
        return repository.save(newEntity);
    }

    @Override
    public DatabaseEntity decryptEntity(final DatabaseEntity entity) {
        final EmailGlobalConfigEntity emailEntity = (EmailGlobalConfigEntity) entity;
        final Optional<String> decryptedValue = decryptValue(SMTP_PASSWORD_FIELD_PROPERTY_KEY, emailEntity.getMailSmtpPassword());
        final EmailGlobalConfigEntity newEntity = new EmailGlobalConfigEntity(
            emailEntity.getMailSmtpHost(),
            emailEntity.getMailSmtpUser(),
            decryptedValue.orElse(null),
            emailEntity.getMailSmtpPort(),
            emailEntity.getMailSmtpConnectionTimeout(),
            emailEntity.getMailSmtpTimeout(),
            emailEntity.getMailSmtpWriteTimeout(),
            emailEntity.getMailSmtpFrom(),
            emailEntity.getMailSmtpLocalhost(),
            emailEntity.getMailSmtpLocalAddress(),
            emailEntity.getMailSmtpLocalPort(),
            emailEntity.getMailSmtpEhlo(),
            emailEntity.getMailSmtpAuth(),
            emailEntity.getMailSmtpAuthMechanisms(),
            emailEntity.getMailSmtpAuthLoginDisable(),
            emailEntity.getMailSmtpAuthPlainDisable(),
            emailEntity.getMailSmtpAuthDigestMd5Disable(),
            emailEntity.getMailSmtpAuthNtlmDisable(),
            emailEntity.getMailSmtpAuthNtlmDomain(),
            emailEntity.getMailSmtpAuthNtlmFlags(),
            emailEntity.getMailSmtpAuthXoauth2Disable(),
            emailEntity.getMailSmtpSubmitter(),
            emailEntity.getMailSmtpDnsNotify(),
            emailEntity.getMailSmtpDnsRet(),
            emailEntity.getMailSmtpAllow8bitmime(),
            emailEntity.getMailSmtpSendPartial(),
            emailEntity.getMailSmtpSaslEnable(),
            emailEntity.getMailSmtpSaslMechanisms(),
            emailEntity.getMailSmtpSaslAuthorizationId(),
            emailEntity.getMailSmtpSaslRealm(),
            emailEntity.getMailSmtpSaslUseCanonicalHostname(),
            emailEntity.getMailSmtpQuitwait(),
            emailEntity.getMailSmtpReportSuccess(),
            emailEntity.getMailSmtpSslEnable(),
            emailEntity.getMailSmtpSslCheckServerIdentity(),
            emailEntity.getMailSmtpSslTrust(),
            emailEntity.getMailSmtpSslProtocols(),
            emailEntity.getMailSmtpSslCipherSuites(),
            emailEntity.getMailSmtpStartTlsEnable(),
            emailEntity.getMailSmtpStartTlsRequired(),
            emailEntity.getMailSmtpProxyHost(),
            emailEntity.getMailSmtpProxyPort(),
            emailEntity.getMailSmtpSocksHost(),
            emailEntity.getMailSmtpSocksPort(),
            emailEntity.getMailSmtpMailExtension(),
            emailEntity.getMailSmtpUserSet(),
            emailEntity.getMailSmtpNoopStrict());
        newEntity.setId(emailEntity.getId());
        return newEntity;
    }
}
