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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
@Deprecated(forRemoval = true)
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
    public static final Set<String> OLD_ADDITIONAL_PROPERTY_KEYSET = Set.of(
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_ALLOW8BITMIME",
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
        ENVIRONMENT_VARIABLE_JAVAMAIL_PREFIX + "SMTP_WRITETIMEOUT"
    );

    private final EmailGlobalConfigAccessor configAccessor;
    private final EnvironmentVariableUtility environmentVariableUtility;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private EmailGlobalConfigurationValidator validator;

    @Autowired
    public EmailEnvironmentVariableHandlerFactory(EmailGlobalConfigAccessor configAccessor, EnvironmentVariableUtility environmentVariableUtility, EmailGlobalConfigurationValidator validator) {
        this.configAccessor = configAccessor;
        this.environmentVariableUtility = environmentVariableUtility;
        this.validator = validator;
    }

    @Override
    public EnvironmentVariableHandler build() {
        Set<String> variableNames = Stream.concat(EMAIL_CONFIGURATION_KEYSET.stream(), OLD_ADDITIONAL_PROPERTY_KEYSET.stream())
            .collect(Collectors.toSet());
        return new EnvironmentVariableHandler(ChannelKeys.EMAIL.getDisplayName(), variableNames, this::isConfigurationMissing, this::updateConfiguration);
    }

    private Boolean isConfigurationMissing() {
        return !configAccessor.doesConfigurationExist();
    }

    private EnvironmentProcessingResult updateConfiguration() {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(EMAIL_CONFIGURATION_KEYSET)
            .addVariableNames(OLD_ADDITIONAL_PROPERTY_KEYSET);
        EmailGlobalConfigModel configModel = new EmailGlobalConfigModel();
        configModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        configureEmailSettings(configModel);
        configureAdditionalProperties(builder, configModel);

        ValidationResponseModel validationResponseModel = validator.validate(configModel);
        if (validationResponseModel.hasErrors()) {
            logger.error("Error inserting startup values: {}", validationResponseModel.getMessage());
            Map<String, AlertFieldStatus> errors = validationResponseModel.getErrors();
            for (Map.Entry<String, AlertFieldStatus> error : errors.entrySet()) {
                AlertFieldStatus status = error.getValue();
                logger.error("Field: '{}' failed with the error: {}", status.getFieldName(), status.getFieldMessage());
            }
            return EnvironmentProcessingResult.empty();
        }

        EmailGlobalConfigModel obfuscatedModel = configModel.obfuscate();

        obfuscatedModel.getSmtpHost().ifPresent(value -> builder.addVariableValue(EMAIL_HOST_KEY, value));
        obfuscatedModel.getSmtpPort().map(String::valueOf).ifPresent(value -> builder.addVariableValue(EMAIL_PORT_KEY, value));
        obfuscatedModel.getSmtpFrom().ifPresent(value -> builder.addVariableValue(EMAIL_FROM_KEY, value));
        obfuscatedModel.getSmtpAuth().map(String::valueOf).ifPresent(value -> builder.addVariableValue(AUTH_REQUIRED_KEY, value));
        obfuscatedModel.getSmtpUsername().ifPresent(value -> builder.addVariableValue(AUTH_USER_KEY, value));

        if (Boolean.TRUE.equals(obfuscatedModel.getIsSmtpPasswordSet())) {
            builder.addVariableValue(AUTH_PASSWORD_KEY, AlertConstants.MASKED_VALUE);
        }

        EnvironmentProcessingResult result = builder.build();
        if (result.hasValues()) {
            try {
                configAccessor.createConfiguration(configModel);
            } catch (AlertConfigurationException ex) {
                logger.error("Failed to create config: ", ex);
            }
        }

        return result;
    }

    private void configureEmailSettings(EmailGlobalConfigModel configuration) {
        environmentVariableUtility.getEnvironmentValue(EMAIL_HOST_KEY)
            .ifPresent(configuration::setSmtpHost);

        environmentVariableUtility.getEnvironmentValue(EMAIL_PORT_KEY)
            .filter(NumberUtils::isDigits)
            .map(NumberUtils::toInt)
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

    private void configureAdditionalProperties(EnvironmentProcessingResult.Builder builder, EmailGlobalConfigModel configuration) {
        Map<String, String> additionalProperties = new HashMap<>();
        for (String additionalPropertyName : OLD_ADDITIONAL_PROPERTY_KEYSET) {
            if (environmentVariableUtility.hasEnvironmentValue(additionalPropertyName)) {
                String javamailPropertyName = EmailEnvironmentVariableHandlerFactory.convertVariableNameToJavamailPropertyKey(additionalPropertyName);
                String value = environmentVariableUtility.getEnvironmentValue(additionalPropertyName).orElse(null);
                additionalProperties.put(javamailPropertyName, value);
                builder.addVariableValue(additionalPropertyName, value);
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
