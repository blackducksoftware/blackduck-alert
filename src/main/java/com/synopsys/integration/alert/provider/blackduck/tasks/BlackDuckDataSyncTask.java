/**
 * blackduck-alert
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
package com.synopsys.integration.alert.provider.blackduck.tasks;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderTask;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectUsersService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.SilentIntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckDataSyncTask extends ProviderTask {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckDataSyncTask.class);
    private final ProviderDataAccessor blackDuckDataAccessor;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public BlackDuckDataSyncTask(BlackDuckProviderKey blackDuckProviderKey, TaskScheduler taskScheduler, ProviderDataAccessor blackDuckDataAccessor, ConfigurationAccessor configurationAccessor) {
        super(blackDuckProviderKey, taskScheduler);
        this.blackDuckDataAccessor = blackDuckDataAccessor;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public void runProviderTask() {
        try {
            BlackDuckProperties providerProperties = getProviderProperties();
            Optional<BlackDuckHttpClient> optionalBlackDuckHttpClient = providerProperties.createBlackDuckHttpClientAndLogErrors(logger);
            if (optionalBlackDuckHttpClient.isPresent()) {
                BlackDuckHttpClient blackDuckHttpClient = optionalBlackDuckHttpClient.get();
                BlackDuckServicesFactory blackDuckServicesFactory = providerProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new Slf4jIntLogger(logger));
                ProjectUsersService projectUsersService = blackDuckServicesFactory.createProjectUsersService();
                BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();

                List<ProjectView> projectViews = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
                Map<ProjectView, ProviderProject> blackDuckToAlertProjects = mapBlackDuckProjectsToAlertProjects(projectViews, blackDuckService);
                Set<String> allProjectsInJobs = retrieveAllProjectsInJobs(blackDuckToAlertProjects.values());

                Map<ProviderProject, Set<String>> projectToEmailAddresses = getEmailsPerProject(blackDuckToAlertProjects, projectUsersService);
                Set<String> allRelevantBlackDuckUsers = getAllActiveBlackDuckUserEmailAddresses(blackDuckService);
                blackDuckDataAccessor.updateProjectAndUserData(providerProperties.getConfigId(), projectToEmailAddresses, allRelevantBlackDuckUsers);

                blackDuckServicesFactory = providerProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new SilentIntLogger());
                projectUsersService = blackDuckServicesFactory.createProjectUsersService();
                updateBlackDuckProjectPermissions(allProjectsInJobs, projectViews, projectUsersService, blackDuckService);
            } else {
                logger.error("Missing BlackDuck global configuration.");
            }
        } catch (IntegrationException | AlertRuntimeException e) {
            logger.error("Could not retrieve the current data from the BlackDuck server: " + e.getMessage(), e);
        }
    }

    @Override
    protected BlackDuckProperties getProviderProperties() {
        return (BlackDuckProperties) super.getProviderProperties();
    }

    private Map<ProjectView, ProviderProject> mapBlackDuckProjectsToAlertProjects(List<ProjectView> projectViews, BlackDuckService blackDuckService) {
        Map<ProjectView, ProviderProject> projectMap = new ConcurrentHashMap<>();
        projectViews
            .parallelStream()
            .forEach(projectView -> {
                String projectOwnerEmail = null;
                if (StringUtils.isNotBlank(projectView.getProjectOwner())) {
                    try {
                        UserView projectOwner = blackDuckService.getResponse(projectView.getProjectOwner(), UserView.class);
                        projectOwnerEmail = projectOwner.getEmail();
                    } catch (IntegrationException e) {
                        logger.error(String.format("Could not get the project owner for Project: %s. Error: %s", projectView.getName(), e.getMessage()), e);
                    }
                }
                projectMap.put(projectView, new ProviderProject(projectView.getName(), StringUtils.trimToEmpty(projectView.getDescription()), projectView.getMeta().getHref(), projectOwnerEmail));
            });
        return projectMap;
    }

    private Map<ProviderProject, Set<String>> getEmailsPerProject(Map<ProjectView, ProviderProject> blackDuckToAlertProjects, ProjectUsersService projectUsersService) {
        Map<ProviderProject, Set<String>> projectToEmailAddresses = new ConcurrentHashMap<>();
        blackDuckToAlertProjects.entrySet()
            .parallelStream()
            .forEach(entry -> {
                try {
                    ProjectView blackDuckProjectView = entry.getKey();
                    ProviderProject alertProject = entry.getValue();
                    Set<String> projectUserEmailAddresses = projectUsersService.getAllActiveUsersForProject(blackDuckProjectView)
                                                                .stream()
                                                                .filter(UserView::getActive)
                                                                .map(UserView::getEmail)
                                                                .filter(StringUtils::isNotBlank)
                                                                .collect(Collectors.toSet());
                    if (StringUtils.isNotBlank(alertProject.getProjectOwnerEmail())) {
                        projectUserEmailAddresses.add(alertProject.getProjectOwnerEmail());
                    }
                    projectToEmailAddresses.put(alertProject, projectUserEmailAddresses);
                } catch (IntegrationException e) {
                    // We do this to break out of the stream
                    throw new AlertRuntimeException(e.getMessage(), e);
                }
            });
        return projectToEmailAddresses;
    }

    private Set<String> retrieveAllProjectsInJobs(Collection<ProviderProject> foundProjects) {
        Set<String> configuredProjectNames = new HashSet<>();
        for (ConfigurationJobModel configurationJobModel : configurationAccessor.getAllJobs()) {
            FieldAccessor fieldAccessor = configurationJobModel.getFieldAccessor();
            String projectNamePattern = fieldAccessor.getStringOrEmpty(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN);
            if (StringUtils.isNotBlank(projectNamePattern)) {
                Set<String> matchedProjectNames = foundProjects.stream().map(ProviderProject::getName).filter(name -> name.matches(projectNamePattern)).collect(Collectors.toSet());
                configuredProjectNames.addAll(matchedProjectNames);
            }
            Collection<String> configuredProjects = fieldAccessor.getAllStrings(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT);
            configuredProjectNames.addAll(configuredProjects);
        }

        return configuredProjectNames;
    }

    private Set<String> getAllActiveBlackDuckUserEmailAddresses(BlackDuckService blackDuckService) throws IntegrationException {
        return blackDuckService.getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE)
                   .stream()
                   .filter(UserView::getActive)
                   .map(UserView::getEmail)
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toSet());
    }

    private void updateBlackDuckProjectPermissions(Set<String> configuredProjects, List<ProjectView> projectViews, ProjectUsersService projectUsersService, BlackDuckService blackDuckService) throws IntegrationException {
        UserView currentUser = blackDuckService.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
        Set<ProjectView> matchingProjects = projectViews.parallelStream()
                                                .filter(projectView -> configuredProjects.contains(projectView.getName()))
                                                .collect(Collectors.toSet());
        for (ProjectView projectView : matchingProjects) {
            logger.debug("Adding user to Project {}", projectView.getName());
            projectUsersService.addUserToProject(projectView, currentUser);
        }
    }

}
