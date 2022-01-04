/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.validator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;

public class ConfigurationFieldValidator {
    public static final String REQUIRED_FIELD_MISSING_MESSAGE = "Required field missing";
    public static final String INVALID_OPTION_MESSAGE = "Invalid option selected";
    public static final String NOT_AN_INTEGER_VALUE = "Not an integer value";
    public static final String ENCRYPTION_MISSING = "Encryption configuration missing.";

    private final Map<String, FieldValueModel> fieldMap;
    private final Set<AlertFieldStatus> statuses;

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
        statuses = new HashSet<>();
    }

    public Set<AlertFieldStatus> getValidationResults() {
        return statuses;
    }

    public void validateRequiredFieldIsNotBlank(String fieldKey) {
        if (fieldContainsNoData(fieldKey)) {
            statuses.add(AlertFieldStatus.error(fieldKey, REQUIRED_FIELD_MISSING_MESSAGE));
        }
    }

    public void validateRequiredFieldsAreNotBlank(String... fieldKeys) {
        if (null != fieldKeys) {
            validateRequiredFieldsAreNotBlank(Arrays.asList(fieldKeys));
        }
    }

    public void validateRequiredFieldsAreNotBlank(List<String> fieldKeys) {
        for (String fieldKey : fieldKeys) {
            validateRequiredFieldIsNotBlank(fieldKey);
        }
    }

    public void validateRequiredRelatedSet(String fieldKey, String fieldLabel, String... requiredRelatedFieldKeys) {
        if (fieldContainsData(fieldKey)) {
            for (String requiredFieldKey : requiredRelatedFieldKeys) {
                if (fieldContainsNoData(requiredFieldKey)) {
                    statuses.add(AlertFieldStatus.error(requiredFieldKey, String.format("Must be set if %s is set", fieldLabel)));
                }
            }
        }
    }

    public void validateIsANumber(String fieldKey) {
        boolean isANumberOrEmpty = getStringValue(fieldKey)
            .map(NumberUtils::isCreatable)
            .orElse(true);

        if (!isANumberOrEmpty) {
            statuses.add(AlertFieldStatus.error(fieldKey, NOT_AN_INTEGER_VALUE));
        }
    }

    public void validateIsAURL(String fieldKey) {
        String url = getStringValue(fieldKey).orElse("");
        if (StringUtils.isNotBlank(url)) {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                statuses.add(AlertFieldStatus.error(fieldKey, e.getMessage()));
            }
        }
    }

    public void validateIsAValidOption(String fieldKey, Set<String> validOptions) {
        boolean allValuesInValidOptions = getCollectionOfValues(fieldKey)
            .orElse(Collections.emptySet())
            .stream()
            .map(StringUtils::trimToEmpty)
            .allMatch(option -> validOptions.stream().anyMatch(validOption -> StringUtils.equalsIgnoreCase(option, validOption)));

        if (!allValuesInValidOptions) {
            statuses.add(AlertFieldStatus.error(fieldKey, INVALID_OPTION_MESSAGE));
        }
    }

    public void addValidationResults(AlertFieldStatus... alertFieldStatuses) {
        statuses.addAll(Arrays.asList(alertFieldStatuses));
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
