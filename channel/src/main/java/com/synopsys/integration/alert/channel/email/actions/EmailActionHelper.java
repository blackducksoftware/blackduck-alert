/**
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.email.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.email.EmailAddressHandler;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailActionHelper {
    private final EmailAddressHandler emailAddressHandler;
    private final ProviderDataAccessor providerDataAccessor;

    public EmailActionHelper(EmailAddressHandler emailAddressHandler, ProviderDataAccessor providerDataAccessor) {
        this.emailAddressHandler = emailAddressHandler;
        this.providerDataAccessor = providerDataAccessor;
    }

    public FieldUtility createUpdatedFieldAccessor(FieldUtility fieldUtility, String destination) throws IntegrationException {
        Set<String> emailAddresses = new HashSet<>();
        if (StringUtils.isNotBlank(destination)) {
            emailAddresses.add(destination);
        }

        boolean filterByProject = fieldUtility.getBooleanOrFalse(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT);
        Long providerConfigId = fieldUtility.getLong(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID).orElse(null);
        boolean onlyAdditionalEmails = fieldUtility.getBooleanOrFalse(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY);

        if (null != providerConfigId && !onlyAdditionalEmails) {
            Set<ProviderProject> providerProjects = retrieveProviderProjects(fieldUtility, filterByProject, providerConfigId);
            if (null != providerProjects) {
                Set<String> providerEmailAddresses = addEmailAddresses(providerProjects, fieldUtility);
                emailAddresses.addAll(providerEmailAddresses);
            }
        }

        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(EmailDescriptor.KEY_EMAIL_ADDRESSES);
        configurationFieldModel.setFieldValues(emailAddresses);

        Map<String, ConfigurationFieldModel> fields = fieldUtility.getFields();
        fields.put(EmailDescriptor.KEY_EMAIL_ADDRESSES, configurationFieldModel);

        return new FieldUtility(fields);
    }

    private boolean doesProjectMatchConfiguration(String currentProjectName, String projectNamePattern, Set<String> configuredProjectNames) {
        return currentProjectName.matches(projectNamePattern) || configuredProjectNames.contains(currentProjectName);
    }

    private Set<ProviderProject> retrieveProviderProjects(FieldUtility fieldUtility, boolean filterByProject, Long providerConfigId) {
        List<ProviderProject> providerProjects = providerDataAccessor.getProjectsByProviderConfigId(providerConfigId);
        if (filterByProject) {
            Optional<ConfigurationFieldModel> projectField = fieldUtility.getField(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT);
            Set<String> configuredProjects = new HashSet<>(projectField.map(ConfigurationFieldModel::getFieldValues).orElse(Set.of()));
            String projectNamePattern = fieldUtility.getStringOrEmpty(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN);
            return providerProjects
                       .stream()
                       .filter(databaseEntity -> doesProjectMatchConfiguration(databaseEntity.getName(), projectNamePattern, configuredProjects))
                       .collect(Collectors.toSet());
        }
        return new HashSet<>(providerProjects);
    }

    private Set<String> addEmailAddresses(Set<ProviderProject> providerProjects, FieldUtility fieldUtility) throws AlertFieldException {
        boolean projectOwnerOnly = fieldUtility.getBoolean(EmailDescriptor.KEY_PROJECT_OWNER_ONLY).orElse(false);

        Set<String> emailAddresses = new HashSet<>();
        Set<String> projectsWithoutEmails = new HashSet<>();
        for (ProviderProject project : providerProjects) {
            Set<String> emailsForProject = emailAddressHandler.getEmailAddressesForProject(project, projectOwnerOnly);
            if (emailsForProject.isEmpty()) {
                projectsWithoutEmails.add(project.getName());
            }
            emailAddresses.addAll(emailsForProject);
        }
        if (!projectsWithoutEmails.isEmpty()) {
            String projects = StringUtils.join(projectsWithoutEmails, ", ");
            String errorMessage;
            if (projectOwnerOnly) {
                errorMessage = String.format("Could not find Project owners for the projects: %s", projects);
            } else {
                errorMessage = String.format("Could not find any email addresses for the projects: %s", projects);
            }
            String errorField = ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT;
            boolean filterByProject = fieldUtility.getBoolean(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT).orElse(false);
            if (!filterByProject) {
                errorField = ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT;
            }
            throw AlertFieldException.singleFieldError(errorField, errorMessage);
        }
        return emailAddresses;
    }

}
