/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.context.ChannelDistributionDescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckEmailHandler;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.configuration.TestConfigModel;

@Component
public class EmailDistributionDescriptorActionApi extends ChannelDistributionDescriptorActionApi {
    private final BlackDuckEmailHandler blackDuckEmailHandler;
    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;

    @Autowired
    public EmailDistributionDescriptorActionApi(final EmailChannel emailChannel, final List<ProviderDescriptor> providerDescriptors, final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor,
        final BlackDuckEmailHandler blackDuckEmailHandler) {
        super(emailChannel, providerDescriptors);
        this.blackDuckEmailHandler = blackDuckEmailHandler;
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
    }

    @Override
    public TestConfigModel createTestConfigModel(final String configId, final FieldAccessor fieldAccessor, final String destination) throws AlertFieldException {
        final Set<String> emailAddresses = new HashSet<>();

        final Boolean filterByProject = fieldAccessor.getString(BlackDuckDescriptor.KEY_FILTER_BY_PROJECT)
                                            .map(Boolean::parseBoolean)
                                            .orElse(Boolean.FALSE);
        final Boolean isBlackduckProvider = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME)
                                                .filter(providerName -> BlackDuckProvider.COMPONENT_NAME.equals(providerName))
                                                .isPresent();
        if (isBlackduckProvider) {
            final Set<BlackDuckProjectEntity> blackDuckProjectEntities = retrieveBlackDuckEntities(fieldAccessor, filterByProject);
            if (null != blackDuckProjectEntities) {
                addEmailAddresses(blackDuckProjectEntities, fieldAccessor, emailAddresses);
            }
        }

        final ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(EmailDescriptor.KEY_EMAIL_ADDRESSES);
        configurationFieldModel.setFieldValues(emailAddresses);

        final Map<String, ConfigurationFieldModel> fields = fieldAccessor.getFields();
        fields.put(EmailDescriptor.KEY_EMAIL_ADDRESSES, configurationFieldModel);

        final FieldAccessor newFieldAccessor = new FieldAccessor(fields);
        return super.createTestConfigModel(configId, newFieldAccessor, destination);
    }

    public boolean doesProjectMatchConfiguration(final String currentProjectName, final String projectNamePattern, final Set<String> configuredProjectNames) {
        return currentProjectName.matches(projectNamePattern) || configuredProjectNames.contains(currentProjectName);
    }

    private Set<BlackDuckProjectEntity> retrieveBlackDuckEntities(final FieldAccessor fieldAccessor, final Boolean filterByProject) throws AlertFieldException {
        if (filterByProject) {
            final Optional<ConfigurationFieldModel> projectField = fieldAccessor.getField(BlackDuckDescriptor.KEY_CONFIGURED_PROJECT);
            final Set<String> configuredProjects = projectField.map(ConfigurationFieldModel::getFieldValues).orElse(Set.of()).stream().collect(Collectors.toSet());
            final String projectNamePattern = fieldAccessor.getString(BlackDuckDescriptor.KEY_PROJECT_NAME_PATTERN).orElse("");
            final List<BlackDuckProjectEntity> blackDuckProjects = blackDuckProjectRepositoryAccessor.readEntities();
            final boolean noProjectsMatchPattern = blackDuckProjects.stream().noneMatch(databaseEntity -> projectNamePattern.matches(databaseEntity.getName()));
            if (noProjectsMatchPattern && StringUtils.isNotBlank(projectNamePattern)) {
                final Map<String, String> fieldErrors = new HashMap<>();
                fieldErrors.put(BlackDuckDescriptor.KEY_PROJECT_NAME_PATTERN, "Does not match any of the Projects.");
                throw new AlertFieldException(fieldErrors);
            }
            return blackDuckProjects
                       .stream()
                       .filter(databaseEntity -> doesProjectMatchConfiguration(databaseEntity.getName(), projectNamePattern, configuredProjects))
                       .collect(Collectors.toSet());
        }
        return blackDuckProjectRepositoryAccessor.readEntities()
                   .stream()
                   .collect(Collectors.toSet());
    }

    private void addEmailAddresses(final Set<BlackDuckProjectEntity> blackDuckProjectEntities, final FieldAccessor fieldAccessor, final Set<String> emailAddresses) throws AlertFieldException {
        final Optional<String> projectOwnerOnlyOptional = fieldAccessor.getString(EmailDescriptor.KEY_PROJECT_OWNER_ONLY);
        final Boolean projectOwnerOnly = Boolean.parseBoolean(projectOwnerOnlyOptional.orElse("false"));
        final Set<String> projectsWithoutEmails = new HashSet<>();
        for (final BlackDuckProjectEntity project : blackDuckProjectEntities) {
            final Set<String> emailsForProject = blackDuckEmailHandler.getBlackDuckEmailAddressesForProject(project, projectOwnerOnly);
            if (emailsForProject.isEmpty()) {
                projectsWithoutEmails.add(project.getName());
            }
            emailAddresses.addAll(emailsForProject);
        }
        if (!projectsWithoutEmails.isEmpty()) {
            final String projects = StringUtils.join(projectsWithoutEmails, ", ");
            final Map<String, String> fieldErrors = new HashMap<>();
            final String errorMessage;
            if (projectOwnerOnly) {
                errorMessage = String.format("Could not find Project owners for the projects: %s", projects);
            } else {
                errorMessage = String.format("Could not find any email addresses for the projects: %s", projects);
            }
            fieldErrors.put(BlackDuckDescriptor.KEY_CONFIGURED_PROJECT, errorMessage);
            throw new AlertFieldException(fieldErrors);
        }
    }

}
