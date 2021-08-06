/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.validator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;

public class ConfigurationFieldValidator {
    public static final String REQUIRED_FIELD_MISSING_MESSAGE = "Required field missing";
    public static final String INVALID_OPTION_MESSAGE = "Invalid option selected";

    private final Map<String, FieldValueModel> fieldMap;

    public static ConfigurationFieldValidator fromFieldModel(FieldModel fieldModel) {
        return new ConfigurationFieldValidator(fieldModel.getKeyToValues());
    }

    public static ConfigurationFieldValidator fromJobFieldModel(JobFieldModel jobFieldModel) {
        Map<String, FieldValueModel> mapToValidate = jobFieldModel.getFieldModels()
            .stream()
            .map(FieldModel::getKeyToValues)
            .map(Map::entrySet)
            .flatMap(Set::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (old, newIgnored) -> old)); // Merge operation is equivalent to Map::putIfAbsent --rotte
        return new ConfigurationFieldValidator(mapToValidate);
    }

    public ConfigurationFieldValidator(Map<String, FieldValueModel> mapToValidate) {
        this.fieldMap = mapToValidate;
    }

    public Optional<AlertFieldStatus> validateRequiredFieldIsNotBlank(String fieldKey) {
        if (fieldContainsNoData(fieldKey)) {
            AlertFieldStatus error = AlertFieldStatus.error(fieldKey, REQUIRED_FIELD_MISSING_MESSAGE);
            return Optional.of(error);
        }
        return Optional.empty();
    }

    public List<AlertFieldStatus> validateRequiredFieldsAreNotBlank(String... fieldKeys) {
        if (null != fieldKeys) {
            return validateRequiredFieldsAreNotBlank(Arrays.asList(fieldKeys));
        }
        return List.of();
    }

    public List<AlertFieldStatus> validateRequiredFieldsAreNotBlank(List<String> fieldKeys) {
        List<AlertFieldStatus> errors = new LinkedList<>();
        for (String fieldKey : fieldKeys) {
            validateRequiredFieldIsNotBlank(fieldKey)
                .ifPresent(errors::add);
        }
        return errors;
    }

    public List<AlertFieldStatus> validateRequiredRelatedSet(String fieldKey, String fieldLabel, String... requiredRelatedFieldKeys) {
        List<AlertFieldStatus> errors = new LinkedList<>();
        if (fieldContainsData(fieldKey)) {
            for (String requiredFieldKey : requiredRelatedFieldKeys) {
                if (fieldContainsNoData(requiredFieldKey)) {
                    AlertFieldStatus error = AlertFieldStatus.error(requiredFieldKey, String.format("Must be set if %s is set", fieldLabel));
                    errors.add(error);
                }
            }
        }
        return errors;
    }

    public Optional<AlertFieldStatus> validateIsANumber(String fieldKey) {
        boolean isANumberOrEmpty = getStringValue(fieldKey)
            .map(NumberUtils::isCreatable)
            .orElse(true);

        if (!isANumberOrEmpty) {
            AlertFieldStatus error = AlertFieldStatus.error(fieldKey, NumberConfigField.NOT_AN_INTEGER_VALUE);
            return Optional.of(error);
        }
        return Optional.empty();
    }

    public Optional<AlertFieldStatus> validateIsAURL(String fieldKey) {
        String url = getStringValue(fieldKey).orElse("");
        if (StringUtils.isNotBlank(url)) {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                AlertFieldStatus error = AlertFieldStatus.error(fieldKey, e.getMessage());
                return Optional.of(error);
            }
        }
        return Optional.empty();
    }

    public Optional<AlertFieldStatus> validateIsAValidOption(String fieldKey, Set<String> validOptions) {
        boolean allValuesInValidOptions = getCollectionOfValues(fieldKey)
            .orElse(Collections.emptySet())
            .stream()
            .map(StringUtils::trimToEmpty)
            .allMatch(option -> validOptions.stream().anyMatch(validOption -> StringUtils.equalsIgnoreCase(option, validOption)));

        if (!allValuesInValidOptions) {
            AlertFieldStatus error = AlertFieldStatus.error(fieldKey, INVALID_OPTION_MESSAGE);
            return Optional.of(error);
        }
        return Optional.empty();
    }

    public boolean fieldContainsData(String fieldKey) {
        return !fieldContainsNoData(fieldKey);
    }

    public boolean fieldContainsNoData(String fieldKey) {
        return getFieldValues(fieldKey)
            .map(FieldValueModel::containsNoData)
            .orElse(true);
    }

    public boolean fieldHasNoReadableValue(String fieldKey) {
        return !fieldHasReadableValue(fieldKey);
    }

    public boolean fieldHasReadableValue(String fieldKey) {
        return getFieldValues(fieldKey)
            .map(FieldValueModel::hasValues)
            .orElse(false);
    }

    public Optional<FieldValueModel> getFieldValues(String fieldKey) {
        return Optional.ofNullable(fieldMap.get(fieldKey));
    }

    public Optional<String> getStringValue(String fieldKey) {
        return getFieldValues(fieldKey).flatMap(FieldValueModel::getValue);
    }

    public Optional<Boolean> getBooleanValue(String fieldKey) {
        return getFieldValues(fieldKey).flatMap(FieldValueModel::getValue).map(Boolean::valueOf);
    }

    public Optional<Collection<String>> getCollectionOfValues(String fieldKey) {
        return getFieldValues(fieldKey)
            .map(FieldValueModel::getValues);
    }

    public Optional<Long> getLongValue(String fieldKey) {
        return getFieldValues(fieldKey).flatMap(FieldValueModel::getValue).map(Long::valueOf);
    }

}
