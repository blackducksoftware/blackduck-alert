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
package com.blackducksoftware.integration.alert.provider.hub.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.provider.hub.model.HubGroup;
import com.blackducksoftware.integration.alert.provider.hub.model.HubProject;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.UserGroupView;
import com.blackducksoftware.integration.hub.rest.BlackduckRestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Component
public class HubDataActions {
    private final Logger logger = LoggerFactory.getLogger(HubDataActions.class);
    private final GlobalProperties globalProperties;

    @Autowired
    public HubDataActions(final GlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    public List<HubGroup> getHubGroups() throws IntegrationException {
        try (final BlackduckRestConnection restConnection = globalProperties.createRestConnectionAndLogErrors(logger)) {
            if (restConnection != null) {
                final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactory(restConnection, new Slf4jIntLogger(logger));
                final List<UserGroupView> rawGroups = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.USERGROUPS_LINK_RESPONSE);

                final List<HubGroup> groups = new ArrayList<>();
                for (final UserGroupView userGroupView : rawGroups) {
                    final HubGroup hubGroup = new HubGroup(userGroupView.name, userGroupView.active, userGroupView._meta.href);
                    groups.add(hubGroup);
                }
                return groups;
            } else {
                throw new AlertException("Missing global configuration.");
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    public List<HubProject> getHubProjects() throws IntegrationException {
        try (final BlackduckRestConnection restConnection = globalProperties.createRestConnectionAndLogErrors(logger)) {
            if (restConnection != null) {
                final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactory(restConnection, new Slf4jIntLogger(logger));
                final List<ProjectView> rawProjects = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);

                final List<HubProject> projects = new ArrayList<>();
                for (final ProjectView projectView : rawProjects) {
                    final HubProject project = new HubProject(projectView.name, projectView.description);
                    projects.add(project);
                }
                return projects;
            } else {
                throw new AlertException("Missing global configuration.");
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

}
