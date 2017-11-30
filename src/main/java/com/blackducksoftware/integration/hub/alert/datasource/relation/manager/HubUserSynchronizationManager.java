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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HubUsersEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HubUsersRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserProjectVersionsRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.model.HubUsersConfigWrapper;
import com.blackducksoftware.integration.hub.alert.web.model.ProjectVersionConfigWrapper;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.dataservice.user.UserDataService;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

@Component
public class HubUserSynchronizationManager implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(HubUserSynchronizationManager.class);
    private final GlobalProperties globalProperties;
    private final HubUsersRepository hubUsersRepository;
    final HubUserProjectVersionsRepository hubUserProjectVersionsRepository;
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
            if (hubServicesFactory == null) {
                throw new IntegrationException("The HubServicesFactory object was null.");
            }
        } catch (final IntegrationException e) {
            logger.error("There was a problem when creating the HubServicesFactory.", e);
            return;
        }

        final List<UserView> hubServerUsers = getHubServerUsers(hubServicesFactory.createUserRequestService());
        if (hubServerUsers != null) {
            final Map<String, HubUsersEntity> localUsernamesMap = getLocalUsernames();
            synchronizeUsersWithHubServer(hubServerUsers, localUsernamesMap, hubServicesFactory.createUserDataService(), hubServicesFactory.createProjectVersionRequestService());
        } else {
            logger.error("There was a problem getting the Hub Server Users. Cannot synchronize the local data with the Hub.");
        }
    }

    public void synchronizeUsersWithHubServer(final List<UserView> hubServerUsers, final Map<String, HubUsersEntity> localUsernamesMap, final UserDataService userDataService,
            final ProjectVersionRequestService projectVersionRequestService) {
        final List<HubUsersEntity> usersThatDoNotExist = new ArrayList<>();
        final List<String> hubServerUsernames = hubServerUsers.stream().map(user -> user.userName).collect(Collectors.toList());
        localUsernamesMap.keySet().forEach(localUsername -> {
            if (!hubServerUsernames.contains(localUsername)) {
                usersThatDoNotExist.add(localUsernamesMap.get(localUsername));
            }
        });

        // Delete local users if there is no configuration for them, or deactivate them if there is.
        usersThatDoNotExist.forEach(user -> {
            if (!hubUserManager.hasChannelConfiguration(user.getId())) {
                hubUserManager.deleteConfig(user.getId());
                localUsernamesMap.remove(user.getUsername());
            } else if (Boolean.TRUE.equals(user.getActive())) {
                final HubUsersEntity newEntity = new HubUsersEntity(user.getUsername(), Boolean.FALSE);
                newEntity.setId(user.getId());
                hubUsersRepository.save(newEntity);
            }
        });

        // If a Hub users is active, synchronize the local user with it, otherwise deactivate the local user (if not already inactive).
        hubServerUsers.forEach(serverUser -> {
            final HubUsersEntity hubUsersEntity = getHubUsersEntityOrCreateIfNeeded(localUsernamesMap, serverUser);
            if (Boolean.TRUE.equals(serverUser.active)) {
                synchronizeUserProjectVersionsWithHubServer(hubUsersEntity, userDataService, projectVersionRequestService);
            } else if (!Boolean.FALSE.equals(hubUsersEntity.getActive())) {
                final HubUsersEntity newEntity = new HubUsersEntity(hubUsersEntity.getUsername(), Boolean.FALSE);
                newEntity.setId(hubUsersEntity.getId());
                hubUsersRepository.save(newEntity);
            }
        });
    }

    private HubUsersEntity getHubUsersEntityOrCreateIfNeeded(final Map<String, HubUsersEntity> localUsernames, final UserView serverUser) {
        if (!localUsernames.containsKey(serverUser.userName)) {
            final String isActive = hubUserManager.getObjectTransformer().objectToString(serverUser.active);
            final HubUsersConfigWrapper newWrapper = new HubUsersConfigWrapper(null, serverUser.userName, "DAILY", null, null, null, isActive, null);
            try {
                final Long savedId = hubUserManager.saveConfig(newWrapper);
                final HubUsersEntity savedEntity = hubUsersRepository.findOne(savedId);
                localUsernames.put(serverUser.userName, savedEntity);
                return savedEntity;
            } catch (final AlertException e) {
                logger.error("Error saving a new Hub user configuration.", e);
            }
        }
        return localUsernames.get(serverUser.userName);
    }

    private void synchronizeUserProjectVersionsWithHubServer(final HubUsersEntity oldEntity, final UserDataService userDataService, final ProjectVersionRequestService projectVersionRequestService) {
        final List<ProjectVersionConfigWrapper> localProjectVersions = hubUserManager.getProjectVersions(oldEntity.getId());
        final List<ProjectVersionConfigWrapper> remoteProjectVersions = getHubProjectVersionsForUser(oldEntity.getUsername(), userDataService, projectVersionRequestService);

        final List<ProjectVersionConfigWrapper> staleConfigs = getItemsFromFirstListMissingFromSecondList(localProjectVersions, remoteProjectVersions);
        staleConfigs.forEach(config -> {
            hubUserManager.deleteHubUserProjectVersionRelation(oldEntity.getId(), config);
        });

        if (localProjectVersions.size() != remoteProjectVersions.size()) {
            final List<ProjectVersionConfigWrapper> missingConfigs = getItemsFromFirstListMissingFromSecondList(remoteProjectVersions, localProjectVersions);
            hubUserManager.saveProjectVersionsForUser(oldEntity.getId(), missingConfigs);
        }
    }

    private List<ProjectVersionConfigWrapper> getHubProjectVersionsForUser(final String username, final UserDataService userDataService, final ProjectVersionRequestService projectVersionRequestService) {
        try {
            final List<ProjectView> projects = userDataService.getProjectsForUser(username);
            final List<ProjectVersionConfigWrapper> versionWrappers = new ArrayList<>();

            for (final ProjectView project : projects) {
                final List<ProjectVersionView> projectVersions = projectVersionRequestService.getAllProjectVersions(project);
                if (projectVersions != null) {
                    for (final ProjectVersionView projectVersion : projectVersions) {
                        versionWrappers.add(new ProjectVersionConfigWrapper(project.name, projectVersion.versionName, Boolean.FALSE.toString()));
                    }
                }
            }
            return versionWrappers;
        } catch (final IntegrationException e) {
            logger.error("Unable to retrieve projects and/or versions for {}: {}", username, e);
        }
        return Collections.emptyList();
    }

    private List<ProjectVersionConfigWrapper> getItemsFromFirstListMissingFromSecondList(final List<ProjectVersionConfigWrapper> firstList, final List<ProjectVersionConfigWrapper> secondList) {
        final List<ProjectVersionConfigWrapper> missingItems = new ArrayList<>();
        boolean found = false;
        for (final ProjectVersionConfigWrapper firstItem : firstList) {
            for (final ProjectVersionConfigWrapper secondItem : secondList) {
                if (firstItem.getProjectName().equals(secondItem.getProjectName())) {
                    if (firstItem.getProjectVersionName().equals(secondItem.getProjectVersionName())) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                missingItems.add(firstItem);
            }
        }
        return missingItems;
    }

    private Map<String, HubUsersEntity> getLocalUsernames() {
        final List<HubUsersEntity> hubUserEntities = hubUsersRepository.findAll();
        final Map<String, HubUsersEntity> map = new HashMap<>();
        hubUserEntities.forEach(entity -> {
            map.put(entity.getUsername(), entity);
        });
        return map;
    }

    private List<UserView> getHubServerUsers(final UserRequestService userRequestService) {
        List<UserView> userList;
        try {
            userList = userRequestService.getAllUsers();
        } catch (final IntegrationException e) {
            logger.error("Unable to get list of users from the hub.", e);
            return null;
        }
        return userList;
    }

}
