/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.test.common.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import org.junit.jupiter.api.Assertions;

public class GlobalConfigurationValidatorAsserter {
    private final String descriptorKey;
    private final GlobalConfigurationFieldModelValidator globalConfigurationValidator;
    private final Map<String, FieldValueModel> defaultKeyToValues;

    public GlobalConfigurationValidatorAsserter(String descriptorKey, GlobalConfigurationFieldModelValidator globalConfigurationValidator, Map<String, FieldValueModel> defaultKeyToValues) {
        this.descriptorKey = descriptorKey;
        this.globalConfigurationValidator = globalConfigurationValidator;
        this.defaultKeyToValues = new HashMap<>(defaultKeyToValues);
    }

    public void assertInvalidValue(String key, String invalidValue) {
        assertInvalidValue(key, invalidValue, value -> {});
    }

    public void assertInvalidValue(String key, String invalidValue, Consumer<AlertFieldStatus> additionalAsserts) {
        FieldValueModel apiKeyFieldValueModel = new FieldValueModel(List.of(invalidValue), true);
        defaultKeyToValues.put(key, apiKeyFieldValueModel);

        Set<AlertFieldStatus> alertFieldStatuses = runValidation();

        assertEquals(1, alertFieldStatuses.size(), alertFieldStatuses.toString());
        AlertFieldStatus alertFieldStatus = alertFieldStatuses.stream().findFirst().orElseThrow();

        assertEquals(key, alertFieldStatus.getFieldName());
        additionalAsserts.accept(alertFieldStatus);
    }

    public void assertMissingValue(String key) {
        defaultKeyToValues.remove(key);
        Set<AlertFieldStatus> alertFieldStatuses = runValidation();

        assertEquals(1, alertFieldStatuses.size(), alertFieldStatuses.toString());
        AlertFieldStatus alertFieldStatus = alertFieldStatuses.stream().findFirst().orElseThrow();
        assertEquals(key, alertFieldStatus.getFieldName());
    }

    public void assertCustom(Consumer<Set<AlertFieldStatus>> additionalAsserts) {
        Set<AlertFieldStatus> alertFieldStatuses = runValidation();
        additionalAsserts.accept(alertFieldStatuses);
    }

    public void assertValid() {
        Set<AlertFieldStatus> fieldStatuses = runValidation();
        assertEquals(0, fieldStatuses.size(), fieldStatuses.toString());
    }

    public void assertExceptionThrown(Class<? extends Exception> expectedExceptionClass, String key, String invalidValue) {
        FieldValueModel apiKeyFieldValueModel = new FieldValueModel(List.of(invalidValue), true);
        defaultKeyToValues.put(key, apiKeyFieldValueModel);
        Assertions.assertThrows(expectedExceptionClass, this::runValidation);
    }

    private Set<AlertFieldStatus> runValidation() {
        FieldModel fieldModel = new FieldModel(descriptorKey, ConfigContextEnum.GLOBAL.name(), defaultKeyToValues);
        return globalConfigurationValidator.validate(fieldModel);
    }

}
