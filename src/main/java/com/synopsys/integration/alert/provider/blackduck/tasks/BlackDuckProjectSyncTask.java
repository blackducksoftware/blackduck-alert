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
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
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
public class BlackDuckProjectSyncTask extends ScheduledTask {
    public static final String TASK_NAME = "blackduck-sync-project-task";
    private final Logger logger = LoggerFactory.getLogger(BlackDuckProjectSyncTask.class);
    private final BlackDuckProperties blackDuckProperties;
    private final ProviderDataAccessor blackDuckDataAccessor;
    private final ConfigurationAccessor configurationAccessor;
    private final BlackDuckProviderKey providerKey;

    @Autowired
    public BlackDuckProjectSyncTask(TaskScheduler taskScheduler, BlackDuckProperties blackDuckProperties, ProviderDataAccessor blackDuckDataAccessor, ConfigurationAccessor configurationAccessor, BlackDuckProviderKey providerKey) {
        super(taskScheduler, TASK_NAME);
        this.blackDuckProperties = blackDuckProperties;
        this.blackDuckDataAccessor = blackDuckDataAccessor;
        this.configurationAccessor = configurationAccessor;
        this.providerKey = providerKey;
    }

    @Override
    public void runTask() {
        try {
            final Optional<BlackDuckHttpClient> optionalBlackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger);
            if (optionalBlackDuckHttpClient.isPresent()) {
                final BlackDuckHttpClient blackDuckHttpClient = optionalBlackDuckHttpClient.get();
                BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new Slf4jIntLogger(logger));
                ProjectUsersService projectUsersService = blackDuckServicesFactory.createProjectUsersService();
                final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
                final List<ProjectView> projectViews = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
                final Map<ProviderProject, ProjectView> currentDataMap = getCurrentData(projectViews, blackDuckService);
                final Set<String> allProjectsInJobs = retrieveAllProjectsInJobs(currentDataMap.keySet());
                final Map<ProviderProject, Set<String>> projectToEmailAddresses = getEmailsPerProject(currentDataMap, projectUsersService);
                blackDuckDataAccessor.updateProjectAndUserData(providerKey, projectToEmailAddresses);

                blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new SilentIntLogger());
                projectUsersService = blackDuckServicesFactory.createProjectUsersService();
                updateBlackDuckProjectPermissions(allProjectsInJobs, projectViews, projectUsersService, blackDuckService);
            } else {
                logger.error("Missing BlackDuck global configuration.");
            }
        } catch (final IntegrationException | AlertRuntimeException e) {
            logger.error("Could not retrieve the current data from the BlackDuck server: " + e.getMessage(), e);
        }
    }

    private Map<ProviderProject, ProjectView> getCurrentData(final List<ProjectView> projectViews, final BlackDuckService blackDuckService) {
        final Map<ProviderProject, ProjectView> projectMap = new ConcurrentHashMap<>();
        projectViews
            .parallelStream()
            .forEach(projectView -> {
                String projectOwnerEmail = null;
                if (StringUtils.isNotBlank(projectView.getProjectOwner())) {
                    try {
                        final UserView projectOwner = blackDuckService.getResponse(projectView.getProjectOwner(), UserView.class);
                        projectOwnerEmail = projectOwner.getEmail();
                    } catch (final IntegrationException e) {
                        logger.error(String.format("Could not get the project owner for Project: %s. Error: %s", projectView.getName(), e.getMessage()), e);
                    }
                }
                projectMap.put(new ProviderProject(projectView.getName(), StringUtils.trimToEmpty(projectView.getDescription()), projectView.getMeta().getHref(), projectOwnerEmail), projectView);
            });
        return projectMap;
    }

    private Map<ProviderProject, Set<String>> getEmailsPerProject(final Map<ProviderProject, ProjectView> currentDataMap, final ProjectUsersService projectUsersService) {
        final Map<ProviderProject, Set<String>> projectToEmailAddresses = new ConcurrentHashMap<>();
        currentDataMap.entrySet()
            .parallelStream()
            .forEach(entry -> {
                try {
                    final ProviderProject blackDuckProject = entry.getKey();
                    final ProjectView projectView = entry.getValue();

                    final Set<String> projectUserEmailAddresses = projectUsersService.getAllActiveUsersForProject(projectView)
                                                                      .stream()
                                                                      .map(UserView::getEmail)
                                                                      .filter(StringUtils::isNotBlank)
                                                                      .collect(Collectors.toSet());
                    if (StringUtils.isNotBlank(blackDuckProject.getProjectOwnerEmail())) {
                        projectUserEmailAddresses.add(blackDuckProject.getProjectOwnerEmail());
                    }
                    projectToEmailAddresses.put(blackDuckProject, projectUserEmailAddresses);

                } catch (final IntegrationException e) {
                    // We do this to break out of the stream
                    throw new AlertRuntimeException(e.getMessage(), e);
                }
            });
        return projectToEmailAddresses;
    }

    private Set<String> retrieveAllProjectsInJobs(final Set<ProviderProject> foundProjects) {
        final Set<String> configuredProjectNames = new HashSet<>();
        for (final ConfigurationJobModel configurationJobModel : configurationAccessor.getAllJobs()) {
            final FieldAccessor fieldAccessor = configurationJobModel.getFieldAccessor();
            final String projectNamePattern = fieldAccessor.getStringOrEmpty(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN);
            if (StringUtils.isNotBlank(projectNamePattern)) {
                final Set<String> matchedProjectNames = foundProjects.stream().map(ProviderProject::getName).filter(name -> name.matches(projectNamePattern)).collect(Collectors.toSet());
                configuredProjectNames.addAll(matchedProjectNames);
            }
            final Collection<String> configuredProjects = fieldAccessor.getAllStrings(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT);
            configuredProjectNames.addAll(configuredProjects);
        }

        return configuredProjectNames;
    }

    private void updateBlackDuckProjectPermissions(final Set<String> configuredProjects, final List<ProjectView> projectViews, final ProjectUsersService projectUsersService, final BlackDuckService blackDuckService)
        throws IntegrationException {
        final UserView currentUser = blackDuckService.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
        final Set<ProjectView> matchingProjects = projectViews.parallelStream()
                                                      .filter(projectView -> configuredProjects.contains(projectView.getName()))
                                                      .collect(Collectors.toSet());
        for (final ProjectView projectView : matchingProjects) {
            logger.debug("Adding user to Project {}", projectView.getName());
            projectUsersService.addUserToProject(projectView, currentUser);
        }
    }

}
