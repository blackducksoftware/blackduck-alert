/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

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
