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
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.action.ChannelDistributionDescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.rest.model.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.alert.database.api.DefaultProviderDataAccessor;
import com.synopsys.integration.alert.provider.DefaultEmailHandler;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;

@Component
public class EmailDistributionDescriptorActionApi extends ChannelDistributionDescriptorActionApi {
    private final DefaultEmailHandler blackDuckEmailHandler;
    private final DefaultProviderDataAccessor blackDuckDataAccessor;

    @Autowired
    public EmailDistributionDescriptorActionApi(final EmailChannel emailChannel, final List<ProviderDescriptor> providerDescriptors, final DefaultProviderDataAccessor blackDuckDataAccessor,
        final DefaultEmailHandler blackDuckEmailHandler) {
        super(emailChannel, providerDescriptors);
        this.blackDuckEmailHandler = blackDuckEmailHandler;
        this.blackDuckDataAccessor = blackDuckDataAccessor;
    }

    @Override
    public TestConfigModel createTestConfigModel(final String configId, final FieldAccessor fieldAccessor, final String destination) throws AlertFieldException {
        final Set<String> emailAddresses = new HashSet<>();
        if (StringUtils.isNotBlank(destination)) {
            emailAddresses.add(destination);
        }

        final Boolean filterByProject = fieldAccessor.getString(CommonDistributionConfiguration.KEY_FILTER_BY_PROJECT)
                                            .map(Boolean::parseBoolean)
                                            .orElse(Boolean.FALSE);
        final Boolean isBlackduckProvider = fieldAccessor.getString(ChannelDistributionUIConfig.KEY_PROVIDER_NAME)
                                                .filter(providerName -> BlackDuckProvider.COMPONENT_NAME.equals(providerName))
                                                .isPresent();
        if (isBlackduckProvider) {
            final Set<ProviderProject> blackDuckProjects = retrieveBlackDuckProjects(fieldAccessor, filterByProject);
            if (null != blackDuckProjects) {
                final Set<String> blackDuckEmailAddresses = addEmailAddresses(blackDuckProjects, fieldAccessor);
                emailAddresses.addAll(blackDuckEmailAddresses);
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

    private Set<ProviderProject> retrieveBlackDuckProjects(final FieldAccessor fieldAccessor, final Boolean filterByProject) throws AlertFieldException {
        final List<ProviderProject> blackDuckProjects = blackDuckDataAccessor.findByProviderName(BlackDuckProvider.COMPONENT_NAME);
        if (filterByProject) {
            final Optional<ConfigurationFieldModel> projectField = fieldAccessor.getField(CommonDistributionConfiguration.KEY_CONFIGURED_PROJECT);
            final Set<String> configuredProjects = projectField.map(ConfigurationFieldModel::getFieldValues).orElse(Set.of()).stream().collect(Collectors.toSet());
            final String projectNamePattern = fieldAccessor.getString(CommonDistributionConfiguration.KEY_PROJECT_NAME_PATTERN).orElse("");
            final boolean noProjectsMatchPattern = blackDuckProjects.stream().noneMatch(databaseEntity -> projectNamePattern.matches(databaseEntity.getName()));
            if (noProjectsMatchPattern && StringUtils.isNotBlank(projectNamePattern)) {
                final Map<String, String> fieldErrors = new HashMap<>();
                fieldErrors.put(CommonDistributionConfiguration.KEY_PROJECT_NAME_PATTERN, "Does not match any of the Projects.");
                throw new AlertFieldException(fieldErrors);
            }
            return blackDuckProjects
                       .stream()
                       .filter(databaseEntity -> doesProjectMatchConfiguration(databaseEntity.getName(), projectNamePattern, configuredProjects))
                       .collect(Collectors.toSet());
        }
        return blackDuckProjects
                   .stream()
                   .collect(Collectors.toSet());
    }

    private Set<String> addEmailAddresses(final Set<ProviderProject> blackDuckProjects, final FieldAccessor fieldAccessor) throws AlertFieldException {
        final Optional<String> projectOwnerOnlyOptional = fieldAccessor.getString(EmailDescriptor.KEY_PROJECT_OWNER_ONLY);
        final Boolean projectOwnerOnly = Boolean.parseBoolean(projectOwnerOnlyOptional.orElse("false"));

        final Set<String> emailAddresses = new HashSet<>();
        final Set<String> projectsWithoutEmails = new HashSet<>();
        for (final ProviderProject project : blackDuckProjects) {
            final Set<String> emailsForProject = blackDuckEmailHandler.getEmailAddressesForProject(project, projectOwnerOnly);
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
            fieldErrors.put(CommonDistributionConfiguration.KEY_CONFIGURED_PROJECT, errorMessage);
            throw new AlertFieldException(fieldErrors);
        }
        return emailAddresses;
    }

}
