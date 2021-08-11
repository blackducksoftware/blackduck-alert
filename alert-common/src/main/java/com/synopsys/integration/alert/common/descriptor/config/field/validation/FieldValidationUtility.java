/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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

    private boolean isCheckbox(String type) {
        return FieldType.CHECKBOX_INPUT.getFieldTypeName().equals(type) || FieldType.HIDE_CHECKBOX_INPUT.getFieldTypeName().equals(type);
    }

}
