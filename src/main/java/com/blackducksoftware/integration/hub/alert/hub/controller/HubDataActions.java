/**
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.alert.hub.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.hub.model.HubGroup;
import com.blackducksoftware.integration.hub.alert.hub.model.HubProject;
import com.blackducksoftware.integration.hub.api.group.GroupService;
import com.blackducksoftware.integration.hub.api.project.ProjectService;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.UserGroupView;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

@Component
public class HubDataActions {
    private final Logger logger = LoggerFactory.getLogger(HubDataActions.class);
    private final GlobalProperties globalProperties;

    @Autowired
    public HubDataActions(final GlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    public List<HubGroup> getHubGroups() throws IntegrationException {
        final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactory(logger);
        if (hubServicesFactory != null) {
            final GroupService groupService = hubServicesFactory.createGroupService();
            final List<UserGroupView> rawGroups = groupService.getAllGroups();

            final List<HubGroup> groups = new ArrayList<>();
            for (final UserGroupView userGroupView : rawGroups) {
                final HubGroup hubGroup = new HubGroup();
                hubGroup.active = userGroupView.active;
                hubGroup.name = userGroupView.name;
                hubGroup.url = userGroupView.meta.href;
                groups.add(hubGroup);
            }
            return groups;
        } else {
            throw new AlertException("Missing global configuration.");
        }
    }

    public List<HubProject> getHubProjects() throws IntegrationException {
        final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactory(logger);
        if (hubServicesFactory != null) {
            final ProjectService projectRequestService = hubServicesFactory.createProjectService();
            final List<ProjectView> rawProjects = projectRequestService.getAllProjects();

            final List<HubProject> projects = new ArrayList<>();
            for (final ProjectView projectView : rawProjects) {
                final HubProject project = new HubProject();
                project.name = projectView.name;
                projects.add(project);
            }
            return projects;
        } else {
            throw new AlertException("Missing global configuration.");
        }
    }

}
