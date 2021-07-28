/*
 * test-common-channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class GlobalConfigurationValidatorAsserter {
    private final String descriptorKey;
    private final GlobalConfigurationValidator globalConfigurationValidator;
    private final Map<String, FieldValueModel> defaultKeyToValues;

    public GlobalConfigurationValidatorAsserter(String descriptorKey, GlobalConfigurationValidator globalConfigurationValidator, Map<String, FieldValueModel> defaultKeyToValues) {
        this.descriptorKey = descriptorKey;
        this.globalConfigurationValidator = globalConfigurationValidator;
        this.defaultKeyToValues = defaultKeyToValues;
    }

    public void assertInvalidValue(String key, String invalidValue) {
        assertInvalidValue(key, invalidValue, (value) -> {});
    }

    public void assertInvalidValue(String key, String invalidValue, Consumer<AlertFieldStatus> additionalAsserts) {
        FieldValueModel apiKeyFieldValueModel = new FieldValueModel(List.of(invalidValue), true);
        defaultKeyToValues.put(key, apiKeyFieldValueModel);

        Set<AlertFieldStatus> alertFieldStatuses = runValidation();

        assertEquals(1, alertFieldStatuses.size());

        AlertFieldStatus alertFieldStatus = alertFieldStatuses.stream().findFirst().orElse(null);
        assertNotNull(alertFieldStatus);
        assertEquals(key, alertFieldStatus.getFieldName());
        additionalAsserts.accept(alertFieldStatus);
    }

    public void assertMissingValue(String key) {
        defaultKeyToValues.remove(key);
        Set<AlertFieldStatus> alertFieldStatuses = runValidation();

        assertEquals(1, alertFieldStatuses.size());

        AlertFieldStatus alertFieldStatus = alertFieldStatuses.stream().findFirst().orElse(null);
        assertNotNull(alertFieldStatus);
        assertEquals(key, alertFieldStatus.getFieldName());
    }

    public void assertValid() {
        Set<AlertFieldStatus> fieldStatuses = runValidation();
        assertEquals(0, fieldStatuses.size());
    }

    private Set<AlertFieldStatus> runValidation() {
        FieldModel fieldModel = new FieldModel(descriptorKey, ConfigContextEnum.GLOBAL.name(), defaultKeyToValues);
        return globalConfigurationValidator.validate(fieldModel);
    }
}
