/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
public class EmailGlobalConfigResponseTransformer {
    private final Map<String, String> propertyKeyToResponseKey;

    public EmailGlobalConfigResponseTransformer() {
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


    public FieldModel toFieldModel(EmailGlobalConfigResponse resource) {
        HashMap<String, FieldValueModel> responseAsMap = new HashMap<>();

        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), toFieldValueModel(resource.host));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), toFieldValueModel(resource.from));

        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), toFieldValueModel(resource.auth));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), toFieldValueModel(resource.user));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), toFieldValueModel(resource.password));

        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), toFieldValueModel(resource.port));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey(), toFieldValueModel(resource.connectionTimeout));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey(), toFieldValueModel(resource.timeout));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey(), toFieldValueModel(resource.writeTimeout));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY.getPropertyKey(), toFieldValueModel(resource.localhost));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY.getPropertyKey(), toFieldValueModel(resource.localaddress));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey(), toFieldValueModel(resource.localport));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), toFieldValueModel(resource.ehlo));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY.getPropertyKey(), toFieldValueModel(resource.authMechanisms));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY.getPropertyKey(), toFieldValueModel(resource.authLoginDisable));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY.getPropertyKey(), toFieldValueModel(resource.authPlainDisable));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY.getPropertyKey(), toFieldValueModel(resource.authDigestMd5Disable));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY.getPropertyKey(), toFieldValueModel(resource.authNtlmDisable));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY.getPropertyKey(), toFieldValueModel(resource.authNtlmDomain));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey(), toFieldValueModel(resource.authNtlmFlags));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY.getPropertyKey(), toFieldValueModel(resource.authXoauth2Disable));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY.getPropertyKey(), toFieldValueModel(resource.submitter));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY.getPropertyKey(), toFieldValueModel(resource.dsnNotify));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY.getPropertyKey(), toFieldValueModel(resource.dsnRet));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY.getPropertyKey(), toFieldValueModel(resource.allow8bitmime));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY.getPropertyKey(), toFieldValueModel(resource.sendPartial));

        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY.getPropertyKey(), toFieldValueModel(resource.saslEnabled));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY.getPropertyKey(), toFieldValueModel(resource.saslMechanisms));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY.getPropertyKey(), toFieldValueModel(resource.saslAuthorizationId));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY.getPropertyKey(), toFieldValueModel(resource.saslRealm));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY.getPropertyKey(), toFieldValueModel(resource.saslUseCanonicalHostname));

        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY.getPropertyKey(), toFieldValueModel(resource.quitwait));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY.getPropertyKey(), toFieldValueModel(resource.reportSuccess));

        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY.getPropertyKey(), toFieldValueModel(resource.sslEnable));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY.getPropertyKey(), toFieldValueModel(resource.sslCheckServerIdentity));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY.getPropertyKey(), toFieldValueModel(resource.sslTrust));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY.getPropertyKey(), toFieldValueModel(resource.sslProtocols));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY.getPropertyKey(), toFieldValueModel(resource.sslCipherSuites));

        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY.getPropertyKey(), toFieldValueModel(resource.startTlsEnable));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY.getPropertyKey(), toFieldValueModel(resource.startTlsRequired));

        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY.getPropertyKey(), toFieldValueModel(resource.proxyHost));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey(), toFieldValueModel(resource.proxyPort));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY.getPropertyKey(), toFieldValueModel(resource.socksHost));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey(), toFieldValueModel(resource.socksPort));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY.getPropertyKey(), toFieldValueModel(resource.mailExtension));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_USERSET_KEY.getPropertyKey(), toFieldValueModel(resource.userSet));
        responseAsMap.put(EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY.getPropertyKey(), toFieldValueModel(resource.noopStrict));

        return new FieldModel(resource.getId(), ChannelKeys.EMAIL.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), responseAsMap);
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

    public EmailGlobalConfigResponse fromConfigurationModel(ConfigurationModel configurationModel){
        EmailGlobalConfigResponse configResponse = new EmailGlobalConfigResponse();

        configResponse.setId(String.valueOf(configurationModel.getConfigurationId()));

        configResponse.host = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_HOST_KEY);
        configResponse.from = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_FROM_KEY);

        configResponse.auth = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_KEY);
        configResponse.user = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_USER_KEY);
        configResponse.password = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY);

        configResponse.port = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_PORT_KEY);
        configResponse.connectionTimeout = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY);
        configResponse.timeout = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY);
        configResponse.writeTimeout = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY);
        configResponse.localhost = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_LOCALHOST_KEY);
        configResponse.localaddress = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_LOCALHOST_ADDRESS_KEY);
        configResponse.localport = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY);
        configResponse.ehlo = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_EHLO_KEY);
        configResponse.authMechanisms = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_MECHANISMS_KEY);
        configResponse.authLoginDisable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_DISABLE_KEY);
        configResponse.authPlainDisable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY);
        configResponse.authDigestMd5Disable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY);
        configResponse.authNtlmDisable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DISABLE_KEY);
        configResponse.authNtlmDomain = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_DOMAIN_KEY);
        configResponse.authNtlmFlags = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY);
        configResponse.authXoauth2Disable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY);
        configResponse.submitter = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SUBMITTER_KEY);
        configResponse.dsnNotify = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_DSN_NOTIFY_KEY);
        configResponse.dsnRet = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_DSN_RET_KEY);
        configResponse.allow8bitmime = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_ALLOW_8_BITMIME_KEY);
        configResponse.sendPartial = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SEND_PARTIAL_KEY);

        configResponse.saslEnabled = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SASL_ENABLE_KEY);
        configResponse.saslMechanisms = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SASL_MECHANISMS_KEY);
        configResponse.saslAuthorizationId = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SASL_AUTHORIZATION_ID_KEY);
        configResponse.saslRealm = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY);
        configResponse.saslUseCanonicalHostname = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY);

        configResponse.quitwait = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_QUITWAIT_KEY);
        configResponse.reportSuccess = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_REPORT_SUCCESS_KEY);

        configResponse.sslEnable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SSL_ENABLE_KEY);
        configResponse.sslCheckServerIdentity = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY);
        configResponse.sslTrust = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SSL_TRUST_KEY);
        configResponse.sslProtocols = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SSL_PROTOCOLS_KEY);
        configResponse.sslCipherSuites = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SSL_CIPHERSUITES_KEY);

        configResponse.startTlsEnable = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_STARTTLS_ENABLE_KEY);
        configResponse.startTlsRequired = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_STARTTLS_REQUIRED_KEY);

        configResponse.proxyHost = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_PROXY_HOST_KEY);
        configResponse.proxyPort = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY);
        configResponse.socksHost = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SOCKS_HOST_KEY);
        configResponse.socksPort = getIntegerFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY);
        configResponse.mailExtension = getStringFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_MAILEXTENSION_KEY);
        configResponse.userSet = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_USERSET_KEY);
        configResponse.noopStrict = getBooleanFromModel(configurationModel, EmailPropertyKeys.JAVAMAIL_NOOP_STRICT_KEY);

        return configResponse;
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
