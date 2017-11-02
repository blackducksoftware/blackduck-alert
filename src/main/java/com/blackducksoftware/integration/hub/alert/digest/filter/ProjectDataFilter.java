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
package com.blackducksoftware.integration.hub.alert.digest.filter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.UserConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.UserConfigRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.model.view.UserView;

public class ProjectDataFilter {
    private final Logger logger = LoggerFactory.getLogger(ProjectDataFilter.class);

    private final UserConfigRepository userRepo;
    private final UserRequestService userRequestService;
    private final ProjectDataService projectDataService;
    private final MetaService metaService;

    @Autowired
    public ProjectDataFilter(final UserConfigRepository userRepo, final UserRequestService userRequestService, final ProjectDataService projectDataService, final MetaService metaService) {
        this.userRepo = userRepo;
        this.userRequestService = userRequestService;
        this.projectDataService = projectDataService;
        this.metaService = metaService;
    }

    public Set<UserNotificationWrapper> filterUserNotifications(final Collection<ProjectData> notificationData) {
        final List<UserConfigEntity> userConfigs = userRepo.findAll();
        final Set<UserNotificationWrapper> configNotifications = new HashSet<>();
        if (!userConfigs.isEmpty()) {
            final Map<ProjectVersionWrapper, ProjectData> projectToNotificationMap = mapProjectsToNotifications(notificationData);
            final Map<ProjectVersionWrapper, Collection<UserView>> projectToUsersMap = mapProjectsToUsers(projectToNotificationMap.keySet());

            // Flatten the Collection of Collections of UserViews to a Set of UserViews
            final Collection<Collection<UserView>> usersPerProject = projectToUsersMap.values();
            final Set<UserView> allNotificationUsers = usersPerProject.parallelStream().flatMap(Collection::stream).collect(Collectors.toSet());

            userConfigs.forEach(config -> {
                final UserView matchedUser = getHubUserMatchingConfiguredUser(allNotificationUsers, config.getUsername());
                final Set<ProjectData> configData = filterNotificationsByProjectVersionUser(projectToNotificationMap, projectToUsersMap, matchedUser);
                final UserNotificationWrapper configNotification = new UserNotificationWrapper(config.getId(), configData);
                configNotifications.add(configNotification);
            });
        }
        return configNotifications;
    }

    private Map<ProjectVersionWrapper, ProjectData> mapProjectsToNotifications(final Collection<ProjectData> projectData) {
        final Map<ProjectVersionWrapper, ProjectData> projects = new HashMap<>();
        projectData.forEach(item -> {
            try {
                final ProjectVersionWrapper projectVersion = projectDataService.getProjectVersion(item.getProjectName(), item.getProjectVersion());
                projects.put(projectVersion, item);
            } catch (final IntegrationException e) {
                logger.error("Something went wrong trying to get " + item.getProjectName() + " > " + item.getProjectVersion() + " from the Hub", e);
            }
        });
        return projects;
    }

    private Map<ProjectVersionWrapper, Collection<UserView>> mapProjectsToUsers(final Collection<ProjectVersionWrapper> projectVersions) {
        final Map<ProjectVersionWrapper, Collection<UserView>> projectMap = new HashMap<>();
        projectVersions.forEach(projectVersion -> {
            try {
                final String usersLink = metaService.getFirstLink(projectVersion.getProjectView(), MetaService.USERS_LINK);
                final List<UserView> projectUsers = userRequestService.getAllItems(usersLink, UserView.class);
                projectMap.put(projectVersion, projectUsers);
            } catch (final IntegrationException e) {
                logger.error("Something went wrong trying to get the users from the Hub", e);
            }
        });
        return projectMap;
    }

    private UserView getHubUserMatchingConfiguredUser(final Collection<UserView> hubUsers, final String alertUser) {
        return hubUsers.stream().filter(user -> alertUser.equals(user.userName)).findFirst().get();
    }

    private Set<ProjectData> filterNotificationsByProjectVersionUser(final Map<ProjectVersionWrapper, ProjectData> projectToNotificationMap, final Map<ProjectVersionWrapper, Collection<UserView>> projectToUsersMap,
            final UserView configuredUser) {
        final Set<ProjectData> notificationDataFromProjects = new HashSet<>();
        projectToUsersMap.forEach((projectVersion, projectUsers) -> {
            projectUsers.forEach(user -> {
                if (configuredUser.equals(user)) {
                    notificationDataFromProjects.add(projectToNotificationMap.get(projectVersion));
                }
            });
        });
        return notificationDataFromProjects;
    }

}
