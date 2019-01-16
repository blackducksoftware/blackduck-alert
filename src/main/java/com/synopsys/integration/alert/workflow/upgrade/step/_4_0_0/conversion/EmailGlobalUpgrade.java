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
package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.deprecated.channel.email.EmailGlobalRepository;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Component
public class EmailGlobalUpgrade extends DataUpgrade {
    private final FieldCreatorUtil fieldCreatorUtil;

    @Autowired
    public EmailGlobalUpgrade(final EmailGlobalRepository repository, final BaseConfigurationAccessor configurationAccessor, final FieldCreatorUtil fieldCreatorUtil) {
        super(EmailChannel.COMPONENT_NAME, repository, ConfigContextEnum.GLOBAL, configurationAccessor);
        this.fieldCreatorUtil = fieldCreatorUtil;
    }

    @Override
    public List<ConfigurationFieldModel> convertEntityToFieldList(final DatabaseEntity databaseEntity) {
        final EmailGlobalConfigEntity entity = (EmailGlobalConfigEntity) databaseEntity;
        final List<ConfigurationFieldModel> fieldModels = new LinkedList<>();

        final Boolean mailSmtpAllow8bitmime = entity.getMailSmtpAllow8bitmime();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY.getPropertyKey(), mailSmtpAllow8bitmime, fieldModels);

