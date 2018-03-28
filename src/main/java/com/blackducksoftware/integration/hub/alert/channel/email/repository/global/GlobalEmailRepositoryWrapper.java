/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.channel.email.repository.global;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.datasource.SimpleKeyRepositoryWrapper;

@Component
public class GlobalEmailRepositoryWrapper extends SimpleKeyRepositoryWrapper<GlobalEmailConfigEntity, GlobalEmailRepository> {

    @Autowired
    public GlobalEmailRepositoryWrapper(final GlobalEmailRepository repository) {
        super(repository);
    }

    @Override
    public GlobalEmailConfigEntity encryptSensitiveData(final GlobalEmailConfigEntity entity) throws EncryptionException {
        String mailSmtpPassword = entity.getMailSmtpPassword();
        if (StringUtils.isBlank(mailSmtpPassword)) {
            return entity;
        } else {
            final String mailSmtpHost = entity.getMailSmtpHost();
            final String mailSmtpUser = entity.getMailSmtpUser();
            mailSmtpPassword = PasswordEncrypter.encrypt(entity.getMailSmtpPassword());
            final Integer mailSmtpPort = entity.getMailSmtpPort();
            final Integer mailSmtpConnectionTimeout = entity.getMailSmtpConnectionTimeout();
            final Integer mailSmtpTimeout = entity.getMailSmtpTimeout();
            final Integer mailSmtpWriteTimeout = entity.getMailSmtpWriteTimeout();
            final String mailSmtpFrom = entity.getMailSmtpFrom();
            final String mailSmtpLocalhost = entity.getMailSmtpLocalhost();
            final String mailSmtpLocalAddress = entity.getMailSmtpLocalAddress();
            final Integer mailSmtpLocalPort = entity.getMailSmtpLocalPort();
            final Boolean mailSmtpEhlo = entity.getMailSmtpEhlo();
            final Boolean mailSmtpAuth = entity.getMailSmtpAuth();
            final String mailSmtpAuthMechanisms = entity.getMailSmtpAuthMechanisms();
            final Boolean mailSmtpAuthLoginDisable = entity.getMailSmtpAuthLoginDisable();
            final Boolean mailSmtpAuthPlainDisable = entity.getMailSmtpAuthPlainDisable();
            final Boolean mailSmtpAuthDigestMd5Disable = entity.getMailSmtpAuthDigestMd5Disable();
            final Boolean mailSmtpAuthNtlmDisable = entity.getMailSmtpAuthNtlmDisable();
            final String mailSmtpAuthNtlmDomain = entity.getMailSmtpAuthNtlmDomain();
            final Integer mailSmtpAuthNtlmFlags = entity.getMailSmtpAuthNtlmFlags();
            final Boolean mailSmtpAuthXoauth2Disable = entity.getMailSmtpAuthXoauth2Disable();
            final String mailSmtpSubmitter = entity.getMailSmtpSubmitter();
            final String mailSmtpDnsNotify = entity.getMailSmtpDnsNotify();
            final String mailSmtpDnsRet = entity.getMailSmtpDnsRet();
            final Boolean mailSmtpAllow8bitmime = entity.getMailSmtpAllow8bitmime();
            final Boolean mailSmtpSendPartial = entity.getMailSmtpSendPartial();
            final Boolean mailSmtpSaslEnable = entity.getMailSmtpSaslEnable();
            final String mailSmtpSaslMechanisms = entity.getMailSmtpSaslMechanisms();
            final String mailSmtpSaslAuthorizationId = entity.getMailSmtpSaslAuthorizationId();
            final String mailSmtpSaslRealm = entity.getMailSmtpSaslRealm();
            final Boolean mailSmtpSaslUseCanonicalHostname = entity.getMailSmtpSaslUseCanonicalHostname();
            final Boolean mailSmtpQuitwait = entity.getMailSmtpQuitwait();
            final Boolean mailSmtpReportSuccess = entity.getMailSmtpReportSuccess();
            final Boolean mailSmtpSslEnable = entity.getMailSmtpSslEnable();
            final Boolean mailSmtpSslCheckServerIdentity = entity.getMailSmtpSslCheckServerIdentity();
            final String mailSmtpSslTrust = entity.getMailSmtpSslTrust();
            final String mailSmtpSslProtocols = entity.getMailSmtpSslProtocols();
            final String mailSmtpSslCipherSuites = entity.getMailSmtpSslCipherSuites();
            final Boolean mailSmtpStartTlsEnable = entity.getMailSmtpStartTlsEnable();
            final Boolean mailSmtpStartTlsRequired = entity.getMailSmtpStartTlsRequired();
            final String mailSmtpProxyHost = entity.getMailSmtpProxyHost();
            final Integer mailSmtpProxyPort = entity.getMailSmtpProxyPort();
            final String mailSmtpSocksHost = entity.getMailSmtpSocksHost();
            final Integer mailSmtpSocksPort = entity.getMailSmtpSocksPort();
            final String mailSmtpMailExtension = entity.getMailSmtpMailExtension();
            final Boolean mailSmtpUserSet = entity.getMailSmtpUserSet();
            final Boolean mailSmtpNoopStrict = entity.getMailSmtpNoopStrict();

            final GlobalEmailConfigEntity newEntity = new GlobalEmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout,
                    mailSmtpWriteTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpLocalAddress, mailSmtpLocalPort, mailSmtpEhlo, mailSmtpAuth,
                    mailSmtpAuthMechanisms, mailSmtpAuthLoginDisable, mailSmtpAuthPlainDisable, mailSmtpAuthDigestMd5Disable, mailSmtpAuthNtlmDisable,
                    mailSmtpAuthNtlmDomain, mailSmtpAuthNtlmFlags, mailSmtpAuthXoauth2Disable, mailSmtpSubmitter, mailSmtpDnsNotify, mailSmtpDnsRet,
                    mailSmtpAllow8bitmime, mailSmtpSendPartial, mailSmtpSaslEnable, mailSmtpSaslMechanisms, mailSmtpSaslAuthorizationId, mailSmtpSaslRealm,
                    mailSmtpSaslUseCanonicalHostname, mailSmtpQuitwait, mailSmtpReportSuccess, mailSmtpSslEnable, mailSmtpSslCheckServerIdentity, mailSmtpSslTrust,
                    mailSmtpSslProtocols, mailSmtpSslCipherSuites, mailSmtpStartTlsEnable, mailSmtpStartTlsRequired, mailSmtpProxyHost, mailSmtpProxyPort,
                    mailSmtpSocksHost, mailSmtpSocksPort, mailSmtpMailExtension, mailSmtpUserSet, mailSmtpNoopStrict);
            newEntity.setId(entity.getId());
            return newEntity;
        }
    }

    @Override
    public GlobalEmailConfigEntity decryptSensitiveData(final GlobalEmailConfigEntity entity) throws EncryptionException {
        String mailSmtpPassword = entity.getMailSmtpPassword();
        if (StringUtils.isBlank(mailSmtpPassword)) {
            return entity;
        } else {
            final String mailSmtpHost = entity.getMailSmtpHost();
            final String mailSmtpUser = entity.getMailSmtpUser();
            mailSmtpPassword = PasswordDecrypter.decrypt(entity.getMailSmtpPassword());
            final Integer mailSmtpPort = entity.getMailSmtpPort();
            final Integer mailSmtpConnectionTimeout = entity.getMailSmtpConnectionTimeout();
            final Integer mailSmtpTimeout = entity.getMailSmtpTimeout();
            final Integer mailSmtpWriteTimeout = entity.getMailSmtpWriteTimeout();
            final String mailSmtpFrom = entity.getMailSmtpFrom();
            final String mailSmtpLocalhost = entity.getMailSmtpLocalhost();
            final String mailSmtpLocalAddress = entity.getMailSmtpLocalAddress();
            final Integer mailSmtpLocalPort = entity.getMailSmtpLocalPort();
            final Boolean mailSmtpEhlo = entity.getMailSmtpEhlo();
            final Boolean mailSmtpAuth = entity.getMailSmtpAuth();
            final String mailSmtpAuthMechanisms = entity.getMailSmtpAuthMechanisms();
            final Boolean mailSmtpAuthLoginDisable = entity.getMailSmtpAuthLoginDisable();
            final Boolean mailSmtpAuthPlainDisable = entity.getMailSmtpAuthPlainDisable();
            final Boolean mailSmtpAuthDigestMd5Disable = entity.getMailSmtpAuthDigestMd5Disable();
            final Boolean mailSmtpAuthNtlmDisable = entity.getMailSmtpAuthNtlmDisable();
            final String mailSmtpAuthNtlmDomain = entity.getMailSmtpAuthNtlmDomain();
            final Integer mailSmtpAuthNtlmFlags = entity.getMailSmtpAuthNtlmFlags();
            final Boolean mailSmtpAuthXoauth2Disable = entity.getMailSmtpAuthXoauth2Disable();
            final String mailSmtpSubmitter = entity.getMailSmtpSubmitter();
            final String mailSmtpDnsNotify = entity.getMailSmtpDnsNotify();
            final String mailSmtpDnsRet = entity.getMailSmtpDnsRet();
            final Boolean mailSmtpAllow8bitmime = entity.getMailSmtpAllow8bitmime();
            final Boolean mailSmtpSendPartial = entity.getMailSmtpSendPartial();
            final Boolean mailSmtpSaslEnable = entity.getMailSmtpSaslEnable();
            final String mailSmtpSaslMechanisms = entity.getMailSmtpSaslMechanisms();
            final String mailSmtpSaslAuthorizationId = entity.getMailSmtpSaslAuthorizationId();
            final String mailSmtpSaslRealm = entity.getMailSmtpSaslRealm();
            final Boolean mailSmtpSaslUseCanonicalHostname = entity.getMailSmtpSaslUseCanonicalHostname();
            final Boolean mailSmtpQuitwait = entity.getMailSmtpQuitwait();
            final Boolean mailSmtpReportSuccess = entity.getMailSmtpReportSuccess();
            final Boolean mailSmtpSslEnable = entity.getMailSmtpSslEnable();
            final Boolean mailSmtpSslCheckServerIdentity = entity.getMailSmtpSslCheckServerIdentity();
            final String mailSmtpSslTrust = entity.getMailSmtpSslTrust();
            final String mailSmtpSslProtocols = entity.getMailSmtpSslProtocols();
            final String mailSmtpSslCipherSuites = entity.getMailSmtpSslCipherSuites();
            final Boolean mailSmtpStartTlsEnable = entity.getMailSmtpStartTlsEnable();
            final Boolean mailSmtpStartTlsRequired = entity.getMailSmtpStartTlsRequired();
            final String mailSmtpProxyHost = entity.getMailSmtpProxyHost();
            final Integer mailSmtpProxyPort = entity.getMailSmtpProxyPort();
            final String mailSmtpSocksHost = entity.getMailSmtpSocksHost();
            final Integer mailSmtpSocksPort = entity.getMailSmtpSocksPort();
            final String mailSmtpMailExtension = entity.getMailSmtpMailExtension();
            final Boolean mailSmtpUserSet = entity.getMailSmtpUserSet();
            final Boolean mailSmtpNoopStrict = entity.getMailSmtpNoopStrict();

            final GlobalEmailConfigEntity newEntity = new GlobalEmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout,
                    mailSmtpWriteTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpLocalAddress, mailSmtpLocalPort, mailSmtpEhlo, mailSmtpAuth,
                    mailSmtpAuthMechanisms, mailSmtpAuthLoginDisable, mailSmtpAuthPlainDisable, mailSmtpAuthDigestMd5Disable, mailSmtpAuthNtlmDisable,
                    mailSmtpAuthNtlmDomain, mailSmtpAuthNtlmFlags, mailSmtpAuthXoauth2Disable, mailSmtpSubmitter, mailSmtpDnsNotify, mailSmtpDnsRet,
                    mailSmtpAllow8bitmime, mailSmtpSendPartial, mailSmtpSaslEnable, mailSmtpSaslMechanisms, mailSmtpSaslAuthorizationId, mailSmtpSaslRealm,
                    mailSmtpSaslUseCanonicalHostname, mailSmtpQuitwait, mailSmtpReportSuccess, mailSmtpSslEnable, mailSmtpSslCheckServerIdentity, mailSmtpSslTrust,
                    mailSmtpSslProtocols, mailSmtpSslCipherSuites, mailSmtpStartTlsEnable, mailSmtpStartTlsRequired, mailSmtpProxyHost, mailSmtpProxyPort,
                    mailSmtpSocksHost, mailSmtpSocksPort, mailSmtpMailExtension, mailSmtpUserSet, mailSmtpNoopStrict);
            newEntity.setId(entity.getId());
            return newEntity;
        }
    }
}
