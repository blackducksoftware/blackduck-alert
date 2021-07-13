/*
 * api-provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.provider;

import static com.synopsys.integration.alert.api.provider.ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT;
import static com.synopsys.integration.alert.api.provider.ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT;
import static com.synopsys.integration.alert.api.provider.ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

@Component
public class CommonProviderDistributionValidator {
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public CommonProviderDistributionValidator(ConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    public void validate(ConfigurationFieldValidator configurationFieldValidator) {
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
        this.validateConfigExists(configurationFieldValidator);

        configurationFieldValidator.validateRequiredFieldIsNotBlank(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES);

        // TODO the processing type field should be moved to the ChannelDistributionUIConfig
        // TODO add validation for this field, should add a warning if the User has chosen the Summary processing type with an issue tracker channel
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ProviderDistributionUIConfig.KEY_PROCESSING_TYPE);
        configurationFieldValidator.validateRequiredRelatedSet(ProviderDistributionUIConfig.KEY_PROCESSING_TYPE, ProviderDistributionUIConfig.LABEL_PROCESSING, ChannelDistributionUIConfig.KEY_CHANNEL_NAME);

        this.validateFilterByProject(configurationFieldValidator);
        this.validateProjectNamePattern(configurationFieldValidator);

        configurationFieldValidator.validateRequiredRelatedSet(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, ProviderDistributionUIConfig.LABEL_PROJECTS, ChannelDistributionUIConfig.KEY_PROVIDER_NAME, ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
        this.validateConfiguredProject(configurationFieldValidator);
    }

    private void validateConfigExists(ConfigurationFieldValidator configurationFieldValidator) {
        Optional<ConfigurationModel> configModel = configurationFieldValidator.getLongValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID)
                                                       .flatMap(configurationAccessor::getConfigurationById);
        if (configModel.isEmpty()) {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, "Provider configuration missing."));
        }
    }

    private void validateFilterByProject(ConfigurationFieldValidator configurationFieldValidator) {
        boolean filterByProject = configurationFieldValidator.getBooleanValue(KEY_FILTER_BY_PROJECT).orElse(false);
        String projectNamePattern = configurationFieldValidator.getStringValue(KEY_PROJECT_NAME_PATTERN).orElse(null);
        Collection<String> configuredProjects = configurationFieldValidator.getCollectionOfValues(KEY_CONFIGURED_PROJECT).orElse(Collections.emptySet());

        if (filterByProject && StringUtils.isBlank(projectNamePattern) && configuredProjects.isEmpty()) {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(KEY_FILTER_BY_PROJECT, "You must specify a project name pattern or select at least one project."));
        }
    }

    private void validateProjectNamePattern(ConfigurationFieldValidator configurationFieldValidator) {
        String projectNamePattern = configurationFieldValidator.getStringValue(KEY_PROJECT_NAME_PATTERN).orElse(null);
        if (StringUtils.isNotBlank(projectNamePattern)) {
            try {
                Pattern.compile(projectNamePattern);
            } catch (PatternSyntaxException e) {
                configurationFieldValidator.addValidationResults(AlertFieldStatus.error(KEY_PROJECT_NAME_PATTERN, "Project name pattern is not a regular expression. " + e.getMessage()));
            }
        }
    }

    private void validateConfiguredProject(ConfigurationFieldValidator configurationFieldValidator) {
        boolean filterByProject = configurationFieldValidator.getBooleanValue(KEY_FILTER_BY_PROJECT).orElse(false);
        String projectNamePattern = configurationFieldValidator.getStringValue(KEY_PROJECT_NAME_PATTERN).orElse(null);
        Collection<String> configuredProjects = configurationFieldValidator.getCollectionOfValues(KEY_CONFIGURED_PROJECT).orElse(Collections.emptySet());

        boolean missingProject = configuredProjects.isEmpty() && StringUtils.isBlank(projectNamePattern);
        if (filterByProject && missingProject) {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(KEY_CONFIGURED_PROJECT, "You must select at least one project."));
        }
    }

}
