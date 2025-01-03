/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.google.gson.Gson;

@Component
public class JiraFieldMappingValidator {
    private final Gson gson;

    @Autowired
    public JiraFieldMappingValidator(Gson gson) {
        this.gson = gson;
    }

    public Optional<AlertFieldStatus> validateFieldMappings(String fieldMappingFieldKey, FieldValueModel fieldMappingField) {
        List<JiraJobCustomFieldModel> customFields = fieldMappingField.getValues()
            .stream()
            .map(fieldMappingString -> gson.fromJson(fieldMappingString, JiraJobCustomFieldModel.class))
            .collect(Collectors.toList());

        Set<String> fieldNames = new HashSet<>();
        List<String> duplicateNameList = new ArrayList<>();

        for (JiraJobCustomFieldModel jiraJobCustomFieldModel : customFields) {
            String currentFieldName = jiraJobCustomFieldModel.getFieldName();
            if (fieldNames.contains(currentFieldName)) {
                duplicateNameList.add(currentFieldName);
            }
            fieldNames.add(currentFieldName);
        }

        if (!duplicateNameList.isEmpty()) {
            String duplicateNames = StringUtils.join(duplicateNameList, ", ");
            String errorMessage = String.format("Duplicate field name(s): %s", duplicateNames);
            AlertFieldStatus fieldMappingError = AlertFieldStatus.error(fieldMappingFieldKey, errorMessage);
            return Optional.of(fieldMappingError);
        }

        return Optional.empty();
    }

}
