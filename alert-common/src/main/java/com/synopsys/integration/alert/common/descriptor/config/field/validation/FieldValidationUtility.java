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
package com.synopsys.integration.alert.common.descriptor.config.field.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class FieldValidationUtility {
    private final Logger logger = LoggerFactory.getLogger(FieldValidationUtility.class);

    public List<AlertFieldStatus> validateConfig(Map<String, ConfigField> descriptorFields, FieldModel fieldModel) {
        logger.debug("Begin validating fields in configuration field model.");
        List<AlertFieldStatus> fieldStatuses = new ArrayList<>();
        for (ConfigField field : descriptorFields.values()) {
            List<AlertFieldStatus> fieldValidationResults = validateConfigField(field, fieldModel, descriptorFields);
            fieldStatuses.addAll(fieldValidationResults);
        }
        logger.debug("Finished validating fields in configuration field model.");
        return fieldStatuses;
    }

    private List<AlertFieldStatus> validateConfigField(ConfigField fieldToValidate, FieldModel fieldModel, Map<String, ConfigField> descriptorFields) {
        List<AlertFieldStatus> fieldStatuses = new ArrayList<>();
        String key = fieldToValidate.getKey();
        logger.debug("Validating descriptor field: {}", key);
        Optional<FieldValueModel> optionalFieldValue = fieldModel.getFieldValueModel(key);
        if (optionalFieldValue.isPresent()) {
            // field is present now validate the field
            logger.debug("FieldModel contains '{}'", key);
            FieldValueModel fieldValueModel = optionalFieldValue.get();
            if (hasValueOrChecked(fieldValueModel, fieldToValidate.getType())) {
                checkRelatedFields(fieldToValidate, descriptorFields, fieldModel, fieldStatuses);
            }

            ValidationResult validationResult = fieldToValidate.validate(fieldValueModel, fieldModel);
            logger.debug("Validating '{}' errors: {}", key, validationResult);
            if (validationResult.hasErrors()) {
                fieldStatuses.add(AlertFieldStatus.error(key, validationResult.combineErrorMessages()));
            }
            if (validationResult.hasWarnings()) {
                fieldStatuses.add(AlertFieldStatus.warning(key, validationResult.combineWarningMessages()));
            }
        } else if (fieldToValidate.isRequired()) {
            logger.debug("Descriptor field {} is required and missing.", key);
            fieldStatuses.add(AlertFieldStatus.error(key, ConfigField.REQUIRED_FIELD_MISSING));
        }
        return fieldStatuses;
    }

    private void checkRelatedFields(ConfigField field, Map<String, ConfigField> descriptorFields, FieldModel fieldModel, List<AlertFieldStatus> fieldErrors) {
        logger.debug("Begin validating related fields for field: '{}'", field.getKey());
        for (String relatedFieldKey : field.getRequiredRelatedFields()) {
            ConfigField relatedField = descriptorFields.get(relatedFieldKey);
            validateAnyRelatedFieldsMissing(relatedField, fieldModel, fieldErrors);
        }
        for (String disallowedRelatedFieldKey : field.getDisallowedRelatedFields()) {
            ConfigField relatedField = descriptorFields.get(disallowedRelatedFieldKey);
            validateAnyDisallowedFieldsSet(relatedField, fieldModel, fieldErrors, field.getLabel());
        }
        logger.debug("Finished validating related fields for field: '{}'", field.getKey());
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

    private void validateAnyRelatedFieldsMissing(ConfigField field, FieldModel fieldModel, List<AlertFieldStatus> fieldErrors) {
        String key = field.getKey();
        Optional<FieldValueModel> optionalFieldValue = fieldModel.getFieldValueModel(key);
        if (optionalFieldValue.isEmpty() || optionalFieldValue.map(fieldValueModel -> !hasValueOrIsCheckbox(fieldValueModel, field.getType())).orElse(true)) {
            String missingFieldKey = field.getKey();
            logger.debug("Validating '{}': Missing related field '{}'", key, missingFieldKey);
            fieldErrors.add(AlertFieldStatus.error(key, field.getLabel() + " is missing"));
        }
    }

    private void validateAnyDisallowedFieldsSet(ConfigField field, FieldModel fieldModel, List<AlertFieldStatus> fieldErrors, String validatedFieldLabel) {
        String key = field.getKey();
        Optional<FieldValueModel> optionalFieldValue = fieldModel.getFieldValueModel(key);
        if (optionalFieldValue.isPresent() && hasValueOrChecked(optionalFieldValue.get(), field.getType())) {
            String errorMessage = String.format("%s cannot be set if %s is already set", field.getLabel(), validatedFieldLabel);
            String missingFieldKey = field.getKey();
            logger.debug("Validating '{}': Disallowed field '{}' cannot be set.", key, missingFieldKey);
            fieldErrors.add(AlertFieldStatus.error(key, errorMessage));
        }
    }

    private boolean isCheckbox(String type) {
        return FieldType.CHECKBOX_INPUT.getFieldTypeName().equals(type) || FieldType.HIDE_CHECKBOX_INPUT.getFieldTypeName().equals(type);
    }

}
