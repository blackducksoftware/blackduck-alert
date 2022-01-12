/*
 * api-provider
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.provider;

import static com.synopsys.integration.alert.api.provider.ProviderDescriptor.KEY_CONFIGURED_PROJECT;
import static com.synopsys.integration.alert.api.provider.ProviderDescriptor.KEY_FILTER_BY_PROJECT;
import static com.synopsys.integration.alert.api.provider.ProviderDescriptor.KEY_PROJECT_NAME_PATTERN;
import static com.synopsys.integration.alert.api.provider.ProviderDescriptor.KEY_PROJECT_VERSION_NAME_PATTERN;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;

@Component
public class CommonProviderDistributionValidator {
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @Autowired
    public CommonProviderDistributionValidator(ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor) {
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    public void validate(ConfigurationFieldValidator configurationFieldValidator) {
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
        this.validateConfigExists(configurationFieldValidator);

        configurationFieldValidator.validateRequiredFieldIsNotBlank(ProviderDescriptor.KEY_NOTIFICATION_TYPES);

        // TODO the processing type field should be moved to the ChannelDistributionUIConfig
        // TODO add validation for this field, should add a warning if the User has chosen the Summary processing type with an issue tracker channel
        configurationFieldValidator.validateRequiredFieldIsNotBlank(ProviderDescriptor.KEY_PROCESSING_TYPE);
        configurationFieldValidator.validateRequiredRelatedSet(ProviderDescriptor.KEY_PROCESSING_TYPE, ProviderDescriptor.LABEL_PROCESSING, ChannelDescriptor.KEY_CHANNEL_NAME);

        this.validateFilterByProject(configurationFieldValidator);
        this.validateProjectNamePattern(configurationFieldValidator);
        this.validateProjectVersionNamePattern(configurationFieldValidator);

        configurationFieldValidator.validateRequiredRelatedSet(ProviderDescriptor.KEY_CONFIGURED_PROJECT, ProviderDescriptor.LABEL_PROJECTS, ChannelDescriptor.KEY_PROVIDER_TYPE, ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
        this.validateConfiguredProject(configurationFieldValidator);
    }

    private void validateConfigExists(ConfigurationFieldValidator configurationFieldValidator) {
        Optional<ConfigurationModel> configModel = configurationFieldValidator.getLongValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID)
            .flatMap(configurationModelConfigurationAccessor::getConfigurationById);
        if (configModel.isEmpty()) {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, "Provider configuration missing."));
        }
    }

    private void validateFilterByProject(ConfigurationFieldValidator configurationFieldValidator) {
        boolean filterByProject = configurationFieldValidator.getBooleanValue(KEY_FILTER_BY_PROJECT).orElse(false);
        String projectNamePattern = configurationFieldValidator.getStringValue(KEY_PROJECT_NAME_PATTERN).orElse(null);
        String projectVersionNamePattern = configurationFieldValidator.getStringValue(KEY_PROJECT_VERSION_NAME_PATTERN).orElse(null);
        Collection<String> configuredProjects = configurationFieldValidator.getCollectionOfValues(KEY_CONFIGURED_PROJECT).orElse(Collections.emptySet());

        if (filterByProject && StringUtils.isBlank(projectNamePattern) && StringUtils.isBlank(projectVersionNamePattern) && configuredProjects.isEmpty()) {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(KEY_FILTER_BY_PROJECT, "You must specify a project name pattern or select at least one project."));
        }
    }

    private void validateProjectNamePattern(ConfigurationFieldValidator configurationFieldValidator) {
        validatePattern(configurationFieldValidator, KEY_PROJECT_NAME_PATTERN, "Project name pattern");
    }

    private void validateProjectVersionNamePattern(ConfigurationFieldValidator configurationFieldValidator) {
        validatePattern(configurationFieldValidator, KEY_PROJECT_VERSION_NAME_PATTERN, "Project version name pattern");
    }

    private void validatePattern(ConfigurationFieldValidator configurationFieldValidator, String key, String label) {
        String pattern = configurationFieldValidator.getStringValue(key).orElse(null);
        if (StringUtils.isNotBlank(pattern)) {
            try {
                Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                configurationFieldValidator.addValidationResults(AlertFieldStatus.error(key, label + " is not a regular expression. " + e.getMessage()));
            }
        }
    }

    private void validateConfiguredProject(ConfigurationFieldValidator configurationFieldValidator) {
        boolean filterByProject = configurationFieldValidator.getBooleanValue(KEY_FILTER_BY_PROJECT).orElse(false);
        String projectNamePattern = configurationFieldValidator.getStringValue(KEY_PROJECT_NAME_PATTERN).orElse(null);
        String projectVersionNamePattern = configurationFieldValidator.getStringValue(KEY_PROJECT_VERSION_NAME_PATTERN).orElse(null);
        Collection<String> configuredProjects = configurationFieldValidator.getCollectionOfValues(KEY_CONFIGURED_PROJECT).orElse(Collections.emptySet());

        boolean missingProject = configuredProjects.isEmpty() && StringUtils.isBlank(projectNamePattern) && StringUtils.isBlank(projectVersionNamePattern);
        if (filterByProject && missingProject) {
            configurationFieldValidator.addValidationResults(AlertFieldStatus.error(KEY_CONFIGURED_PROJECT, "You must select at least one project."));
        }
    }

}
