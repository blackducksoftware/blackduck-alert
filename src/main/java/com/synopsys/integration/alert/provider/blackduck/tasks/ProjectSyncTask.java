/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import java.io.IOException;
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

import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.model.BlackDuckProject;
import com.synopsys.integration.alert.workflow.scheduled.ScheduledTask;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class ProjectSyncTask extends ScheduledTask {
    private final Logger logger = LoggerFactory.getLogger(ProjectSyncTask.class);
    private final BlackDuckProperties blackDuckProperties;
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    private final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;

    @Autowired
    public ProjectSyncTask(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor,
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor, final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor) {
        super(taskScheduler, "blackduck-sync-project-task");
        this.blackDuckProperties = blackDuckProperties;
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
        this.userProjectRelationRepositoryAccessor = userProjectRelationRepositoryAccessor;
    }

    public void run() {
        logger.info("### Starting {}...", getTaskName());
        try {
            final Optional<BlackduckRestConnection> optionalConnection = blackDuckProperties.createRestConnectionAndLogErrors(logger);
            if (optionalConnection.isPresent()) {
                try (final BlackduckRestConnection restConnection = optionalConnection.get()) {
                    final HubServicesFactory hubServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
                    final HubService hubService = hubServicesFactory.createHubService();
                    final ProjectService projectService = hubServicesFactory.createProjectService();
                    final List<ProjectView> hubViews = hubService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
                    final Map<BlackDuckProject, ProjectView> currentDataMap = getCurrentData(hubViews, hubService);
                    final List<BlackDuckProjectEntity> blackDuckProjectEntities = updateProjectDB(currentDataMap.keySet());

                    final Map<Long, Set<String>> projectToEmailAddresses = getEmailsPerProject(currentDataMap, blackDuckProjectEntities, projectService);
                    final Set<String> emailAddresses = new HashSet<>();
                    projectToEmailAddresses.forEach((projectId, emails) -> emailAddresses.addAll(emails));

                    updateUserDB(emailAddresses);
                    final List<BlackDuckUserEntity> blackDuckUserEntities = (List<BlackDuckUserEntity>) blackDuckUserRepositoryAccessor.readEntities();

                    updateUserProjectRelations(projectToEmailAddresses, blackDuckUserEntities);
                }
            } else {
                logger.error("Missing BlackDuck global configuration.");
            }
        } catch (final IOException | IntegrationException | AlertRuntimeException e) {
            logger.error("Could not retrieve the current data from the BlackDuck server: " + e.getMessage(), e);
        }
        logger.info("### Finished {}...", getTaskName());
    }

    public Map<BlackDuckProject, ProjectView> getCurrentData(final List<ProjectView> projectViews, HubService hubService) {
        final Map<BlackDuckProject, ProjectView> projectMap = new ConcurrentHashMap<>();
        projectViews
            .parallelStream()
            .forEach(projectView -> {
                String projectOwnerEmail = null;
                if (StringUtils.isNotBlank(projectView.projectOwner)) {
                    try {
                        UserView projectOwner = hubService.getResponse(projectView.projectOwner, UserView.class);
                        projectOwnerEmail = projectOwner.email;
                    } catch (IntegrationException e) {
                        logger.error(String.format("Could not get the project owner for Project: %s. Error: %s", projectView.name, e.getMessage()), e);
                    }
                }
                projectMap.put(new BlackDuckProject(projectView.name, StringUtils.trimToEmpty(projectView.description), projectView._meta.href, projectOwnerEmail), projectView);
            });
        return projectMap;
    }

    public List<BlackDuckProjectEntity> updateProjectDB(final Set<BlackDuckProject> currentProjects) {
        final List<BlackDuckProjectEntity> blackDuckProjectEntities = currentProjects
                                                                          .stream()
                                                                          .map(blackDuckProject -> new BlackDuckProjectEntity(blackDuckProject.getName(), blackDuckProject.getDescription(), blackDuckProject.getHref(),
                                                                              blackDuckProject.getProjectOwnerEmail()))
                                                                          .collect(Collectors.toList());
        logger.info("{} projects", blackDuckProjectEntities.size());
        return blackDuckProjectRepositoryAccessor.deleteAndSaveAll(blackDuckProjectEntities);
    }

    private Map<Long, Set<String>> getEmailsPerProject(final Map<BlackDuckProject, ProjectView> currentDataMap, final List<BlackDuckProjectEntity> blackDuckProjectEntities, final ProjectService projectService) {
        final Map<Long, Set<String>> projectToEmailAddresses = new ConcurrentHashMap<>();
        currentDataMap.entrySet()
            .parallelStream()
            .forEach(entry -> {
                try {
                    final BlackDuckProject blackDuckProject = entry.getKey();
                    final ProjectView projectView = entry.getValue();
                    final Optional<BlackDuckProjectEntity> optionalBlackDuckProjectEntity = blackDuckProjectEntities
                                                                                                .stream()
                                                                                                .filter(blackDuckProjectEntity -> blackDuckProjectEntity.getName().equals(blackDuckProject.getName()))
                                                                                                .findFirst();
                    if (optionalBlackDuckProjectEntity.isPresent()) {
                        final BlackDuckProjectEntity projectEntity = optionalBlackDuckProjectEntity.get();

                        final Set<String> projectUserEmailAddresses = projectService.getAllActiveUsersForProject(projectView)
                                                                          .stream()
                                                                          .filter(userView -> StringUtils.isNotBlank(userView.email))
                                                                          .map(userView -> userView.email)
                                                                          .collect(Collectors.toSet());
                        if (StringUtils.isNotBlank(projectEntity.getProjectOwnerEmail())) {
                            projectUserEmailAddresses.add(projectEntity.getProjectOwnerEmail());
                        }
                        projectToEmailAddresses.put(projectEntity.getId(), projectUserEmailAddresses);
                    }
                } catch (final IntegrationException e) {
                    // We do this to break out of the stream
                    throw new AlertRuntimeException(e.getMessage(), e);
                }
            });
        return projectToEmailAddresses;
    }

    private void updateUserDB(final Set<String> userEmailAddresses) {
        final Set<String> emailsToAdd = new HashSet<>();
        final Set<String> emailsToRemove = new HashSet<>();

        final List<BlackDuckUserEntity> blackDuckUserEntities = (List<BlackDuckUserEntity>) blackDuckUserRepositoryAccessor.readEntities();
        final Set<String> storedEmails = blackDuckUserEntities
                                             .stream()
                                             .map(BlackDuckUserEntity::getEmailAddress)
                                             .collect(Collectors.toSet());

        storedEmails.forEach(storedData -> {
            // If the storedData no longer exists in the current then we need to remove the entry
            // If any of the fields have changed in the currentData, then the storedData will not be in the currentData so we will need to remove the old entry
            if (!userEmailAddresses.contains(storedData)) {
                emailsToRemove.add(storedData);
            }
        });
        userEmailAddresses.forEach(currentData -> {
            // If the currentData is not found in the stored data then we will need to add a new entry
            // If any of the fields have changed in the currentData, then it wont be in the stored data so we will need to add a new entry
            if (!storedEmails.contains(currentData)) {
                emailsToAdd.add(currentData);
            }
        });
        logger.info("Adding {} emails", emailsToAdd.size());
        logger.info("Removing {} emails", emailsToRemove.size());

        final List<BlackDuckUserEntity> blackDuckUsersToRemove = blackDuckUserEntities
                                                                     .stream()
                                                                     .filter(blackDuckUserEntity -> emailsToRemove.contains(blackDuckUserEntity.getEmailAddress()))
                                                                     .collect(Collectors.toList());

        final List<BlackDuckUserEntity> blackDuckUserEntityList = emailsToAdd
                                                                      .stream()
                                                                      .map(email -> new BlackDuckUserEntity(email, false))
                                                                      .collect(Collectors.toList());
        blackDuckUserRepositoryAccessor.deleteAndSaveAll(blackDuckUsersToRemove, blackDuckUserEntityList);
    }

    private void updateUserProjectRelations(final Map<Long, Set<String>> projectToEmailAddresses, final List<BlackDuckUserEntity> blackDuckUserEntities) {
        final Map<String, Long> emailToUserId = blackDuckUserEntities
                                                    .stream()
                                                    .collect(Collectors.toMap(BlackDuckUserEntity::getEmailAddress, BlackDuckUserEntity::getId));
        final Set<UserProjectRelation> userProjectRelations = new HashSet<>();
        projectToEmailAddresses.forEach((projectId, emails) -> {
            emails.forEach(email -> userProjectRelations.add(new UserProjectRelation(emailToUserId.get(email), projectId)));
        });
        logger.info("User to project relationships {}", userProjectRelations.size());
        userProjectRelationRepositoryAccessor.deleteAndSaveAll(userProjectRelations);
    }

}
