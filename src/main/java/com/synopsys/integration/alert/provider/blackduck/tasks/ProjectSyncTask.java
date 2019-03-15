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
package com.synopsys.integration.alert.provider.blackduck.tasks;

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

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.model.BlackDuckProject;
import com.synopsys.integration.alert.workflow.scheduled.ScheduledTask;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class ProjectSyncTask extends ScheduledTask {
    public static final String TASK_NAME = "blackduck-sync-project-task";
    private final Logger logger = LoggerFactory.getLogger(ProjectSyncTask.class);
    private final BlackDuckProperties blackDuckProperties;
    private final ProjectSyncDatabase projectSyncDatabase;

    @Autowired
    public ProjectSyncTask(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final ProjectSyncDatabase projectSyncDatabase) {
        super(taskScheduler, TASK_NAME);
        this.blackDuckProperties = blackDuckProperties;
        this.projectSyncDatabase = projectSyncDatabase;
    }

    @Override
    public void run() {
        logger.info("### Starting {}...", getTaskName());
        try {
            final Optional<BlackDuckHttpClient> optionalBlackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger);
            if (optionalBlackDuckHttpClient.isPresent()) {
                final BlackDuckHttpClient blackDuckHttpClient = optionalBlackDuckHttpClient.get();
                final BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new Slf4jIntLogger(logger));
                final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
                final ProjectService projectService = blackDuckServicesFactory.createProjectService();
                final List<ProjectView> projectViews = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
                final Map<BlackDuckProject, ProjectView> currentDataMap = getCurrentData(projectViews, blackDuckService);

                final Map<String, Set<String>> projectToEmailAddresses = getEmailsPerProject(currentDataMap, projectService);
                final Set<BlackDuckProject> blackDuckProjects = currentDataMap.keySet();
                projectSyncDatabase.databaseUpdates(blackDuckProjects, projectToEmailAddresses);
            } else {
                logger.error("Missing BlackDuck global configuration.");
            }
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("There was an issue saving the Black Duck project/user data: " + e.getMessage(), e);
        } catch (final IntegrationException | AlertRuntimeException e) {
            logger.error("Could not retrieve the current data from the BlackDuck server: " + e.getMessage(), e);
        }
        logger.info("### Finished {}...", getTaskName());
    }

    public Map<BlackDuckProject, ProjectView> getCurrentData(final List<ProjectView> projectViews, final BlackDuckService blackDuckService) {
        final Map<BlackDuckProject, ProjectView> projectMap = new ConcurrentHashMap<>();
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
                projectMap.put(new BlackDuckProject(projectView.getName(), StringUtils.trimToEmpty(projectView.getDescription()), projectView.getMeta().getHref(), projectOwnerEmail), projectView);
            });
        return projectMap;
    }

    private Map<String, Set<String>> getEmailsPerProject(final Map<BlackDuckProject, ProjectView> currentDataMap, final ProjectService projectService) {
        final Map<String, Set<String>> projectToEmailAddresses = new ConcurrentHashMap<>();
        currentDataMap.entrySet()
            .parallelStream()
            .forEach(entry -> {
                try {
                    final BlackDuckProject blackDuckProject = entry.getKey();
                    final ProjectView projectView = entry.getValue();
                    final Set<String> projectUserEmailAddresses = projectService.getAllActiveUsersForProject(projectView)
                                                                      .stream()
                                                                      .filter(userView -> StringUtils.isNotBlank(userView.getEmail()))
                                                                      .map(userView -> userView.getEmail())
                                                                      .collect(Collectors.toSet());
                    if (StringUtils.isNotBlank(blackDuckProject.getProjectOwnerEmail())) {
                        projectUserEmailAddresses.add(blackDuckProject.getProjectOwnerEmail());
                    }
                    projectToEmailAddresses.put(blackDuckProject.getName(), projectUserEmailAddresses);

                } catch (final IntegrationException e) {
                    // We do this to break out of the stream
                    throw new AlertRuntimeException(e.getMessage(), e);
                }
            });
        return projectToEmailAddresses;
    }

}
