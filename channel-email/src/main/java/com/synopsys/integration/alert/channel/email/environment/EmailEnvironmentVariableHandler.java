/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailEnvironmentVariableHandler implements EnvironmentVariableHandler {
    public static final String ENVIRONMENT_VARIABLE_PREFIX = "ALERT_CHANNEL_EMAIL_MAIL_";

    // fields in model
    public static final String AUTH_REQUIRED_KEY = ENVIRONMENT_VARIABLE_PREFIX + "SMTP_AUTH";
    public static final String SMTP_FROM_KEY = ENVIRONMENT_VARIABLE_PREFIX + "SMTP_FROM";
    public static final String SMTP_HOST_KEY = ENVIRONMENT_VARIABLE_PREFIX + "SMTP_HOST";
    public static final String AUTH_PASSWORD_KEY = ENVIRONMENT_VARIABLE_PREFIX + "SMTP_PASSWORD";
    public static final String SMTP_PORT_KEY = ENVIRONMENT_VARIABLE_PREFIX + "SMTP_PORT";
    public static final String AUTH_USER_KEY = ENVIRONMENT_VARIABLE_PREFIX + "SMTP_USER";

    public static final Set<String> EMAIL_CONFIGURATION_KEYSET = Set.of(
        AUTH_REQUIRED_KEY, SMTP_FROM_KEY, SMTP_HOST_KEY, AUTH_PASSWORD_KEY, SMTP_PORT_KEY, AUTH_USER_KEY);

    // additional property keys
    public static final Set<String> ADDITIONAL_PROPERTY_KEYSET = Set.of(ENVIRONMENT_VARIABLE_PREFIX + "SMTP_ALLOW8BITMIME",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_AUTH_DIGEST-MD5_DISABLE",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_AUTH_LOGIN_DISABLE",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_AUTH_MECHANISMS",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_AUTH_NTLM_DISABLE",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_AUTH_NTLM_DOMAIN",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_AUTH_NTLM_FLAGS",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_AUTH_PLAIN_DISABLE",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_AUTH_XOAUTH2_DISABLE",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_CONNECTIONTIMEOUT",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_DSN_NOTIFY",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_DSN_RET",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_EHLO",

        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_LOCALADDRESS",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_LOCALHOST",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_LOCALPORT",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_MAILEXTENSION",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_NOOP_STRICT",

        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_PROXY_HOST",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_PROXY_PORT",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_QUITWAIT",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_REPORTSUCCESS",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SASL_AUTHORIZATIONID",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SASL_ENABLE",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SASL_MECHANISMS",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SASL_REALM",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SASL_USECANONICALHOSTNAME",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SENDPARTIAL",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SOCKS_HOST",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SOCKS_PORT",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SSL_CHECKSERVERIDENTITY",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SSL_CIPHERSUITES",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SSL_ENABLE",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SSL_PROTOCOLS",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SSL_TRUST",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_STARTTLS_ENABLE",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_STARTTLS_REQUIRED",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_SUBMITTER",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_TIMEOUT",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_USERSET",
        ENVIRONMENT_VARIABLE_PREFIX + "SMTP_WRITETIMEOUT");

    private EmailGlobalConfigAccessor configAccessor;
    private EnvironmentVariableUtility environmentVariableUtility;

    @Autowired
    public EmailEnvironmentVariableHandler(EmailGlobalConfigAccessor configAccessor, EnvironmentVariableUtility environmentVariableUtility) {
        this.configAccessor = configAccessor;
        this.environmentVariableUtility = environmentVariableUtility;
    }

    @Override
    public String getName() {
        return ChannelKeys.EMAIL.getDisplayName();
    }

    @Override
    public Set<String> getVariableNames() {
        return Stream.concat(EMAIL_CONFIGURATION_KEYSET.stream(), ADDITIONAL_PROPERTY_KEYSET.stream())
            .collect(Collectors.toSet());
    }

    @Override
    public Properties updateFromEnvironment() {
        Properties properties = new Properties();
        boolean anyEmailConfigsExist = configAccessor.getConfigurationCount() > 0;
        if (anyEmailConfigsExist) {
            EmailGlobalConfigModel configModel = new EmailGlobalConfigModel();
            configureEmailSettings(properties, configModel);
            configureAdditionalProperties(properties, configModel);

            configAccessor.createConfiguration(configModel);
        }
        return properties;
    }

    private void configureEmailSettings(Properties properties, EmailGlobalConfigModel configuration) {
        environmentVariableUtility.getEnvironmentValue(SMTP_HOST_KEY)
            .ifPresent(configuration::setHost);

        environmentVariableUtility.getEnvironmentValue(SMTP_PORT_KEY)
            .map(Integer::valueOf)
            .ifPresent(configuration::setPort);

        environmentVariableUtility.getEnvironmentValue(SMTP_FROM_KEY)
            .ifPresent(configuration::setFrom);

        environmentVariableUtility.getEnvironmentValue(AUTH_REQUIRED_KEY)
            .map(Boolean::valueOf)
            .ifPresent(configuration::setAuth);

        environmentVariableUtility.getEnvironmentValue(AUTH_USER_KEY)
            .ifPresent(configuration::setUsername);

        environmentVariableUtility.getEnvironmentValue(AUTH_PASSWORD_KEY)
            .ifPresent(configuration::setPassword);

    }

    private void configureAdditionalProperties(Properties properties, EmailGlobalConfigModel configuration) {
        Map<String, String> additionalProperties = new HashMap<>();
        for (String additionalPropertyName : ADDITIONAL_PROPERTY_KEYSET) {
            if (environmentVariableUtility.hasEnvironmentValue(additionalPropertyName)) {
                String javamailPropertyName = convertVariableNameToJavamailPropertyKey(additionalPropertyName);
                additionalProperties.put(javamailPropertyName, environmentVariableUtility.getEnvironmentValue(additionalPropertyName).orElse(null));
            }
        }
        configuration.setAdditionalJavaMailProperties(additionalProperties);
    }

    private String convertVariableNameToJavamailPropertyKey(String environmentVariableName) {
        String propertyKey = environmentVariableName.substring(ENVIRONMENT_VARIABLE_PREFIX.length());
        propertyKey = propertyKey.replace(".", "_").toLowerCase();
        return propertyKey;
    }
}
