/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
import java.util.Collection;
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

    public List<AlertFieldStatus> validateConfig(Map<String, ConfigField> fieldKeyToConfigField, FieldModel fieldModel) {
        return validateConfig(fieldKeyToConfigField, List.of(fieldModel));
    }

    public List<AlertFieldStatus> validateConfig(Map<String, ConfigField> fieldKeyToConfigField, Collection<FieldModel> fieldModels) {
        logger.trace("Begin validating fields in configuration field model.");
        List<AlertFieldStatus> fieldStatuses = new ArrayList<>();
        for (ConfigField field : fieldKeyToConfigField.values()) {
            List<AlertFieldStatus> fieldValidationResults = validateConfigField(field, fieldModels, fieldKeyToConfigField);
            fieldStatuses.addAll(fieldValidationResults);
        }
        logger.trace("Finished validating fields in configuration field model.");
        return fieldStatuses;
    }

    public List<AlertFieldStatus> validateConfigField(ConfigField fieldToValidate, Collection<FieldModel> fieldModels, Map<String, ConfigField> fieldKeyToConfigField) {
        List<AlertFieldStatus> fieldStatuses = new ArrayList<>();
        String key = fieldToValidate.getKey();
        logger.trace("Validating descriptor field: {}", key);
        Optional<FieldValueModel> optionalFieldValue = getFieldValue(key, fieldModels);
        if (optionalFieldValue.isPresent()) {
            // field is present now validate the field
            logger.trace("FieldModel contains '{}'", key);
            FieldValueModel fieldValueModel = optionalFieldValue.get();
            if (hasValueOrChecked(fieldValueModel, fieldToValidate.getType())) {
                List<AlertFieldStatus> relatedFieldErrors = validateRelatedFields(fieldToValidate, fieldKeyToConfigField, fieldModels);
                fieldStatuses.addAll(relatedFieldErrors);
            }

            Optional<FieldModel> sourceFieldModel = getSourceFieldModel(key, fieldModels);
            ValidationResult validationResult;
            if (sourceFieldModel.isPresent()) {
                validationResult = fieldToValidate.validate(fieldValueModel, sourceFieldModel.get());
            } else {
                validationResult = ValidationResult.success();
            }

            if (validationResult.hasErrors()) {
                logger.debug("Validating '{}' errors: {}", key, validationResult);
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

    public List<AlertFieldStatus> validateRelatedFields(ConfigField field, Map<String, ConfigField> fieldKeyToConfigField, FieldModel fieldModel) {
        return validateRelatedFields(field, fieldKeyToConfigField, List.of(fieldModel));
    }

    public List<AlertFieldStatus> validateRelatedFields(ConfigField field, Map<String, ConfigField> fieldKeyToConfigField, Collection<FieldModel> fieldModels) {
        List<AlertFieldStatus> relatedFieldErrors = new ArrayList<>();
        logger.trace("Begin validating related fields for field: '{}'", field.getKey());
        for (String relatedFieldKey : field.getRequiredRelatedFields()) {
            ConfigField relatedField = fieldKeyToConfigField.get(relatedFieldKey);
            List<AlertFieldStatus> missingFieldErrors = validateAnyRelatedFieldsMissing(relatedField, fieldModels);
            relatedFieldErrors.addAll(missingFieldErrors);
        }
        for (String disallowedRelatedFieldKey : field.getDisallowedRelatedFields()) {
            ConfigField relatedField = fieldKeyToConfigField.get(disallowedRelatedFieldKey);
            validateDisallowedFieldError(relatedField, fieldModels, field.getLabel())
                .ifPresent(relatedFieldErrors::add);
        }
        logger.trace("Finished validating related fields for field: '{}'", field.getKey());
        return relatedFieldErrors;
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

    private List<AlertFieldStatus> validateAnyRelatedFieldsMissing(ConfigField field, Collection<FieldModel> fieldModels) {
        List<AlertFieldStatus> fieldMissingErrors = new ArrayList<>();
        String key = field.getKey();
        Optional<FieldValueModel> optionalFieldValue = getFieldValue(key, fieldModels);
        if (optionalFieldValue.isEmpty() || optionalFieldValue.map(fieldValueModel -> !hasValueOrIsCheckbox(fieldValueModel, field.getType())).orElse(true)) {
            String missingFieldKey = field.getKey();
            logger.trace("Validating '{}': Missing related field '{}'", key, missingFieldKey);
            fieldMissingErrors.add(AlertFieldStatus.error(key, field.getLabel() + " is missing"));
        }
        return fieldMissingErrors;
    }

    private Optional<AlertFieldStatus> validateDisallowedFieldError(ConfigField field, Collection<FieldModel> fieldModels, String validatedFieldLabel) {
        String key = field.getKey();
        Optional<FieldValueModel> optionalFieldValue = getFieldValue(key, fieldModels);
        if (optionalFieldValue.isPresent() && hasValueOrChecked(optionalFieldValue.get(), field.getType())) {
            String errorMessage = String.format("%s cannot be set if %s is already set", field.getLabel(), validatedFieldLabel);
            String missingFieldKey = field.getKey();
            logger.trace("Validating '{}': Disallowed field '{}' cannot be set.", key, missingFieldKey);
            return Optional.of(AlertFieldStatus.error(key, errorMessage));
        }
        return Optional.empty();
    }

    private Optional<FieldValueModel> getFieldValue(String fieldKey, Collection<FieldModel> fieldModels) {
        return fieldModels
                   .stream()
                   .map(fieldModel -> fieldModel.getFieldValueModel(fieldKey))
                   .flatMap(Optional::stream)
                   .findFirst();
    }

    private Optional<FieldModel> getSourceFieldModel(String fieldKey, Collection<FieldModel> fieldModels) {
        return fieldModels
                   .stream()
                   .filter(fieldModel -> fieldModel.getFieldValueModel(fieldKey).isPresent())
                   .findFirst();
    }

    private boolean isCheckbox(String type) {
        return FieldType.CHECKBOX_INPUT.getFieldTypeName().equals(type) || FieldType.HIDE_CHECKBOX_INPUT.getFieldTypeName().equals(type);
    }

}
