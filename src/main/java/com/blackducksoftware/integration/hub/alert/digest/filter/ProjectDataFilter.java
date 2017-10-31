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

import java.util.ArrayList;
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
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailRepository;
import com.blackducksoftware.integration.hub.alert.datasource.repository.GlobalRepository;
import com.blackducksoftware.integration.hub.alert.datasource.repository.HipChatRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.model.view.UserView;

/*
 * Approach #2
 *
 * I think this way will be significantly better, but much more difficult to abstract.
 */
public class ProjectDataFilter {
    private final Logger logger = LoggerFactory.getLogger(ProjectDataFilter.class);

    private final GlobalRepository globalRepo;
    private final EmailRepository emailRepo; // TODO remove unused variable
    private final HipChatRepository hipChatRepo; // TODO remove unused variable
    private final UserRequestService userRequestService;
    private final ProjectDataService projectDataService;
    private final MetaService metaService;

    @Autowired
    public ProjectDataFilter(final GlobalRepository globalRepo, final EmailRepository emailRepo, final HipChatRepository hipChatRepo, final UserRequestService userRequestService, final ProjectDataService projectDataService,
            final MetaService metaService) {
        this.globalRepo = globalRepo;
        this.emailRepo = emailRepo;
        this.hipChatRepo = hipChatRepo;
        this.userRequestService = userRequestService;
        this.projectDataService = projectDataService;
        this.metaService = metaService;
    }

    public Set<ProjectData> filterNotifications(final Collection<ProjectData> notificationData) {
        final Map<ProjectVersionWrapper, ProjectData> projectToNotificationMap = mapProjectsToNotifications(notificationData);
        final Map<ProjectVersionWrapper, Collection<UserView>> projectToUsersMap = mapProjectsToUsers(projectToNotificationMap.keySet());
        final Collection<Collection<UserView>> usersPerProject = projectToUsersMap.values();
        final Set<UserView> allNotificationUsers = usersPerProject.stream().flatMap(Collection::stream).collect(Collectors.toSet());
        final Set<UserView> configuredUsers = getHubUsersMatchingConfiguredUsers(allNotificationUsers);

        return filterNotificationsByProjectUsers(projectToNotificationMap, projectToUsersMap, configuredUsers);
    }

    public Set<UserView> flatten(final Collection<Collection<UserView>> outerCollection) {
        final Set<UserView> uniqueUsers = new HashSet<>();
        for (final Collection<UserView> innerCollection : outerCollection) {
            for (final UserView user : innerCollection) {
                uniqueUsers.add(user);
            }
        }
        return uniqueUsers;
    }

    public Map<ProjectVersionWrapper, ProjectData> mapProjectsToNotifications(final Collection<ProjectData> projectData) {
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

    public Map<ProjectVersionWrapper, Collection<UserView>> mapProjectsToUsers(final Collection<ProjectVersionWrapper> projectVersions) {
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

    public Set<UserView> getHubUsersMatchingConfiguredUsers(final Collection<UserView> users) {
        final Set<UserView> matchingUsers = new HashSet<>();
        // TODO get users from globalConfig
        // final GlobalConfigEntity globalConfig = globalRepo.getOne(new Long(1));
        final List<String> globalConfigUsers = new ArrayList<>();
        users.forEach(user -> {
            if (globalConfigUsers.contains(user.userName)) {
                matchingUsers.add(user);
            }
        });
        return matchingUsers;
    }

    public Set<ProjectData> filterNotificationsByProjectUsers(final Map<ProjectVersionWrapper, ProjectData> projectToNotificationMap, final Map<ProjectVersionWrapper, Collection<UserView>> projectToUsersMap,
            final Collection<UserView> configuredUsers) {
        final Set<ProjectData> notificationDataFromProjects = new HashSet<>();
        projectToUsersMap.forEach((projectVersion, projectUsers) -> {
            projectUsers.forEach(user -> {
                if (configuredUsers.contains(user)) {
                    notificationDataFromProjects.add(projectToNotificationMap.get(projectVersion));
                }
            });
        });
        return notificationDataFromProjects;
    }

}
