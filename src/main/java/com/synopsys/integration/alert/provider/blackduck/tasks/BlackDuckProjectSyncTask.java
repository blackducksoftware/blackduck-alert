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
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.database.api.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.api.ProviderProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.api.ProviderUserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelation;
import com.synopsys.integration.alert.database.provider.user.ProviderUserEntity;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.model.BlackDuckProject;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectUsersService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckProjectSyncTask extends ScheduledTask {
    public static final String TASK_NAME = "blackduck-sync-project-task";
    private final Logger logger = LoggerFactory.getLogger(BlackDuckProjectSyncTask.class);
    private final BlackDuckProperties blackDuckProperties;
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    private final ProviderProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    private final ProviderUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;

    @Autowired
    public BlackDuckProjectSyncTask(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor,
        final ProviderProjectRepositoryAccessor blackDuckProjectRepositoryAccessor, final ProviderUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor) {
        super(taskScheduler, TASK_NAME);
        this.blackDuckProperties = blackDuckProperties;
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
        this.userProjectRelationRepositoryAccessor = userProjectRelationRepositoryAccessor;
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
                final ProjectUsersService projectUsersService = blackDuckServicesFactory.createProjectUsersService();
                final List<ProjectView> projectViews = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
                final Map<BlackDuckProject, ProjectView> currentDataMap = getCurrentData(projectViews, blackDuckService);
                final List<ProviderProjectEntity> blackDuckProjectEntities = updateProjectDB(currentDataMap.keySet());

                final Map<Long, Set<String>> projectToEmailAddresses = getEmailsPerProject(currentDataMap, blackDuckProjectEntities, projectUsersService);
                final Set<String> emailAddresses = new HashSet<>();
                projectToEmailAddresses.forEach((projectId, emails) -> emailAddresses.addAll(emails));

                updateUserDB(emailAddresses);
                final List<ProviderUserEntity> blackDuckUserEntities = blackDuckUserRepositoryAccessor.readEntities();

                updateUserProjectRelations(projectToEmailAddresses, blackDuckUserEntities);
            } else {
                logger.error("Missing BlackDuck global configuration.");
            }
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

    public List<ProviderProjectEntity> updateProjectDB(final Set<BlackDuckProject> currentProjects) {
        final List<ProviderProjectEntity> blackDuckProjectEntities = currentProjects
                                                                         .stream()
                                                                         .map(blackDuckProject -> new ProviderProjectEntity(blackDuckProject.getName(),
                                                                             blackDuckProject.getDescription(),
                                                                             blackDuckProject.getHref(),
                                                                             blackDuckProject.getProjectOwnerEmail(),
                                                                             BlackDuckProvider.COMPONENT_NAME))
                                                                         .collect(Collectors.toList());
        logger.info("{} projects", blackDuckProjectEntities.size());
        return blackDuckProjectRepositoryAccessor.deleteAndSaveAll(blackDuckProjectEntities);
    }

    private Map<Long, Set<String>> getEmailsPerProject(final Map<BlackDuckProject, ProjectView> currentDataMap, final List<ProviderProjectEntity> blackDuckProjectEntities, final ProjectUsersService projectUsersService) {
        final Map<Long, Set<String>> projectToEmailAddresses = new ConcurrentHashMap<>();
        currentDataMap.entrySet()
            .parallelStream()
            .forEach(entry -> {
                try {
                    final BlackDuckProject blackDuckProject = entry.getKey();
                    final ProjectView projectView = entry.getValue();
                    final Optional<ProviderProjectEntity> optionalBlackDuckProjectEntity = blackDuckProjectEntities
                                                                                               .stream()
                                                                                               .filter(blackDuckProjectEntity -> blackDuckProjectEntity.getName().equals(blackDuckProject.getName()))
                                                                                               .findFirst();
                    if (optionalBlackDuckProjectEntity.isPresent()) {
                        final ProviderProjectEntity projectEntity = optionalBlackDuckProjectEntity.get();

                        final Set<String> projectUserEmailAddresses = projectUsersService.getAllActiveUsersForProject(projectView)
                                                                          .stream()
                                                                          .map(UserView::getEmail)
                                                                          .filter(StringUtils::isNotBlank)
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

        final List<ProviderUserEntity> blackDuckUserEntities = blackDuckUserRepositoryAccessor.readEntities();
        final Set<String> storedEmails = blackDuckUserEntities
                                             .stream()
                                             .map(ProviderUserEntity::getEmailAddress)
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

        final List<ProviderUserEntity> blackDuckUsersToRemove = blackDuckUserEntities
                                                                    .stream()
                                                                    .filter(blackDuckUserEntity -> emailsToRemove.contains(blackDuckUserEntity.getEmailAddress()))
                                                                    .collect(Collectors.toList());

        final List<ProviderUserEntity> blackDuckUserEntityList = emailsToAdd
                                                                     .stream()
                                                                     .map(email -> new ProviderUserEntity(email, false, BlackDuckProvider.COMPONENT_NAME))
                                                                     .collect(Collectors.toList());
        blackDuckUserRepositoryAccessor.deleteAndSaveAll(blackDuckUsersToRemove, blackDuckUserEntityList);
    }

    private void updateUserProjectRelations(final Map<Long, Set<String>> projectToEmailAddresses, final List<ProviderUserEntity> blackDuckUserEntities) {
        final Map<String, Long> emailToUserId = blackDuckUserEntities
                                                    .stream()
                                                    .collect(Collectors.toMap(ProviderUserEntity::getEmailAddress, ProviderUserEntity::getId));
        final Set<ProviderUserProjectRelation> userProjectRelations = new HashSet<>();
        projectToEmailAddresses.forEach((projectId, emails) -> emails.forEach(email -> userProjectRelations.add(new ProviderUserProjectRelation(emailToUserId.get(email), projectId))));
        logger.info("User to project relationships {}", userProjectRelations.size());
        userProjectRelationRepositoryAccessor.deleteAndSaveAll(userProjectRelations);
    }

}
