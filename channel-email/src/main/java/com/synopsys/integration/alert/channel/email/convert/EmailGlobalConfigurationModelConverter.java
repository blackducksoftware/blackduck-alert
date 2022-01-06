/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.convert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.api.GlobalConfigurationModelToConcreteConverter;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalConfigurationModelConverter implements GlobalConfigurationModelToConcreteConverter<EmailGlobalConfigModel> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static final String EMAIL_FIELD_PREFIX = "mail.smtp.";

    private static final Set<String> RESERVED_PROPERTY_KEYS = Set.of(
        EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(),
        EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(),
        EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(),
        EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(),
        EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(),
        EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey());

    private Predicate<Map.Entry<String, ConfigurationFieldModel>> validAdditionalPropertiesKeyTest;

    public EmailGlobalConfigurationModelConverter() {
        validAdditionalPropertiesKeyTest = entry -> !RESERVED_PROPERTY_KEYS.contains(entry.getKey());
        validAdditionalPropertiesKeyTest = validAdditionalPropertiesKeyTest.and(entry -> entry.getKey().startsWith(EMAIL_FIELD_PREFIX));
    }

    @Override
    public Optional<EmailGlobalConfigModel> convert(ConfigurationModel globalConfigurationModel) {
        Optional<EmailGlobalConfigModel> convertedModel = Optional.empty();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        try {
            globalConfigurationModel.getField(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey())
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .ifPresent(model::setSmtpFrom);
            globalConfigurationModel.getField(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey())
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .ifPresent(model::setSmtpHost);
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
            convertedModel = Optional.of(model);
        } catch (NumberFormatException ex) {
            logger.error("Error converting field model to concrete email configuration", ex);
        }
        return convertedModel;
    }

    private Map<String, String> createAdditionalProperties(ConfigurationModel globalConfigurationModel) {
        Map<String, String> additionalPropertiesMap = new HashMap<>();
        Map<String, ConfigurationFieldModel> keyToValue = globalConfigurationModel.getCopyOfKeyToFieldMap();
        keyToValue.entrySet().stream()
            .filter(validAdditionalPropertiesKeyTest)
            .forEach(entry -> additionalPropertiesMap.computeIfAbsent(entry.getKey(), (key) -> globalConfigurationModel.getField(key)
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .orElse(StringUtils.EMPTY)));
        return additionalPropertiesMap;
    }
}
