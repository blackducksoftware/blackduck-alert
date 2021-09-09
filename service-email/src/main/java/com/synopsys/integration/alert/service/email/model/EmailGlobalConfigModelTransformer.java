/*
 * service-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email.model;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;

// TODO: Remove this when we no longer support FieldModels.
@Component
public class EmailGlobalConfigModelTransformer {
    private final ConfigurationFieldModelConverter configurationFieldModelConverter;

    @Autowired
    public EmailGlobalConfigModelTransformer(ConfigurationFieldModelConverter configurationFieldModelConverter) {
        this.configurationFieldModelConverter = configurationFieldModelConverter;
    }

    public EmailGlobalConfigModel fromFieldUtility(FieldUtility fieldUtility){
        EmailGlobalConfigModel concreteModel = new EmailGlobalConfigModel();

        concreteModel.setFrom(fieldUtility.getStringOrEmpty(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey()));
        concreteModel.setHost(fieldUtility.getStringOrEmpty(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey()));
        concreteModel.setPort(fieldUtility.getInteger(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()).orElse(0));

        concreteModel.setAuth(fieldUtility.getBooleanOrFalse(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey()));
        concreteModel.setUsername(fieldUtility.getStringOrEmpty(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey()));
        concreteModel.setPassword(fieldUtility.getStringOrEmpty(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey()));

        Map<String, String> additionalJavamailProperties = fieldUtility.getFields()
            .entrySet()
            .stream()
            .filter(Predicate.not(e -> EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey().equals(e.getKey())))
            .filter(Predicate.not(e -> EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey().equals(e.getKey())))
            .filter(Predicate.not(e -> EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey().equals(e.getKey())))
            .filter(Predicate.not(e -> EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey().equals(e.getKey())))
            .filter(Predicate.not(e -> EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey().equals(e.getKey())))
            .filter(Predicate.not(e -> EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey().equals(e.getKey())))
            .filter(e -> e.getValue().getFieldValue().filter(StringUtils::isNotBlank).isPresent())
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFieldValue().get()));

        concreteModel.setAdditionalJavaMailProperties(additionalJavamailProperties);

        return concreteModel;
    }

    public EmailGlobalConfigModel fromConfigurationModel(ConfigurationModel configurationModel){
        return fromFieldUtility(new FieldUtility(configurationModel.getCopyOfKeyToFieldMap()));
    }

    public EmailGlobalConfigModel fromFieldModel(FieldModel fieldModel){
        return fromFieldUtility(configurationFieldModelConverter.convertToFieldAccessor(fieldModel));
    }

}
