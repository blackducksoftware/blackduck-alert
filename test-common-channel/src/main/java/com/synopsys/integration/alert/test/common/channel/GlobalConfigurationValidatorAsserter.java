/*
 * test-common-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

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

    private Set<AlertFieldStatus> runValidation() {
        FieldModel fieldModel = new FieldModel(descriptorKey, ConfigContextEnum.GLOBAL.name(), defaultKeyToValues);
        return globalConfigurationValidator.validate(fieldModel);
    }

}