        final Boolean mailSmtpAuth = entity.getMailSmtpAuth();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), mailSmtpAuth, fieldModels);

        final Boolean mailSmtpAuthDigestMd5Disable = entity.getMailSmtpAuthDigestMd5Disable();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY.getPropertyKey(), mailSmtpAuthDigestMd5Disable, fieldModels);

        final Boolean mailSmtpAuthLoginDisable = entity.getMailSmtpAuthLoginDisable();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY.getPropertyKey(), mailSmtpAuthLoginDisable, fieldModels);

        final String mailSmtpAuthMechanisms = entity.getMailSmtpAuthMechanisms();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY.getPropertyKey(), mailSmtpAuthMechanisms, fieldModels);

        final Boolean mailSmtpAuthNtlmDisable = entity.getMailSmtpAuthNtlmDisable();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY.getPropertyKey(), mailSmtpAuthNtlmDisable, fieldModels);

        final String mailSmtpAuthNtlmDomain = entity.getMailSmtpAuthNtlmDomain();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY.getPropertyKey(), mailSmtpAuthNtlmDomain, fieldModels);

        final Integer mailSmtpAuthNtlmFlags = entity.getMailSmtpAuthNtlmFlags();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey(), mailSmtpAuthNtlmFlags, fieldModels);

        final Boolean mailSmtpAuthPlainDisable = entity.getMailSmtpAuthPlainDisable();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY.getPropertyKey(), mailSmtpAuthPlainDisable, fieldModels);

        final Boolean mailSmtpAuthXoauth2Disable = entity.getMailSmtpAuthXoauth2Disable();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY.getPropertyKey(), mailSmtpAuthXoauth2Disable, fieldModels);

        final Integer mailSmtpConnectionTimeout = entity.getMailSmtpConnectionTimeout();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), mailSmtpConnectionTimeout, fieldModels);

        final String mailSmtpDnsNotify = entity.getMailSmtpDnsNotify();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY.getPropertyKey(), mailSmtpDnsNotify, fieldModels);

        final String mailSmtpDnsRet = entity.getMailSmtpDnsRet();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY.getPropertyKey(), mailSmtpDnsRet, fieldModels);

        final Boolean mailSmtpEhlo = entity.getMailSmtpEhlo();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), mailSmtpEhlo, fieldModels);

        final String mailSmtpFrom = entity.getMailSmtpFrom();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), mailSmtpFrom, fieldModels);

        final String mailSmtpHost = entity.getMailSmtpHost();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), mailSmtpHost, fieldModels);

        final String mailSmtpLocalAddress = entity.getMailSmtpLocalAddress();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY.getPropertyKey(), mailSmtpLocalAddress, fieldModels);

        final String mailSmtpLocalhost = entity.getMailSmtpLocalhost();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY.getPropertyKey(), mailSmtpLocalhost, fieldModels);

        final Integer mailSmtpLocalPort = entity.getMailSmtpLocalPort();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey(), mailSmtpLocalPort, fieldModels);

        final String mailSmtpMailExtension = entity.getMailSmtpMailExtension();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY.getPropertyKey(), mailSmtpMailExtension, fieldModels);

        final Boolean mailSmtpNoopStrict = entity.getMailSmtpNoopStrict();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY.getPropertyKey(), mailSmtpNoopStrict, fieldModels);

        final String mailSmtpPassword = entity.getMailSmtpPassword();
        fieldCreatorUtil.addSecureFieldModel(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), mailSmtpPassword, fieldModels);

        final Integer mailSmtpPort = entity.getMailSmtpPort();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), mailSmtpPort, fieldModels);

        final String mailSmtpProxyHost = entity.getMailSmtpProxyHost();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY.getPropertyKey(), mailSmtpProxyHost, fieldModels);

        final Integer mailSmtpProxyPort = entity.getMailSmtpProxyPort();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey(), mailSmtpProxyPort, fieldModels);

        final Boolean mailSmtpQuitwait = entity.getMailSmtpQuitwait();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY.getPropertyKey(), mailSmtpQuitwait, fieldModels);

        final Boolean mailSmtpReportSuccess = entity.getMailSmtpReportSuccess();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY.getPropertyKey(), mailSmtpReportSuccess, fieldModels);

        final String mailSmtpSaslAuthorizationId = entity.getMailSmtpSaslAuthorizationId();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY.getPropertyKey(), mailSmtpSaslAuthorizationId, fieldModels);

        final Boolean mailSmtpSaslEnable = entity.getMailSmtpSaslEnable();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY.getPropertyKey(), mailSmtpSaslEnable, fieldModels);

        final String mailSmtpSaslMechanisms = entity.getMailSmtpSaslMechanisms();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY.getPropertyKey(), mailSmtpSaslMechanisms, fieldModels);

        final String mailSmtpSaslRealm = entity.getMailSmtpSaslRealm();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY.getPropertyKey(), mailSmtpSaslRealm, fieldModels);

        final Boolean mailSmtpSaslUseCanonicalHostname = entity.getMailSmtpSaslUseCanonicalHostname();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY.getPropertyKey(), mailSmtpSaslUseCanonicalHostname, fieldModels);

        final Boolean mailSmtpSendPartial = entity.getMailSmtpSendPartial();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY.getPropertyKey(), mailSmtpSendPartial, fieldModels);

        final String mailSmtpSocksHost = entity.getMailSmtpSocksHost();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY.getPropertyKey(), mailSmtpSocksHost, fieldModels);

        final Integer mailSmtpSocksPort = entity.getMailSmtpSocksPort();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey(), mailSmtpSocksPort, fieldModels);

        final Boolean mailSmtpSslCheckServerIdentity = entity.getMailSmtpSslCheckServerIdentity();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY.getPropertyKey(), mailSmtpSslCheckServerIdentity, fieldModels);

        final String mailSmtpSslCipherSuites = entity.getMailSmtpSslCipherSuites();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY.getPropertyKey(), mailSmtpSslCipherSuites, fieldModels);

        final Boolean mailSmtpSslEnable = entity.getMailSmtpSslEnable();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY.getPropertyKey(), mailSmtpSslEnable, fieldModels);

        final String mailSmtpSslProtocols = entity.getMailSmtpSslProtocols();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY.getPropertyKey(), mailSmtpSslProtocols, fieldModels);

        final String mailSmtpSslTrust = entity.getMailSmtpSslTrust();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY.getPropertyKey(), mailSmtpSslTrust, fieldModels);

        final Boolean mailSmtpStartTlsEnable = entity.getMailSmtpStartTlsEnable();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY.getPropertyKey(), mailSmtpStartTlsEnable, fieldModels);

        final Boolean mailSmtpStartTlsRequired = entity.getMailSmtpStartTlsRequired();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY.getPropertyKey(), mailSmtpStartTlsRequired, fieldModels);

        final String mailSmtpSubmitter = entity.getMailSmtpSubmitter();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY.getPropertyKey(), mailSmtpSubmitter, fieldModels);

        final Integer mailSmtpTimeout = entity.getMailSmtpTimeout();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), mailSmtpTimeout, fieldModels);

        final String mailSmtpUser = entity.getMailSmtpUser();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), mailSmtpUser, fieldModels);

        final Boolean mailSmtpUserSet = entity.getMailSmtpUserSet();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_USERSET_KEY.getPropertyKey(), mailSmtpUserSet, fieldModels);

        final Integer mailSmtpWriteTimeout = entity.getMailSmtpWriteTimeout();
        fieldCreatorUtil.addFieldModel(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey(), mailSmtpWriteTimeout, fieldModels);

        return fieldModels;
    }
}
