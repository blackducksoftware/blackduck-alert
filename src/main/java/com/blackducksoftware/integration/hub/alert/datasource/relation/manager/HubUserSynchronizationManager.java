/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.datasource.relation.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HubUsersEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HubUsersRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserProjectVersionsRelationPK;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserProjectVersionsRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.model.HubUsersConfigWrapper;
import com.blackducksoftware.integration.hub.alert.web.model.ProjectVersionConfigWrapper;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.model.view.AssignedUserView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

@Component
public class HubUserSynchronizationManager implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(HubUserSynchronizationManager.class);
    private final GlobalProperties globalProperties;
    private final HubUsersRepository hubUsersRepository;
    private final HubUserProjectVersionsRepository hubUserProjectVersionsRepository;
    private final HubUserManager hubUserManager;
    private final TaskScheduler taskScheduler;

    private ScheduledFuture<?> future;

    @Autowired
    public HubUserSynchronizationManager(final GlobalProperties globalProperties, final HubUsersRepository hubUsersRepository, final HubUserProjectVersionsRepository hubUserProjectVersionsRepository, final HubUserManager hubUserManager,
            final TaskScheduler taskScheduler) {
        this.globalProperties = globalProperties;
        this.hubUsersRepository = hubUsersRepository;
        this.hubUserProjectVersionsRepository = hubUserProjectVersionsRepository;
        this.hubUserManager = hubUserManager;
        this.taskScheduler = taskScheduler;
    }

    public void scheduleJobExecution(final String cron) {
        if (StringUtils.isNotBlank(cron)) {
            try {
                final CronTrigger cronTrigger = new CronTrigger(cron, TimeZone.getTimeZone("UTC"));
                if (future != null) {
                    future.cancel(false);
                }
                logger.info("Scheduling " + this.getClass().getSimpleName() + " with cron : " + cron);
                future = taskScheduler.schedule(this, cronTrigger);
            } catch (final IllegalArgumentException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void run() {
        HubServicesFactory hubServicesFactory;
        try {
            hubServicesFactory = globalProperties.createHubServicesFactory(logger);
        } catch (final EncryptionException e) {
            logger.error("Unable to create Hub services factory.", e);
            return;
        }

        final List<ProjectView> hubServerProjects = getHubProjects(hubServicesFactory.createProjectRequestService());
        final List<UserView> hubServerUsers = getHubServerUsernames(hubServicesFactory.createUserRequestService());
        final Map<String, HubUsersEntity> localUsernames = getLocalUsernames();

        hubServerUsers.forEach(serverUser -> {
            if (localUsernames.containsKey(serverUser.userName)) {
                final HubUsersEntity hubUsersEntity = localUsernames.get(serverUser.userName);
                if (Boolean.TRUE.equals(serverUser.active)) {
                    // TODO synchronizeUserWithHubServer(hubUsersEntity, hubServerProjects, hubServicesFactory.createProjectAssignmentRequestService());
                    synchronizeUserWithHubServer(hubUsersEntity, hubServerProjects, hubServicesFactory.createProjectDataService());
                } else if (!Boolean.FALSE.equals(hubUsersEntity.getExistsOnHub())) {
                    final HubUsersEntity newEntity = new HubUsersEntity(hubUsersEntity.getUsername(), Boolean.FALSE);
                    newEntity.setId(hubUsersEntity.getId());
                    hubUsersRepository.save(newEntity);
                }
            } else {
                final HubUsersConfigWrapper newWrapper = new HubUsersConfigWrapper(null, serverUser.userName, "DAILY", null, null, null, serverUser.active.toString(), null);
                try {
                    hubUserManager.saveConfig(newWrapper);
                } catch (final AlertException e) {
                    logger.error("Error saving a new Hub user configuration.", e);
                }
            }
        });

    }

    // TODO private void synchronizeUserWithHubServer(final HubUsersEntity oldEntity, final List<ProjectView> hubServerProjectList, final ProjectAssignmentRequestService projectAssignmentRequestService) {
    private void synchronizeUserWithHubServer(final HubUsersEntity oldEntity, final List<ProjectView> hubServerProjectList, final ProjectDataService projectDataService) {
        final List<ProjectVersionConfigWrapper> projectVersionsConfiguredForUser = hubUserManager.getProjectVersions(oldEntity.getId());

        // for (final ProjectView project : hubServerProjectList) {
        // try {
        // final List<AssignedUserView> projectUsers = projectAssignmentRequestService.getProjectUsers(project);
        // if (isUserConfigured(oldEntity.getUsername(), projectUsers)) {
        // // TODO
        // }
        // } catch (final IntegrationException e) {
        // logger.error("Could not get users from {}: {}", project.name, e);
        // }
        // }

        for (final ProjectVersionConfigWrapper projectVersionWrapper : projectVersionsConfiguredForUser) {
            try {
                projectDataService.getProjectVersion(projectVersionWrapper.getProjectName(), projectVersionWrapper.getProjectVersionName());
                final List<AssignedUserView> usersList = projectDataService.getAssignedUsersToProject(projectVersionWrapper.getProjectName());
                if (isUserConfigured(oldEntity.getUsername(), usersList)) {
                    continue;
                }
            } catch (final IntegrationException e) {
                logger.error("Could not get the project and version {} > {} from the Hub server for {}: {}", projectVersionWrapper.getProjectName(), projectVersionWrapper.getProjectVersionName(), oldEntity.getUsername(), e);
            }
            deleteHubUserProjectVersionRelation(oldEntity.getId(), projectVersionWrapper);
        }
    }

    private List<ProjectView> getHubProjects(final ProjectRequestService projectRequestService) {
        try {
            final List<ProjectView> hubServerProjects = projectRequestService.getAllProjects();
            return hubServerProjects;
        } catch (final IntegrationException e) {
            logger.error("There was a problem getting the projects from the Hub server.", e);
        }
        return Collections.emptyList();
    }

    private boolean isUserConfigured(final String username, final List<AssignedUserView> usersList) {
        if (username != null) {
            for (final AssignedUserView user : usersList) {
                if (username.equals(user.name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, HubUsersEntity> getLocalUsernames() {
        final List<HubUsersEntity> hubUserEntities = hubUsersRepository.findAll();
        final Map<String, HubUsersEntity> map = new HashMap<>();
        hubUserEntities.forEach(entity -> {
            map.put(entity.getUsername(), entity);
        });
        return map;
    }

    private List<UserView> getHubServerUsernames(final UserRequestService userRequestService) {
        List<UserView> userList;
        try {
            userList = userRequestService.getAllUsers();
        } catch (final IntegrationException e) {
            logger.error("Unable to get list of users from the hub.", e);
            return Collections.emptyList();
        }
        return userList;
    }

    private void deleteHubUserProjectVersionRelation(final Long id, final ProjectVersionConfigWrapper projectVersionWrapper) {
        final HubUserProjectVersionsRelationPK key = new HubUserProjectVersionsRelationPK();
        key.userConfigId = id;
        key.projectName = projectVersionWrapper.getProjectName();
        key.projectVersionName = projectVersionWrapper.getProjectVersionName();
        hubUserProjectVersionsRepository.delete(key);
    }

}
