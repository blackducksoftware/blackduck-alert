/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;

// TODO: Remove this when we no longer support FieldModels.
public class EmailGlobalConfigModelTransformer {
    private final Map<String, String> propertyKeyToResponseKey;

    public EmailGlobalConfigModelTransformer() {
        propertyKeyToResponseKey = Map.ofEntries(
            Map.entry(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), "host"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), "from"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), "auth"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), "user"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), "password"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), "port"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), "connectionTimeout"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), "timeout"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey(), "writeTimeout"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY.getPropertyKey(), "localhost"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY.getPropertyKey(), "localaddress"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey(), "localport"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), "ehlo"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY.getPropertyKey(), "authMechanisms"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY.getPropertyKey(), "authLoginDisable"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY.getPropertyKey(), "authPlainDisable"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY.getPropertyKey(), "authDigestMd5Disable"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY.getPropertyKey(), "authNtlmDisable"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY.getPropertyKey(), "authNtlmDomain"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey(), "authNtlmFlags"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY.getPropertyKey(), "authXoauth2Disable"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY.getPropertyKey(), "submitter"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY.getPropertyKey(), "dsnNotify"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY.getPropertyKey(), "dsnRet"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY.getPropertyKey(), "allow8bitmime"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY.getPropertyKey(), "sendPartial"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY.getPropertyKey(), "saslEnabled"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY.getPropertyKey(), "saslMechanisms"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY.getPropertyKey(), "saslAuthorizationId"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY.getPropertyKey(), "saslRealm"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY.getPropertyKey(), "saslUseCanonicalHostname"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY.getPropertyKey(), "quitwait"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY.getPropertyKey(), "reportSuccess"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY.getPropertyKey(), "sslEnable"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY.getPropertyKey(), "sslCheckServerIdentity"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY.getPropertyKey(), "sslTrust"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY.getPropertyKey(), "sslProtocols"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY.getPropertyKey(), "sslCipherSuites"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY.getPropertyKey(), "startTlsEnable"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY.getPropertyKey(), "startTlsRequired"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY.getPropertyKey(), "proxyHost"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey(), "proxyPort"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY.getPropertyKey(), "socksHost"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey(), "socksPort"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY.getPropertyKey(), "mailExtension"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_USERSET_KEY.getPropertyKey(), "userSet"),
            Map.entry(EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY.getPropertyKey(), "noopStrict")
        );
    }


    public FieldModel toFieldModel(EmailGlobalConfigModel resource) {
        HashMap<String, FieldValueModel> resourceAsMap = new HashMap<>();

        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), toFieldValueModel(resource.host));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), toFieldValueModel(resource.from));

        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), toFieldValueModel(resource.auth));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), toFieldValueModel(resource.user));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), toFieldValueModel(resource.password));

        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), toFieldValueModel(resource.port));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), toFieldValueModel(resource.connectionTimeout));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), toFieldValueModel(resource.timeout));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey(), toFieldValueModel(resource.writeTimeout));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY.getPropertyKey(), toFieldValueModel(resource.localhost));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY.getPropertyKey(), toFieldValueModel(resource.localaddress));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey(), toFieldValueModel(resource.localport));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), toFieldValueModel(resource.ehlo));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY.getPropertyKey(), toFieldValueModel(resource.authMechanisms));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY.getPropertyKey(), toFieldValueModel(resource.authLoginDisable));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY.getPropertyKey(), toFieldValueModel(resource.authPlainDisable));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY.getPropertyKey(), toFieldValueModel(resource.authDigestMd5Disable));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY.getPropertyKey(), toFieldValueModel(resource.authNtlmDisable));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY.getPropertyKey(), toFieldValueModel(resource.authNtlmDomain));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey(), toFieldValueModel(resource.authNtlmFlags));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY.getPropertyKey(), toFieldValueModel(resource.authXoauth2Disable));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY.getPropertyKey(), toFieldValueModel(resource.submitter));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY.getPropertyKey(), toFieldValueModel(resource.dsnNotify));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY.getPropertyKey(), toFieldValueModel(resource.dsnRet));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY.getPropertyKey(), toFieldValueModel(resource.allow8bitmime));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY.getPropertyKey(), toFieldValueModel(resource.sendPartial));

        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY.getPropertyKey(), toFieldValueModel(resource.saslEnabled));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY.getPropertyKey(), toFieldValueModel(resource.saslMechanisms));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY.getPropertyKey(), toFieldValueModel(resource.saslAuthorizationId));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY.getPropertyKey(), toFieldValueModel(resource.saslRealm));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY.getPropertyKey(), toFieldValueModel(resource.saslUseCanonicalHostname));

        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY.getPropertyKey(), toFieldValueModel(resource.quitwait));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY.getPropertyKey(), toFieldValueModel(resource.reportSuccess));

        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY.getPropertyKey(), toFieldValueModel(resource.sslEnable));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY.getPropertyKey(), toFieldValueModel(resource.sslCheckServerIdentity));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY.getPropertyKey(), toFieldValueModel(resource.sslTrust));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY.getPropertyKey(), toFieldValueModel(resource.sslProtocols));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY.getPropertyKey(), toFieldValueModel(resource.sslCipherSuites));

        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY.getPropertyKey(), toFieldValueModel(resource.startTlsEnable));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY.getPropertyKey(), toFieldValueModel(resource.startTlsRequired));

        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY.getPropertyKey(), toFieldValueModel(resource.proxyHost));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey(), toFieldValueModel(resource.proxyPort));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY.getPropertyKey(), toFieldValueModel(resource.socksHost));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey(), toFieldValueModel(resource.socksPort));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY.getPropertyKey(), toFieldValueModel(resource.mailExtension));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_USERSET_KEY.getPropertyKey(), toFieldValueModel(resource.userSet));
        resourceAsMap.put(EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY.getPropertyKey(), toFieldValueModel(resource.noopStrict));

        return new FieldModel(resource.getId(), ChannelKeys.EMAIL.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), resourceAsMap);
    }

    public FieldValueModel toFieldValueModel(Integer value) {
        if (value == null) {
            return createEmptyFieldValueModel();
        }
        return toFieldValueModel(value.toString());
    }

    public FieldValueModel toFieldValueModel(Boolean value) {
        if (value == null) {
            return createEmptyFieldValueModel();
        }
        return toFieldValueModel(value.toString());
    }

    public FieldValueModel toFieldValueModel(String value) {
        if (value == null) {
            return createEmptyFieldValueModel();
        }
        return new FieldValueModel(List.of(value),true);
    }

    public FieldValueModel createEmptyFieldValueModel() {
        return new FieldValueModel(List.of(), false);
    }

    public EmailGlobalConfigModel fromConfigurationModel(ConfigurationModel configurationModel){
        EmailGlobalConfigModel concreteModel = new EmailGlobalConfigModel();

        concreteModel.setId(String.valueOf(configurationModel.getConfigurationId()));

        concreteModel.host = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_HOST_KEY);
        concreteModel.from = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_FROM_KEY);

        concreteModel.auth = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_KEY);
        concreteModel.user = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_USER_KEY);
        concreteModel.password = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY);

        concreteModel.port = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_PORT_KEY);
        concreteModel.connectionTimeout = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY);
        concreteModel.timeout = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY);
        concreteModel.writeTimeout = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY);
        concreteModel.localhost = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY);
        concreteModel.localaddress = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY);
        concreteModel.localport = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY);
        concreteModel.ehlo = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_EHLO_KEY);
        concreteModel.authMechanisms = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY);
        concreteModel.authLoginDisable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY);
        concreteModel.authPlainDisable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY);
        concreteModel.authDigestMd5Disable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY);
        concreteModel.authNtlmDisable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY);
        concreteModel.authNtlmDomain = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY);
        concreteModel.authNtlmFlags = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY);
        concreteModel.authXoauth2Disable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY);
        concreteModel.submitter = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY);
        concreteModel.dsnNotify = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY);
        concreteModel.dsnRet = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY);
        concreteModel.allow8bitmime = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY);
        concreteModel.sendPartial = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY);

        concreteModel.saslEnabled = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY);
        concreteModel.saslMechanisms = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY);
        concreteModel.saslAuthorizationId = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY);
        concreteModel.saslRealm = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY);
        concreteModel.saslUseCanonicalHostname = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY);

        concreteModel.quitwait = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY);
        concreteModel.reportSuccess = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY);

        concreteModel.sslEnable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY);
        concreteModel.sslCheckServerIdentity = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY);
        concreteModel.sslTrust = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY);
        concreteModel.sslProtocols = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY);
        concreteModel.sslCipherSuites = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY);

        concreteModel.startTlsEnable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY);
        concreteModel.startTlsRequired = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY);

        concreteModel.proxyHost = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY);
        concreteModel.proxyPort = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY);
        concreteModel.socksHost = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY);
        concreteModel.socksPort = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY);
        concreteModel.mailExtension = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY);
        concreteModel.userSet = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_USERSET_KEY);
        concreteModel.noopStrict = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY);

        return concreteModel;
    }

    public Boolean getBooleanFromModel(ConfigurationModel configurationModel, EmailPropertyKeys emailPropertyKey) {
        return getValueFromModel(configurationModel, emailPropertyKey)
            .map(Boolean::valueOf)
            .orElse(null);
    }

    public Integer getIntegerFromModel(ConfigurationModel configurationModel, EmailPropertyKeys emailPropertyKey) {
        return getValueFromModel(configurationModel, emailPropertyKey)
            .map(Integer::valueOf)
            .orElse(null);
    }

    public String getStringFromModel(ConfigurationModel configurationModel, EmailPropertyKeys emailPropertyKey) {
        return getValueFromModel(configurationModel, emailPropertyKey)
            .orElse(null);
    }

    public Optional<String> getValueFromModel(ConfigurationModel configurationModel, EmailPropertyKeys emailPropertyKey) {
        return configurationModel
            .getField(emailPropertyKey.getPropertyKey())
            .flatMap(ConfigurationFieldModel::getFieldValue);
    }

    public AlertFieldStatus toResponseFieldStatus(AlertFieldStatus alertFieldStatus) {
        return new AlertFieldStatus(propertyKeyToResponseKey.get(alertFieldStatus.getFieldName()), alertFieldStatus.getSeverity(),alertFieldStatus.getFieldMessage());
    }
}
