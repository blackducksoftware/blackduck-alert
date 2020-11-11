/**
 * provider
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
package com.synopsys.integration.alert.provider.blackduck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectUsersService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpUrl;

@Component
public class BlackDuckProviderDataAccessor implements ProviderDataAccessor {
    private final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(BlackDuckProviderDataAccessor.class));
    private final ConfigurationAccessor configurationAccessor;
    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;

    @Autowired
    public BlackDuckProviderDataAccessor(ConfigurationAccessor configurationAccessor, BlackDuckPropertiesFactory blackDuckPropertiesFactory) {
        this.configurationAccessor = configurationAccessor;
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
    }

    @Override
    public List<ProviderProject> getProjectsByProviderConfigName(String providerConfigName) {
        try {
            Optional<ConfigurationModel> providerConfigOptional = configurationAccessor.getProviderConfigurationByName(providerConfigName);
            if (providerConfigOptional.isPresent()) {
                return getProjectsForProvider(providerConfigOptional.get());
            }
        } catch (IntegrationException e) {
            logger.error(String.format("Could not get the project for the provider '%s'. %s", providerConfigName, e.getMessage()));
            logger.debug(e.getMessage(), e);
        }
        return List.of();
    }

    @Override
    public List<ProviderProject> getProjectsByProviderConfigId(Long providerConfigId) {
        try {
            Optional<ConfigurationModel> providerConfigOptional = configurationAccessor.getConfigurationById(providerConfigId);
            if (providerConfigOptional.isPresent()) {
                return getProjectsForProvider(providerConfigOptional.get());
            }
        } catch (IntegrationException e) {
            logger.error(String.format("Could not get the project for the provider with id '%s'. %s", providerConfigId, e.getMessage()));
            logger.debug(e.getMessage(), e);
        }
        return List.of();
    }

    private List<ProviderProject> getProjectsForProvider(ConfigurationModel blackDuckConfigurationModel) throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckConfigurationModel);
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        List<ProjectView> allProjects = projectService.getAllProjects();
        return convertBlackDuckProjects(allProjects, blackDuckServicesFactory.getBlackDuckService());
    }

    @Override
    public void deleteProjects(Collection<ProviderProject> providerProjects) {
        //ignored since we are not using the database
    }

    @Override
    public Set<String> getEmailAddressesForProjectHref(Long providerConfigId, String projectHref) {
        try {
            Optional<ConfigurationModel> providerConfigOptional = configurationAccessor.getConfigurationById(providerConfigId);
            if (providerConfigOptional.isPresent()) {
                BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(providerConfigOptional.get());
                BlackDuckService blackDuckService = blackDuckServicesFactory.getBlackDuckService();
                ProjectView projectView = blackDuckService.getResponse(new HttpUrl(projectHref), ProjectView.class);
                return getEmailAddressesForProject(projectView, blackDuckServicesFactory.createProjectUsersService());
            }
        } catch (IntegrationException e) {
            logger.error(String.format("Could not get the project for the provider with id '%s'. %s", providerConfigId, e.getMessage()));
            logger.debug(e.getMessage(), e);
        }
        return Set.of();
    }

    @Override
    public List<ProviderUserModel> getUsersByProviderConfigId(Long providerConfigId) {
        if (null == providerConfigId) {
            return List.of();
        }
        try {
            Optional<ConfigurationModel> providerConfigOptional = configurationAccessor.getConfigurationById(providerConfigId);
            if (providerConfigOptional.isPresent()) {
                return getEmailAddressesByProvider(providerConfigOptional.get());
            }
        } catch (IntegrationException e) {
            logger.error(String.format("Could not get the project for the provider with id '%s'. %s", providerConfigId, e.getMessage()));
            logger.debug(e.getMessage(), e);
        }
        return List.of();
    }

    @Override
    public List<ProviderUserModel> getUsersByProviderConfigName(String providerConfigName) {
        if (StringUtils.isBlank(providerConfigName)) {
            return List.of();
        }
        try {
            Optional<ConfigurationModel> providerConfigOptional = configurationAccessor.getProviderConfigurationByName(providerConfigName);
            if (providerConfigOptional.isPresent()) {
                return getEmailAddressesByProvider(providerConfigOptional.get());
            }
        } catch (IntegrationException e) {
            logger.error(String.format("Could not get the project for the provider '%s'. %s", providerConfigName, e.getMessage()));
            logger.debug(e.getMessage(), e);
        }
        return List.of();
    }

    private List<ProviderUserModel> getEmailAddressesByProvider(ConfigurationModel blackDuckConfiguration) throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckConfiguration);
        BlackDuckService blackDuckService = blackDuckServicesFactory.getBlackDuckService();
        Set<String> allActiveBlackDuckUserEmailAddresses = getAllActiveBlackDuckUserEmailAddresses(blackDuckService);
        return allActiveBlackDuckUserEmailAddresses.stream()
                   .map(emailAddress -> new ProviderUserModel(emailAddress, false))
                   .collect(Collectors.toList());
    }

    @Override
    public void updateProjectAndUserData(Long providerConfigId, Map<ProviderProject, Set<String>> projectToUserData, Set<String> additionalRelevantUsers) {
        //ignored since are not updating the database
    }

    private List<ProviderProject> convertBlackDuckProjects(List<ProjectView> projectViews, BlackDuckService blackDuckService) {
        List<ProviderProject> providerProjects = new ArrayList<>();
        projectViews
            .stream()
            .forEach(projectView -> {
                ProviderProject providerProject = createProviderProject(projectView, blackDuckService);
                providerProjects.add(providerProject);
            });
        return providerProjects;
    }

    private ProviderProject createProviderProject(ProjectView projectView, BlackDuckService blackDuckService) {
        String projectOwnerEmail = null;
        if (StringUtils.isNotBlank(projectView.getProjectOwner())) {
            try {
                HttpUrl projectOwnerHttpUrl = new HttpUrl(projectView.getProjectOwner());
                UserView projectOwner = blackDuckService.getResponse(projectOwnerHttpUrl, UserView.class);
                projectOwnerEmail = projectOwner.getEmail();
            } catch (IntegrationException e) {
                logger.error(String.format("Could not get the project owner for Project: %s. Error: %s", projectView.getName(), e.getMessage()), e);
            }
        }
        return new ProviderProject(projectView.getName(), StringUtils.trimToEmpty(projectView.getDescription()), projectView.getMeta().getHref().toString(), projectOwnerEmail);
    }

    private BlackDuckServicesFactory createBlackDuckServicesFactory(ConfigurationModel blackDuckConfiguration) throws AlertException {
        BlackDuckProperties properties = blackDuckPropertiesFactory.createProperties(blackDuckConfiguration);
        BlackDuckHttpClient blackDuckHttpClient = properties.createBlackDuckHttpClient(logger);
        return properties.createBlackDuckServicesFactory(blackDuckHttpClient, logger);
    }

    private Set<String> getEmailAddressesForProject(ProjectView projectView, ProjectUsersService projectUsersService) throws IntegrationException {
        return projectUsersService.getAllActiveUsersForProject(projectView)
                   .stream()
                   .filter(UserView::getActive)
                   .map(UserView::getEmail)
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toSet());

    }

    private Set<String> getAllActiveBlackDuckUserEmailAddresses(BlackDuckService blackDuckService) throws IntegrationException {
        return blackDuckService.getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE)
                   .stream()
                   .filter(UserView::getActive)
                   .map(UserView::getEmail)
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toSet());
    }

}
