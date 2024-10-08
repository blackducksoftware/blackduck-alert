/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.convert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteConverter;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

/**
 * @deprecated This class is required for converting an old ConfigurationModel into the new GlobalConfigModel classes. This is a temporary class that should be removed once we
 * remove unsupported REST endpoints.
 */
@Component
@Deprecated(forRemoval = true)
public class EmailGlobalConfigurationModelConverter extends GlobalConfigurationModelToConcreteConverter<EmailGlobalConfigModel> {
    private static final String EMAIL_FIELD_PREFIX = "mail.smtp.";

    private static final Set<String> RESERVED_PROPERTY_KEYS = Set.of(
        EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(),
        EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(),
        EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(),
        EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(),
        EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(),
        EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()
    );
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final EmailGlobalConfigurationValidator validator;

    private Predicate<Map.Entry<String, ConfigurationFieldModel>> validAdditionalPropertiesKeyTest;

    @Autowired
    public EmailGlobalConfigurationModelConverter(EmailGlobalConfigurationValidator validator) {
        this.validator = validator;
        validAdditionalPropertiesKeyTest = entry -> !RESERVED_PROPERTY_KEYS.contains(entry.getKey());
        validAdditionalPropertiesKeyTest = validAdditionalPropertiesKeyTest.and(entry -> entry.getKey().startsWith(EMAIL_FIELD_PREFIX));
    }

    @Override
    protected Optional<EmailGlobalConfigModel> convert(ConfigurationModel globalConfigurationModel) {
        String smtpFrom = globalConfigurationModel.getField(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey())
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .orElse(null);
        String smtpHost = globalConfigurationModel.getField(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey())
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .orElse(null);

        if (smtpFrom == null || smtpHost == null) {
            return Optional.empty();
        }

        EmailGlobalConfigModel model = new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, smtpFrom, smtpHost);
        try {
            globalConfigurationModel.getField(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey())
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .map(Integer::valueOf)
                .ifPresent(model::setSmtpPort);
            globalConfigurationModel.getField(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey())
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .map(Boolean::valueOf)
                .ifPresent(model::setSmtpAuth);
            globalConfigurationModel.getField(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey())
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .ifPresent(model::setSmtpUsername);
            globalConfigurationModel.getField(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey())
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .ifPresent(model::setSmtpPassword);
            model.setAdditionalJavaMailProperties(createAdditionalProperties(globalConfigurationModel));
        } catch (NumberFormatException ex) {
            logger.error("Error converting field model to concrete email configuration", ex);
            return Optional.empty();
        }
        return Optional.of(model);
    }

    @Override
    protected ValidationResponseModel validate(EmailGlobalConfigModel configModel, @Nullable String existingConfigurationId) {
        //Since there is only a single email global configuration, existingConfigurationId is ignored.
        return validator.validate(configModel);
    }

    private Map<String, String> createAdditionalProperties(ConfigurationModel globalConfigurationModel) {
        Map<String, String> additionalPropertiesMap = new HashMap<>();
        Map<String, ConfigurationFieldModel> keyToValue = globalConfigurationModel.getCopyOfKeyToFieldMap();
        keyToValue.entrySet().stream()
            .filter(validAdditionalPropertiesKeyTest)
            .forEach(entry -> additionalPropertiesMap.computeIfAbsent(entry.getKey(), key -> globalConfigurationModel.getField(key)
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .orElse(StringUtils.EMPTY)));
        return additionalPropertiesMap;
    }
}
