/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.HideCheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointSelectField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.EndpointTableSelectField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.TableSelectColumn;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.ProviderContent;
import com.synopsys.integration.alert.common.provider.ProviderNotificationType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public abstract class ProviderDistributionUIConfig extends UIConfig {
    public static final String KEY_NOTIFICATION_TYPES = "provider.distribution.notification.types";
    public static final String KEY_FORMAT_TYPE = "provider.distribution.format.type";
    public static final String KEY_FILTER_BY_PROJECT = ChannelDistributionUIConfig.KEY_COMMON_CHANNEL_PREFIX + "filter.by.project";
    public static final String KEY_PROJECT_NAME_PATTERN = ChannelDistributionUIConfig.KEY_COMMON_CHANNEL_PREFIX + "project.name.pattern";
    public static final String KEY_CONFIGURED_PROJECT = ChannelDistributionUIConfig.KEY_COMMON_CHANNEL_PREFIX + "configured.project";

    protected static final String LABEL_FILTER_BY_PROJECT = "Filter By Project";
    protected static final String LABEL_PROJECT_NAME_PATTERN = "Project Name Pattern";
    protected static final String LABEL_PROJECTS = "Projects";
    protected static final String DESCRIPTION_FILTER_BY_PROJECT = "If selected, only notifications from the selected Projects table will be processed. Otherwise notifications from all Projects are processed.";
    protected static final String DESCRIPTION_PROJECT_NAME_PATTERN = "The regular expression to use to determine what Projects to include. These are in addition to the Projects selected in the table.";
    private static final String DESCRIPTION_PROJECTS = "Select a project or projects that will be used to retrieve notifications from your provider.";

    private static final String LABEL_NOTIFICATION_TYPES = "Notification Types";
    private static final String LABEL_FORMAT = "Format";
    private static final String DESCRIPTION_NOTIFICATION_TYPES = "Select one or more of the notification types. Only these notification types will be included for this distribution job.";
    private static final String DESCRIPTION_FORMAT = "Select the format of the message that will be created: ";

    private final ProviderContent providerContent;

    public ProviderDistributionUIConfig(String label, String urlName, ProviderContent providerContent) {
        super(label, label + " provider distribution setup.", urlName);
        this.providerContent = providerContent;
    }

    @Override
    public List<ConfigField> createFields() {
        ConfigField providerConfigNameField = new EndpointSelectField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, ProviderDescriptor.LABEL_PROVIDER_CONFIG_NAME, ProviderDescriptor.DESCRIPTION_PROVIDER_CONFIG_NAME)
                                                  .applyClearable(false)
                                                  .applyRequired(true);
        List<LabelValueSelectOption> notificationTypeOptions = providerContent.getContentTypes()
                                                                   .stream()
                                                                   .map(this::convertToLabelValueOption)
                                                                   .sorted()
                                                                   .collect(Collectors.toList());
        ConfigField notificationTypesField = new SelectConfigField(KEY_NOTIFICATION_TYPES, LABEL_NOTIFICATION_TYPES, DESCRIPTION_NOTIFICATION_TYPES, notificationTypeOptions)
                                                 .applyMultiSelect(true)
                                                 .applyRequired(true);

        List<LabelValueSelectOption> supportedFormatOptions = providerContent.getSupportedContentFormats()
                                                                  .stream()
                                                                  .map(this::convertToLabelValueOption)
                                                                  .sorted()
                                                                  .collect(Collectors.toList());
        ConfigField formatField = new SelectConfigField(KEY_FORMAT_TYPE, LABEL_FORMAT, DESCRIPTION_FORMAT + createFormatDescription(), supportedFormatOptions)
                                      .applyRequired(true);

        ConfigField filterByProject = new HideCheckboxConfigField(KEY_FILTER_BY_PROJECT, LABEL_FILTER_BY_PROJECT, DESCRIPTION_FILTER_BY_PROJECT)
                                          .applyRelatedHiddenFieldKeys(KEY_PROJECT_NAME_PATTERN, KEY_CONFIGURED_PROJECT);
        ConfigField projectNamePattern = new TextInputConfigField(KEY_PROJECT_NAME_PATTERN, LABEL_PROJECT_NAME_PATTERN, DESCRIPTION_PROJECT_NAME_PATTERN)
                                             .applyValidationFunctions(this::validateProjectNamePattern);
        ConfigField configuredProject = new EndpointTableSelectField(KEY_CONFIGURED_PROJECT, LABEL_PROJECTS, DESCRIPTION_PROJECTS)
                                            .applyPaged(true)
                                            .applySearchable(true)
                                            .applyColumn(new TableSelectColumn("name", "Project Name", true, true))
                                            .applyColumn(new TableSelectColumn("description", "Project Description", false, false))
                                            .applyRequestedDataFieldKey(ChannelDistributionUIConfig.KEY_PROVIDER_NAME)
                                            .applyRequestedDataFieldKey(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME)
                                            .applyValidationFunctions(this::validateConfiguredProject);

        List<ConfigField> configFields = List.of(providerConfigNameField, notificationTypesField, formatField, filterByProject, projectNamePattern, configuredProject);
        List<ConfigField> providerDistributionFields = createProviderDistributionFields();
        return Stream.concat(configFields.stream(), providerDistributionFields.stream()).collect(Collectors.toList());
    }

    public abstract List<ConfigField> createProviderDistributionFields();

    private LabelValueSelectOption convertToLabelValueOption(ProviderNotificationType providerContentType) {
        String notificationType = providerContentType.getNotificationType();
        String notificationTypeLabel = WordUtils.capitalizeFully(notificationType.replace("_", " "));
        return new LabelValueSelectOption(notificationTypeLabel, notificationType);
    }

    private LabelValueSelectOption convertToLabelValueOption(FormatType formatType) {
        return new LabelValueSelectOption(formatType.getLabel(), formatType.name());
    }

    private String createFormatDescription() {
        StringBuilder formatDescription = new StringBuilder();
        for (FormatType format : providerContent.getSupportedContentFormats()) {
            String label = format.getLabel();
            String description = format.getDescription();

            formatDescription.append(label);
            formatDescription.append(": ");
            formatDescription.append(description);
            formatDescription.append(" ");
        }

        return formatDescription.toString();
    }

    private Collection<String> validateProjectNamePattern(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        String projectNamePattern = fieldToValidate.getValue().orElse(null);
        if (StringUtils.isNotBlank(projectNamePattern)) {
            try {
                Pattern.compile(projectNamePattern);
            } catch (PatternSyntaxException e) {
                return List.of("Project name pattern is not a regular expression. " + e.getMessage());
            }
        }
        return List.of();
    }

    private Collection<String> validateConfiguredProject(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        Collection<String> configuredProjects = Optional.ofNullable(fieldToValidate.getValues()).orElse(List.of());
        boolean filterByProject = fieldModel.getFieldValueModel(KEY_FILTER_BY_PROJECT).flatMap(FieldValueModel::getValue).map(Boolean::parseBoolean).orElse(false);
        String projectNamePattern = fieldModel.getFieldValueModel(KEY_PROJECT_NAME_PATTERN).flatMap(FieldValueModel::getValue).orElse(null);
        boolean missingProject = configuredProjects.isEmpty() && StringUtils.isBlank(projectNamePattern);
        if (filterByProject && missingProject) {
            return List.of("You must select at least one project.");
        }
        return List.of();
    }

}
