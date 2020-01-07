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

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.HideCheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
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

    protected static final String LABEL_FILTER_BY_PROJECT = "Filter by project";
    protected static final String LABEL_PROJECT_NAME_PATTERN = "Project name pattern";
    protected static final String LABEL_PROJECTS = "Projects";
    protected static final String DESCRIPTION_FILTER_BY_PROJECT = "If selected, only notifications from the selected Projects table will be processed. Otherwise notifications from all Projects are processed.";
    protected static final String DESCRIPTION_PROJECT_NAME_PATTERN = "The regular expression to use to determine what Projects to include. These are in addition to the Projects selected in the table.";
    private static final String DESCRIPTION_PROJECTS = "Select a project or projects that will be used to retrieve notifications from your provider.";

    private static final String LABEL_NOTIFICATION_TYPES = "Notification Types";
    private static final String LABEL_FORMAT = "Format";
    private static final String DESCRIPTION_NOTIFICATION_TYPES = "Select one or more of the notification types. Only these notification types will be included for this distribution job.";
    private static final String DESCRIPTION_FORMAT = "Select the format of the message that will be created.";

    private final ProviderContent providerContent;

    public ProviderDistributionUIConfig(final String label, final String urlName, final ProviderContent providerContent) {
        super(label, label + " provider distribution setup.", urlName);
        this.providerContent = providerContent;
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField notificationTypesField = SelectConfigField.createRequired(KEY_NOTIFICATION_TYPES, LABEL_NOTIFICATION_TYPES, DESCRIPTION_NOTIFICATION_TYPES, false, true,
            providerContent.getContentTypes()
                .stream()
                .map(this::convertToLabelValueOption)
                .sorted()
                .collect(Collectors.toList()));
        final ConfigField formatField = SelectConfigField.createRequired(KEY_FORMAT_TYPE, LABEL_FORMAT, DESCRIPTION_FORMAT,
            providerContent.getSupportedContentFormats()
                .stream()
                .map(FormatType::name)
                .map(this::convertToLabelValueOption)
                .sorted()
                .collect(Collectors.toList()));

        final ConfigField filterByProject = HideCheckboxConfigField.create(KEY_FILTER_BY_PROJECT, LABEL_FILTER_BY_PROJECT, DESCRIPTION_FILTER_BY_PROJECT)
                                                .addRelatedHiddenFieldKeys(KEY_PROJECT_NAME_PATTERN, KEY_CONFIGURED_PROJECT)
                                                .addDefaultValue(Boolean.FALSE.toString());
        final ConfigField projectNamePattern = TextInputConfigField.create(KEY_PROJECT_NAME_PATTERN, LABEL_PROJECT_NAME_PATTERN, DESCRIPTION_PROJECT_NAME_PATTERN, this::validateProjectNamePattern);
        final ConfigField configuredProject = EndpointTableSelectField.createSearchable(KEY_CONFIGURED_PROJECT, LABEL_PROJECTS, DESCRIPTION_PROJECTS, this::validateConfiguredProject)
                                                  .addColumn(new TableSelectColumn("name", "Project Name", true, true))
                                                  .addColumn(new TableSelectColumn("description", "Project Description", false, false))
                                                  .addRequestedDataFieldKey(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);

        final List<ConfigField> configFields = List.of(notificationTypesField, formatField, filterByProject, projectNamePattern, configuredProject);
        final List<ConfigField> providerDistributionFields = createProviderDistributionFields();
        return Stream.concat(configFields.stream(), providerDistributionFields.stream()).collect(Collectors.toList());
    }

    public abstract List<ConfigField> createProviderDistributionFields();

    private LabelValueSelectOption convertToLabelValueOption(ProviderNotificationType providerContentType) {
        final String notificationType = providerContentType.getNotificationType();
        final String notificationTypeLabel = WordUtils.capitalizeFully(notificationType.replace("_", " "));
        return new LabelValueSelectOption(notificationTypeLabel, notificationType);
    }

    private LabelValueSelectOption convertToLabelValueOption(String formatName) {
        final String formatNameLabel = WordUtils.capitalizeFully(formatName.replace("_", " "));
        return new LabelValueSelectOption(formatNameLabel, formatName);
    }

    private Collection<String> validateProjectNamePattern(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final String projectNamePattern = fieldToValidate.getValue().orElse(null);
        if (StringUtils.isNotBlank(projectNamePattern)) {
            try {
                Pattern.compile(projectNamePattern);
            } catch (final PatternSyntaxException e) {
                return List.of("Project name pattern is not a regular expression. " + e.getMessage());
            }
        }
        return List.of();
    }

    private Collection<String> validateConfiguredProject(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final Collection<String> configuredProjects = Optional.ofNullable(fieldToValidate.getValues()).orElse(List.of());
        final boolean filterByProject = fieldModel.getFieldValueModel(KEY_FILTER_BY_PROJECT).flatMap(FieldValueModel::getValue).map(Boolean::parseBoolean).orElse(false);
        final String projectNamePattern = fieldModel.getFieldValueModel(KEY_PROJECT_NAME_PATTERN).flatMap(FieldValueModel::getValue).orElse(null);
        final boolean missingProject = configuredProjects.isEmpty() && StringUtils.isBlank(projectNamePattern);
        if (filterByProject && missingProject) {
            return List.of("You must select at least one project.");
        }
        return List.of();
    }

}
