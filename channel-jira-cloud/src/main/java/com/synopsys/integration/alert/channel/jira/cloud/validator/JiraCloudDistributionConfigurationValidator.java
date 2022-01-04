/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.validator;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.CommonChannelDistributionValidator;
import com.synopsys.integration.alert.api.channel.jira.validation.JiraFieldMappingValidator;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;

@Component
public class JiraCloudDistributionConfigurationValidator implements DistributionConfigurationValidator {
    private final JiraCloudChannelKey jiraCloudChannelKey;
    private final JiraFieldMappingValidator jiraFieldMappingValidator;
    private final CommonChannelDistributionValidator commonChannelDistributionValidator;

    @Autowired
    public JiraCloudDistributionConfigurationValidator(
        JiraCloudChannelKey jiraCloudChannelKey,
        JiraFieldMappingValidator jiraFieldMappingValidator,
        CommonChannelDistributionValidator commonChannelDistributionValidator
    ) {
        this.jiraFieldMappingValidator = jiraFieldMappingValidator;
        this.jiraCloudChannelKey = jiraCloudChannelKey;
        this.commonChannelDistributionValidator = commonChannelDistributionValidator;
    }

    @Override
    public Set<AlertFieldStatus> validate(JobFieldModel jobFieldModel) {
        HashSet<AlertFieldStatus> validationResults = new HashSet<>();
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromJobFieldModel(jobFieldModel);

        commonChannelDistributionValidator.validate(configurationFieldValidator);
        configurationFieldValidator.validateRequiredFieldsAreNotBlank(JiraCloudDescriptor.KEY_JIRA_PROJECT_NAME, JiraCloudDescriptor.KEY_ISSUE_TYPE);
        configurationFieldValidator.validateRequiredRelatedSet(
            JiraCloudDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, JiraCloudDescriptor.LABEL_OPEN_WORKFLOW_TRANSITION,
            JiraCloudDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION
        );

        // Validate custom field mappings
        jobFieldModel.getFieldModels()
            .stream()
            .filter(fieldModel -> jiraCloudChannelKey.getUniversalKey().equals(fieldModel.getDescriptorName()))
            .findFirst()
            .flatMap(fieldModel -> fieldModel.getFieldValueModel(JiraCloudDescriptor.KEY_FIELD_MAPPING))
            .flatMap(fieldValueModel -> jiraFieldMappingValidator.validateFieldMappings(JiraCloudDescriptor.KEY_FIELD_MAPPING, fieldValueModel))
            .ifPresent(validationResults::add);

        validationResults.addAll(configurationFieldValidator.getValidationResults());
        return validationResults;
    }

}
