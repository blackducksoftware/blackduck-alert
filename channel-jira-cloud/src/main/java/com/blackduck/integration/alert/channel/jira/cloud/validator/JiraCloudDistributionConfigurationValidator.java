package com.blackduck.integration.alert.channel.jira.cloud.validator;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.CommonChannelDistributionValidator;
import com.blackduck.integration.alert.api.channel.jira.validation.JiraFieldMappingValidator;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.blackduck.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.blackduck.integration.alert.common.rest.model.JobFieldModel;

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
