/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.web.config;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class FieldValidationAction {

    public void validateConfig(Map<String, ConfigField> descriptorFields, FieldModel fieldModel, Map<String, String> fieldErrors) {
        for (Map.Entry<String, ConfigField> fieldEntry : descriptorFields.entrySet()) {
            String key = fieldEntry.getKey();
            ConfigField field = fieldEntry.getValue();
            Optional<FieldValueModel> optionalFieldValue = fieldModel.getFieldValueModel(key);
            if (field.isRequired() && optionalFieldValue.isEmpty()) {
                fieldErrors.put(key, ConfigField.REQUIRED_FIELD_MISSING);
            }

            if (!fieldErrors.containsKey(key) && optionalFieldValue.isPresent()) {
                // field is present now validate the field
                FieldValueModel fieldValueModel = optionalFieldValue.get();
                if (hasValueOrChecked(fieldValueModel, field.getType())) {
                    checkRelatedFields(field, descriptorFields, fieldModel, fieldErrors);
                }
                Collection<String> validationErrors = field.validate(fieldValueModel, fieldModel);
                if (!validationErrors.isEmpty()) {
                    fieldErrors.put(key, StringUtils.join(validationErrors, ", "));
                }
            }
        }
    }

    private void checkRelatedFields(ConfigField field, Map<String, ConfigField> descriptorFields, FieldModel fieldModel, Map<String, String> fieldErrors) {
        for (String relatedFieldKey : field.getRequiredRelatedFields()) {
            ConfigField relatedField = descriptorFields.get(relatedFieldKey);
            validateAnyRelatedFieldsMissing(relatedField, fieldModel, fieldErrors);
        }
        for (String disallowedRelatedFieldKey : field.getDisallowedRelatedFields()) {
            ConfigField relatedField = descriptorFields.get(disallowedRelatedFieldKey);
            validateAnyDisallowedFieldsSet(relatedField, fieldModel, fieldErrors, field.getLabel());
        }
    }

    private boolean hasValueOrChecked(FieldValueModel fieldValueModel, String type) {
        boolean isValueTrue = fieldValueModel.getValue().map(Boolean::parseBoolean).orElse(false);
        boolean isCheckbox = isCheckbox(type);
        return (isValueTrue && isCheckbox) || (!fieldValueModel.containsNoData() && !isCheckbox);
    }

    private Boolean hasValueOrIsCheckbox(FieldValueModel fieldValueModel, String type) {
        boolean isCheckbox = isCheckbox(type);
        return isCheckbox || !fieldValueModel.containsNoData();
    }

    private void validateAnyRelatedFieldsMissing(ConfigField field, FieldModel fieldModel, Map<String, String> fieldErrors) {
        String key = field.getKey();
        Optional<FieldValueModel> optionalFieldValue = fieldModel.getFieldValueModel(key);
        if (optionalFieldValue.isEmpty() || optionalFieldValue.map(fieldValueModel -> !hasValueOrIsCheckbox(fieldValueModel, field.getType())).orElse(true)) {
            fieldErrors.put(key, field.getLabel() + " is missing");
        }
    }

    private void validateAnyDisallowedFieldsSet(ConfigField field, FieldModel fieldModel, Map<String, String> fieldErrors, String validatedFieldLabel) {
        String key = field.getKey();
        Optional<FieldValueModel> optionalFieldValue = fieldModel.getFieldValueModel(key);
        if (optionalFieldValue.isPresent() && hasValueOrChecked(optionalFieldValue.get(), field.getType())) {
            String errorMessage = String.format("%s cannot be set if %s is already set", field.getLabel(), validatedFieldLabel);
            fieldErrors.put(key, errorMessage);
        }
    }

    private boolean isCheckbox(String type) {
        return FieldType.CHECKBOX_INPUT.getFieldTypeName().equals(type) || FieldType.HIDE_CHECKBOX_INPUT.getFieldTypeName().equals(type);
    }
}
