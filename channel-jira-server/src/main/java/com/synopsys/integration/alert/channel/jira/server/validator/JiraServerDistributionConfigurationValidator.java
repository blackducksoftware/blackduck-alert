/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.validator;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.CommonChannelDistributionValidator;
import com.synopsys.integration.alert.api.channel.jira.validation.JiraFieldMappingValidator;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;

@Component
public class JiraServerDistributionConfigurationValidator implements DistributionConfigurationValidator {
    private final JiraServerChannelKey jiraServerChannelKey;
    private final JiraFieldMappingValidator jiraFieldMappingValidator;
    private final CommonChannelDistributionValidator commonChannelDistributionValidator;

    @Autowired
    public JiraServerDistributionConfigurationValidator(
        JiraServerChannelKey jiraServerChannelKey,
        JiraFieldMappingValidator jiraFieldMappingValidator,
        CommonChannelDistributionValidator commonChannelDistributionValidator
    ) {
        this.jiraServerChannelKey = jiraServerChannelKey;
        this.jiraFieldMappingValidator = jiraFieldMappingValidator;
        this.commonChannelDistributionValidator = commonChannelDistributionValidator;
    }

    @Override
    public Set<AlertFieldStatus> validate(JobFieldModel jobFieldModel) {
        HashSet<AlertFieldStatus> validationResults = new HashSet<>();
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromJobFieldModel(jobFieldModel);

        commonChannelDistributionValidator.validate(configurationFieldValidator);
        configurationFieldValidator.validateRequiredFieldsAreNotBlank(JiraServerDescriptor.KEY_JIRA_PROJECT_NAME, JiraServerDescriptor.KEY_ISSUE_TYPE);
        configurationFieldValidator.validateRequiredRelatedSet(
            JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, JiraServerDescriptor.LABEL_OPEN_WORKFLOW_TRANSITION,
            JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION
        );

        // Validate custom field mappings
        jobFieldModel.getFieldModels()
            .stream()
            .filter(fieldModel -> jiraServerChannelKey.getUniversalKey().equals(fieldModel.getDescriptorName()))
            .findFirst()
            .flatMap(fieldModel -> fieldModel.getFieldValueModel(JiraServerDescriptor.KEY_FIELD_MAPPING))
            .flatMap(fieldValueModel -> jiraFieldMappingValidator.validateFieldMappings(JiraServerDescriptor.KEY_FIELD_MAPPING, fieldValueModel))
            .ifPresent(validationResults::add);

        validationResults.addAll(configurationFieldValidator.getValidationResults());
        return validationResults;
    }

}
