/*
 * channel-jira-server
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.jira.JiraConstants;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.FieldMappingEndpointField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class JiraServerDistributionUIConfig extends ChannelDistributionUIConfig {
    private static final String LABEL_ADD_COMMENTS = "Add comments";
    private static final String LABEL_ISSUE_CREATOR = "Issue Creator";
    private static final String LABEL_JIRA_PROJECT = "Jira Project";
    private static final String LABEL_ISSUE_TYPE = "Issue Type";
    private static final String LABEL_RESOLVE_WORKFLOW_TRANSITION = "Resolve Transition";
    private static final String LABEL_OPEN_WORKFLOW_TRANSITION = "Re-open Transition";
    public static final String LABEL_FIELD_MAPPING = "Field Mapping";

    private static final String DESCRIPTION_ADD_COMMENTS = "If true, this will add comments to the Jira ticket with data describing the latest change.";
    private static final String DESCRIPTION_ISSUE_CREATOR = "The username of the Jira Server user to assign as the issue creator field of the Jira issue.";
    private static final String DESCRIPTION_JIRA_PROJECT = "The name or key of the Jira Project for which this job creates and/or updates Jira tickets.";
    private static final String DESCRIPTION_ISSUE_TYPE = "The issue type to open when creating an issue in Jira Server.";
    private static final String DESCRIPTION_RESOLVE_WORKFLOW_TRANSITION = "If a transition is listed (case sensitive), it will be used when resolving an issue. This will happen when Alert receives a DELETE operation from a provider. "
                                                                              + "Note: This must be in the 'Done' status category.";
    private static final String DESCRIPTION_OPEN_WORKFLOW_TRANSITION = "If a transition is listed (case sensitive), it will be used when re-opening an issue. This will happen when Alert receives an ADD/UPDATE operation from a provider. "
                                                                           + "Note: This must be in the 'To Do' status category.";
    public static final String DESCRIPTION_FIELD_MAPPING = "Use this field to provide static values to Jira fields or map them to information from the notifications.";

    private final Gson gson;

    @Autowired
    public JiraServerDistributionUIConfig(Gson gson) {
        super(ChannelKeys.JIRA_SERVER, JiraServerDescriptor.JIRA_LABEL, JiraServerDescriptor.JIRA_URL);
        this.gson = gson;
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        ConfigField addComments = new CheckboxConfigField(JiraServerDescriptor.KEY_ADD_COMMENTS, LABEL_ADD_COMMENTS, DESCRIPTION_ADD_COMMENTS);
        ConfigField issueCreator = new TextInputConfigField(JiraServerDescriptor.KEY_ISSUE_CREATOR, LABEL_ISSUE_CREATOR, DESCRIPTION_ISSUE_CREATOR);
        ConfigField jiraProjectName = new TextInputConfigField(JiraServerDescriptor.KEY_JIRA_PROJECT_NAME, LABEL_JIRA_PROJECT, DESCRIPTION_JIRA_PROJECT).applyRequired(true);

        ConfigField issueType = new TextInputConfigField(JiraServerDescriptor.KEY_ISSUE_TYPE, LABEL_ISSUE_TYPE, DESCRIPTION_ISSUE_TYPE).applyRequired(true).applyDefaultValue(JiraConstants.DEFAULT_ISSUE_TYPE);
        ConfigField resolveWorkflow = new TextInputConfigField(JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION, LABEL_RESOLVE_WORKFLOW_TRANSITION, DESCRIPTION_RESOLVE_WORKFLOW_TRANSITION);
        ConfigField openWorkflow = new TextInputConfigField(JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, LABEL_OPEN_WORKFLOW_TRANSITION, DESCRIPTION_OPEN_WORKFLOW_TRANSITION)
                                       .applyRequiredRelatedField(resolveWorkflow.getKey());
        ConfigField fieldMapping = new FieldMappingEndpointField(JiraServerDescriptor.KEY_FIELD_MAPPING, LABEL_FIELD_MAPPING, DESCRIPTION_FIELD_MAPPING, "Jira Field", "Value")
                                       .applyNewMappingTitle("Create Jira Field Mapping")
                                       .applyValidationFunctions(this::validateFieldMapping)
                                       .applyPanel("Advanced Jira Configuration");

        return List.of(addComments, issueCreator, jiraProjectName, issueType, resolveWorkflow, openWorkflow, fieldMapping);
    }

    private ValidationResult validateFieldMapping(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        Collection<String> fieldMappingStrings = fieldToValidate.getValues();
        List<JiraJobCustomFieldModel> customFields = fieldMappingStrings.stream()
                                                         .map(fieldMappingString -> gson.fromJson(fieldMappingString, JiraJobCustomFieldModel.class))
                                                         .collect(Collectors.toList());
        Set<String> fieldNames = new HashSet();
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
            String error = String.format("Duplicate field name(s): %s", duplicateNames);
            return ValidationResult.errors(error);
        }
        return ValidationResult.success();
    }

}
