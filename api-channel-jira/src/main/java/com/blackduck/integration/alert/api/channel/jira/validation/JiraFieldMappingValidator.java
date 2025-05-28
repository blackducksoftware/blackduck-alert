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

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.google.gson.Gson;

@Component
public class JiraFieldMappingValidator {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;

    @Autowired
    public JiraFieldMappingValidator(Gson gson) {
        this.gson = gson;
    }

    public Optional<AlertFieldStatus> validateFieldMappings(String fieldMappingFieldKey, FieldValueModel fieldMappingField) {
        List<JiraJobCustomFieldModel> customFields = fieldMappingField.getValues()
            .stream()
            .map(fieldMappingString -> gson.fromJson(fieldMappingString, JiraJobCustomFieldModel.class))
                .toList();

        Set<String> fieldNames = new HashSet<>();
        List<String> duplicateNameList = new ArrayList<>();
        List<String> invalidJsonFieldNames = new ArrayList<>();

        for (JiraJobCustomFieldModel jiraJobCustomFieldModel : customFields) {
            String currentFieldName = jiraJobCustomFieldModel.getFieldName();
            if (fieldNames.contains(currentFieldName)) {
                duplicateNameList.add(currentFieldName);
            }

            if(jiraJobCustomFieldModel.isTreatValueAsJson()) {
                validateJson(jiraJobCustomFieldModel).ifPresent(invalidJsonFieldNames::add);
            }

            fieldNames.add(currentFieldName);
        }
        String errorMessage = null;
        if (!duplicateNameList.isEmpty()) {
            String duplicateNames = StringUtils.join(duplicateNameList, ", ");
            errorMessage = String.format("Duplicate field name(s): %s", duplicateNames);
        }

        if(!invalidJsonFieldNames.isEmpty()) {
            String jsonErrorFieldNames = StringUtils.join(invalidJsonFieldNames, ", ");
            if(StringUtils.isNotBlank(errorMessage)) {
                errorMessage = String.format("and invalid JSON value field name(s): %s", jsonErrorFieldNames);
            } else {
                errorMessage = String.format("Invalid JSON value field name(s): %s", jsonErrorFieldNames);
            }
        }

        if(StringUtils.isNotBlank(errorMessage)) {
            logger.error("Custom Field Validation error: {}", errorMessage);
            AlertFieldStatus fieldMappingError = AlertFieldStatus.error(fieldMappingFieldKey, errorMessage);
            return Optional.of(fieldMappingError);
        }

        return Optional.empty();
    }

    private Optional<String> validateJson(JiraJobCustomFieldModel jiraJobCustomFieldModel) {
        try{
            String value = jiraJobCustomFieldModel.getFieldValue();
            JsonParser.parseString(value);
            return Optional.empty();
        } catch(JsonSyntaxException ex) {
            return Optional.of(jiraJobCustomFieldModel.getFieldName());
        }
    }

}
