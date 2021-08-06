/*
 * api-provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.provider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.HideCheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointSelectField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.EndpointTableSelectField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.TableSelectColumn;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public abstract class ProviderDistributionUIConfig extends UIConfig {

    private final ProviderContent providerContent;
    private final ConfigurationAccessor configurationAccessor;

    public ProviderDistributionUIConfig(String label, String urlName, ProviderContent providerContent, ConfigurationAccessor configurationAccessor) {
        super(label, label + " provider distribution setup.", urlName);
        this.providerContent = providerContent;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public List<ConfigField> createFields() {
        ConfigField providerConfigIdField = new EndpointSelectField(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, ProviderDescriptor.LABEL_PROVIDER_CONFIG_NAME, ProviderDescriptor.DESCRIPTION_PROVIDER_CONFIG_FIELD)
            .applyClearable(false)
            .applyValidationFunctions(this::validateConfigExists)
            .applyRequired(true);
        List<LabelValueSelectOption> notificationTypeOptions = providerContent.getContentTypes()
            .stream()
            .map(this::convertToLabelValueOption)
            .sorted()
            .collect(Collectors.toList());
        ConfigField notificationTypesField = new SelectConfigField(ProviderDescriptor.KEY_NOTIFICATION_TYPES, ProviderDescriptor.LABEL_NOTIFICATION_TYPES, ProviderDescriptor.DESCRIPTION_NOTIFICATION_TYPES, notificationTypeOptions)
            .applyMultiSelect(true)
            .applyRequired(true);

        // TODO the processing type field should be moved to the ChannelDistributionUIConfig
        // TODO add validation for this field, should add a warning if the User has chosen the Summary processing type with an issue tracker channel
        ConfigField processingField = new EndpointSelectField(ProviderDescriptor.KEY_PROCESSING_TYPE, ProviderDescriptor.LABEL_PROCESSING, ProviderDescriptor.DESCRIPTION_PROCESSING + createProcessingDescription())
            .applyRequiredRelatedField(ChannelDistributionUIConfig.KEY_CHANNEL_NAME)
            .applyRequired(true);

        ConfigField filterByProject = new HideCheckboxConfigField(ProviderDescriptor.KEY_FILTER_BY_PROJECT, ProviderDescriptor.LABEL_FILTER_BY_PROJECT, ProviderDescriptor.DESCRIPTION_FILTER_BY_PROJECT)
            .applyRelatedHiddenFieldKeys(ProviderDescriptor.KEY_PROJECT_NAME_PATTERN, ProviderDescriptor.KEY_CONFIGURED_PROJECT)
            .applyValidationFunctions(this::validateFilterByProject);
        ConfigField projectNamePattern = new TextInputConfigField(ProviderDescriptor.KEY_PROJECT_NAME_PATTERN, ProviderDescriptor.LABEL_PROJECT_NAME_PATTERN, ProviderDescriptor.DESCRIPTION_PROJECT_NAME_PATTERN)
            .applyValidationFunctions(this::validateProjectNamePattern);
        ConfigField configuredProject = new EndpointTableSelectField(ProviderDescriptor.KEY_CONFIGURED_PROJECT, ProviderDescriptor.LABEL_PROJECTS, ProviderDescriptor.DESCRIPTION_PROJECTS)
            .applyPaged(true)
            .applySearchable(true)
            .applyUseRowAsValue(true)
            .applyColumn(TableSelectColumn.visible("name", "Project Name", true, true))
            .applyColumn(TableSelectColumn.hidden("href", "Project URL", false, false))
            .applyColumn(TableSelectColumn.visible("description", "Project Description", false, false))
            .applyRequiredRelatedField(ChannelDistributionUIConfig.KEY_PROVIDER_NAME)
            .applyRequiredRelatedField(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID)
            .applyValidationFunctions(this::validateConfiguredProject);

        List<ConfigField> configFields = List.of(providerConfigIdField, notificationTypesField, processingField, filterByProject, projectNamePattern, configuredProject);
        List<ConfigField> providerDistributionFields = createProviderDistributionFields();
        return Stream.concat(configFields.stream(), providerDistributionFields.stream()).collect(Collectors.toList());
    }

    public abstract List<ConfigField> createProviderDistributionFields();

    private LabelValueSelectOption convertToLabelValueOption(ProviderNotificationType providerContentType) {
        String notificationType = providerContentType.getNotificationType();
        String notificationTypeLabel = WordUtils.capitalizeFully(notificationType.replace("_", " "));
        return new LabelValueSelectOption(notificationTypeLabel, notificationType);
    }

    private String createProcessingDescription() {
        StringBuilder formatDescription = new StringBuilder();
        for (ProcessingType format : providerContent.getSupportedProcessingTypes()) {
            String label = format.getLabel();
            String description = format.getDescription();

            formatDescription.append(label);
            formatDescription.append(": ");
            formatDescription.append(description);
            formatDescription.append(" ");
        }

        return formatDescription.toString();
    }

    private ValidationResult validateConfigExists(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        Optional<ConfigurationModel> configModel = fieldToValidate.getValue()
            .map(Long::parseLong)
            .flatMap(configurationAccessor::getConfigurationById);
        if (!configModel.isPresent()) {
            return ValidationResult.errors("Provider configuration missing.");
        }
        return ValidationResult.success();
    }

    private ValidationResult validateFilterByProject(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        boolean filterByProject = fieldToValidate.getValue().map(Boolean::parseBoolean).orElse(false);
        String projectNamePattern = fieldModel.getFieldValue(ProviderDescriptor.KEY_PROJECT_NAME_PATTERN).orElse(null);
        Collection<String> configuredProjects = fieldModel.getFieldValueModel(ProviderDescriptor.KEY_CONFIGURED_PROJECT).map(FieldValueModel::getValues).orElse(List.of());

        if (filterByProject && StringUtils.isBlank(projectNamePattern) && configuredProjects.isEmpty()) {
            return ValidationResult.errors("You must specify a project name pattern or select at least one project.");
        }
        return ValidationResult.success();
    }

    private ValidationResult validateProjectNamePattern(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        String projectNamePattern = fieldToValidate.getValue().orElse(null);
        if (StringUtils.isNotBlank(projectNamePattern)) {
            try {
                Pattern.compile(projectNamePattern);
            } catch (PatternSyntaxException e) {
                return ValidationResult.errors("Project name pattern is not a regular expression. " + e.getMessage());
            }
        }
        return ValidationResult.success();
    }

    private ValidationResult validateConfiguredProject(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        Collection<String> configuredProjects = Optional.ofNullable(fieldToValidate.getValues()).orElse(List.of());
        boolean filterByProject = fieldModel.getFieldValueModel(ProviderDescriptor.KEY_FILTER_BY_PROJECT).flatMap(FieldValueModel::getValue).map(Boolean::parseBoolean).orElse(false);
        String projectNamePattern = fieldModel.getFieldValueModel(ProviderDescriptor.KEY_PROJECT_NAME_PATTERN).flatMap(FieldValueModel::getValue).orElse(null);
        boolean missingProject = configuredProjects.isEmpty() && StringUtils.isBlank(projectNamePattern);
        if (filterByProject && missingProject) {
            return ValidationResult.errors("You must select at least one project.");
        }
        return ValidationResult.success();
    }

}
