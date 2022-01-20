/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailEnvironmentVariableHandlerFactory implements EnvironmentVariableHandlerFactory {
    public static final String ENVIRONMENT_VARIABLE_PREFIX = "ALERT_CHANNEL_EMAIL_";
    public static final String ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX = ENVIRONMENT_VARIABLE_PREFIX + "MAIL_";

    // fields in model
    public static final String AUTH_REQUIRED_KEY = ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_AUTH";
    public static final String EMAIL_FROM_KEY = ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_FROM";
    public static final String EMAIL_HOST_KEY = ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_HOST";
    public static final String AUTH_PASSWORD_KEY = ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_PASSWORD";
    public static final String EMAIL_PORT_KEY = ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_PORT";
    public static final String AUTH_USER_KEY = ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_USER";

    public static final Set<String> EMAIL_CONFIGURATION_KEYSET = Set.of(
        AUTH_REQUIRED_KEY, EMAIL_FROM_KEY, EMAIL_HOST_KEY, AUTH_PASSWORD_KEY, EMAIL_PORT_KEY, AUTH_USER_KEY);

    // additional property keys
    public static final Set<String> OLD_ADDITIONAL_PROPERTY_KEYSET = Set.of(ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_ALLOW8BITMIME",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_AUTH_DIGEST-MD5_DISABLE",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_AUTH_LOGIN_DISABLE",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_AUTH_MECHANISMS",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_AUTH_NTLM_DISABLE",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_AUTH_NTLM_DOMAIN",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_AUTH_NTLM_FLAGS",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_AUTH_PLAIN_DISABLE",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_AUTH_XOAUTH2_DISABLE",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_CONNECTIONTIMEOUT",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_DSN_NOTIFY",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_DSN_RET",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_EHLO",

        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_LOCALADDRESS",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_LOCALHOST",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_LOCALPORT",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_MAILEXTENSION",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_NOOP_STRICT",

        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_PROXY_HOST",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_PROXY_PORT",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_QUITWAIT",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_REPORTSUCCESS",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SASL_AUTHORIZATIONID",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SASL_ENABLE",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SASL_MECHANISMS",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SASL_REALM",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SASL_USECANONICALHOSTNAME",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SENDPARTIAL",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SOCKS_HOST",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SOCKS_PORT",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SSL_CHECKSERVERIDENTITY",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SSL_CIPHERSUITES",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SSL_ENABLE",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SSL_PROTOCOLS",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SSL_TRUST",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_STARTTLS_ENABLE",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_STARTTLS_REQUIRED",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_SUBMITTER",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_TIMEOUT",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_USERSET",
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_WRITETIMEOUT");

    private EmailGlobalConfigAccessor configAccessor;
    private EnvironmentVariableUtility environmentVariableUtility;

    @Autowired
    public EmailEnvironmentVariableHandlerFactory(EmailGlobalConfigAccessor configAccessor, EnvironmentVariableUtility environmentVariableUtility) {
        this.configAccessor = configAccessor;
        this.environmentVariableUtility = environmentVariableUtility;
    }

    @Override
    public EnvironmentVariableHandler build() {
        Set<String> variableNames = Stream.concat(EMAIL_CONFIGURATION_KEYSET.stream(), OLD_ADDITIONAL_PROPERTY_KEYSET.stream())
            .collect(Collectors.toSet());
        return new EnvironmentVariableHandler(ChannelKeys.EMAIL.getDisplayName(), variableNames, this::isConfigurationMissing, this::updateConfiguration);
    }

    private Boolean isConfigurationMissing() {
        return configAccessor.getConfigurationCount() <= 0;
    }

    private Properties updateConfiguration() {
        Properties properties = new Properties();
        EmailGlobalConfigModel configModel = new EmailGlobalConfigModel();
        configModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        configureEmailSettings(configModel);
        configureAdditionalProperties(properties, configModel);

        EmailGlobalConfigModel obfuscatedModel = configModel.obfuscate();

        obfuscatedModel.getSmtpHost().ifPresent(value -> properties.put(EMAIL_HOST_KEY, value));
        obfuscatedModel.getSmtpPort().map(String::valueOf).ifPresent(value -> properties.put(EMAIL_PORT_KEY, value));
        obfuscatedModel.getSmtpFrom().ifPresent(value -> properties.put(EMAIL_FROM_KEY, value));
        obfuscatedModel.getSmtpAuth().map(String::valueOf).ifPresent(value -> properties.put(AUTH_REQUIRED_KEY, value));
        obfuscatedModel.getSmtpPassword().ifPresent(value -> properties.put(AUTH_PASSWORD_KEY, value));
        obfuscatedModel.getSmtpUsername().ifPresent(value -> properties.put(AUTH_USER_KEY, value));

        if (!properties.isEmpty()) {
            configAccessor.createConfiguration(configModel);
        }

        return properties;
    }

    private void configureEmailSettings(EmailGlobalConfigModel configuration) {
        environmentVariableUtility.getEnvironmentValue(EMAIL_HOST_KEY)
            .ifPresent(configuration::setSmtpHost);

        environmentVariableUtility.getEnvironmentValue(EMAIL_PORT_KEY)
            .map(Integer::valueOf)
            .ifPresent(configuration::setSmtpPort);

        environmentVariableUtility.getEnvironmentValue(EMAIL_FROM_KEY)
            .ifPresent(configuration::setSmtpFrom);

        environmentVariableUtility.getEnvironmentValue(AUTH_REQUIRED_KEY)
            .map(Boolean::valueOf)
            .ifPresent(configuration::setSmtpAuth);

        environmentVariableUtility.getEnvironmentValue(AUTH_USER_KEY)
            .ifPresent(configuration::setSmtpUsername);

        environmentVariableUtility.getEnvironmentValue(AUTH_PASSWORD_KEY)
            .ifPresent(configuration::setSmtpPassword);

    }

    private void configureAdditionalProperties(Properties properties, EmailGlobalConfigModel configuration) {
        Map<String, String> additionalProperties = new HashMap<>();
        for (String additionalPropertyName : OLD_ADDITIONAL_PROPERTY_KEYSET) {
            if (environmentVariableUtility.hasEnvironmentValue(additionalPropertyName)) {
                String javamailPropertyName = EmailEnvironmentVariableHandlerFactory.convertVariableNameToJavamailPropertyKey(additionalPropertyName);
                String value = environmentVariableUtility.getEnvironmentValue(additionalPropertyName).orElse(null);
                additionalProperties.put(javamailPropertyName, value);
                properties.put(additionalPropertyName, value);
            }
        }
        configuration.setAdditionalJavaMailProperties(additionalProperties);
    }

    public static String convertVariableNameToJavamailPropertyKey(String environmentVariableName) {
        String propertyKey = environmentVariableName.substring(ENVIRONMENT_VARIABLE_PREFIX.length());
        propertyKey = propertyKey.replace("_", ".").toLowerCase();
        return propertyKey;
    }
}
