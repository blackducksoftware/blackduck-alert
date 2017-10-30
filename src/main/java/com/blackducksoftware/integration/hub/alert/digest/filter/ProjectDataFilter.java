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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailRepository;
import com.blackducksoftware.integration.hub.alert.datasource.repository.GlobalRepository;
import com.blackducksoftware.integration.hub.alert.datasource.repository.HipChatRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.UserView;

/*
 * Approach #2
 *
 * I think this way will be significantly better, but much more difficult to abstract.
 */
public class ProjectDataFilter {
    private final Logger logger = LoggerFactory.getLogger(ProjectDataFilter.class);

    private final GlobalRepository globalRepo;
    private final EmailRepository emailRepo;
    private final HipChatRepository hipChatRepo;
    private final UserRequestService userRequestService;
    private final ProjectRequestService projectRequestService;
    private final MetaService metaService;

    @Autowired
    public ProjectDataFilter(final GlobalRepository globalRepo, final EmailRepository emailRepo, final HipChatRepository hipChatRepo, final UserRequestService userRequestService, final ProjectRequestService projectRequestService,
            final MetaService metaService) {
        this.globalRepo = globalRepo;
        this.emailRepo = emailRepo;
        this.hipChatRepo = hipChatRepo;
        this.userRequestService = userRequestService;
        this.projectRequestService = projectRequestService;
        this.metaService = metaService;
    }

    // Approach #2a
    public Set<ProjectView> getProjectsFromNotifications(final Collection<ProjectData> projectData) {
        final Set<ProjectView> projects = new HashSet<>();
        projectData.forEach(item -> {
            try {
                final ProjectView project = projectRequestService.getProjectByName(item.getProjectName());
                projects.add(project);
            } catch (final IntegrationException e) {
                logger.error("Something went wrong trying to get the projects from the Hub", e);
            }
        });
        return projects;
    }

    public Set<UserView> getUsersFromProjects(final Collection<ProjectView> projects) {
        final Set<UserView> users = new HashSet<>();
        projects.forEach(project -> {
            try {
                final String usersLink = metaService.getFirstLink(project, MetaService.USERS_LINK);
                final List<UserView> projectUsers = userRequestService.getAllItems(usersLink, UserView.class);
                users.addAll(projectUsers);
            } catch (final IntegrationException e) {
                logger.error("Something went wrong trying to get the users from the Hub", e);
            }
        });
        return users;
    }

    // Approach #2b
    public Set<UserView> getConfiguredHubUsers(final Collection<String> hubUserNames) {
        try {
            final List<UserView> allHubUsers = userRequestService.getAllUsers();
            final Set<UserView> configuredUsers = new HashSet<>();
            allHubUsers.forEach(user -> {
                if (hubUserNames.contains(user.userName)) {
                    configuredUsers.add(user);
                }
            });
            return configuredUsers;
        } catch (final IntegrationException e) {
            logger.error("Something went wrong trying to get the users from the Hub", e);
        }
        return Collections.emptySet();
    }

    // TODO
    public Set<ProjectVersionModel> getProjectsForUser(final Collection<UserView> configuredUsers) {
        // TODO reverse the above
        return Collections.emptySet();
    }
}
