/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import com.synopsys.integration.alert.common.action.api.GlobalFieldModelToConcreteConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalFieldModelConverter implements GlobalFieldModelToConcreteConverter<EmailGlobalConfigModel> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static final String EMAIL_FIELD_PREFIX = "mail.smtp.";
    public static final String AUTH_REQUIRED_KEY = EMAIL_FIELD_PREFIX + "auth";
    public static final String EMAIL_FROM_KEY = EMAIL_FIELD_PREFIX + "from";
    public static final String EMAIL_HOST_KEY = EMAIL_FIELD_PREFIX + "host";
    public static final String AUTH_PASSWORD_KEY = EMAIL_FIELD_PREFIX + "password";
    public static final String EMAIL_PORT_KEY = EMAIL_FIELD_PREFIX + "port";
    public static final String AUTH_USER_KEY = EMAIL_FIELD_PREFIX + "user";

    private static final Set<String> RESERVED_PROPERTY_KEYS = Set.of(
        AUTH_PASSWORD_KEY,
        AUTH_REQUIRED_KEY,
        AUTH_USER_KEY,
        EMAIL_FROM_KEY,
        EMAIL_HOST_KEY,
        EMAIL_PORT_KEY);

    private Predicate<Map.Entry<String, FieldValueModel>> validAdditionalPropertiesKeyTest;

    public EmailGlobalFieldModelConverter() {
        validAdditionalPropertiesKeyTest = entry -> !RESERVED_PROPERTY_KEYS.contains(entry.getKey());
        validAdditionalPropertiesKeyTest = validAdditionalPropertiesKeyTest.and(entry -> entry.getKey().startsWith(EMAIL_FIELD_PREFIX));
    }

    @Override
    public Optional<EmailGlobalConfigModel> convert(FieldModel globalFieldModel) {
        Optional<EmailGlobalConfigModel> convertedModel = Optional.empty();
        Map<String, FieldValueModel> keyToValues = globalFieldModel.getKeyToValues();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        try {
            globalFieldModel.getFieldValue(EMAIL_FROM_KEY).ifPresent(model::setSmtpFrom);
            globalFieldModel.getFieldValue(EMAIL_HOST_KEY).ifPresent(model::setSmtpHost);
            globalFieldModel.getFieldValue(EMAIL_PORT_KEY)
                .map(Integer::valueOf)
                .ifPresent(model::setSmtpPort);
            globalFieldModel.getFieldValue(AUTH_REQUIRED_KEY)
                .map(Boolean::valueOf)
                .ifPresent(model::setSmtpAuth);
            globalFieldModel.getFieldValue(AUTH_USER_KEY)
                .ifPresent(model::setSmtpUsername);
            globalFieldModel.getFieldValueModel(AUTH_PASSWORD_KEY)
                .filter(Predicate.not(FieldValueModel::getIsSet))
                .flatMap(FieldValueModel::getValue)
                .ifPresent(model::setSmtpPassword);
            model.setAdditionalJavaMailProperties(createAdditionalProperties(globalFieldModel, keyToValues));
            convertedModel = Optional.of(model);
        } catch (NumberFormatException ex) {
            logger.error("Error converting field model to concrete email configuration", ex);
        }
        return convertedModel;
    }

    private Map<String, String> createAdditionalProperties(FieldModel globalFieldModel, Map<String, FieldValueModel> keyToValue) {
        Map<String, String> additionalPropertiesMap = new HashMap<>();
        keyToValue.entrySet().stream()
            .filter(validAdditionalPropertiesKeyTest)
            .forEach(entry -> additionalPropertiesMap.computeIfAbsent(entry.getKey(), (key) -> globalFieldModel.getFieldValue(key).orElse(StringUtils.EMPTY)));
        return additionalPropertiesMap;
    }
}
