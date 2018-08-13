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
package com.synopsys.integration.alert.web.provider.blackduck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.hub.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.hub.api.generated.view.ProjectView;
import com.synopsys.integration.hub.api.generated.view.UserGroupView;
import com.synopsys.integration.hub.rest.BlackduckRestConnection;
import com.synopsys.integration.hub.service.HubServicesFactory;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.alert.common.exception.AlertException;

@Component
public class BlackDuckDataActions {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckDataActions.class);
    private final BlackDuckProperties blackDuckProperties;

    @Autowired
    public BlackDuckDataActions(final BlackDuckProperties blackDuckProperties) {
        this.blackDuckProperties = blackDuckProperties;
    }

    public List<BlackDuckGroup> getBlackDuckGroups() throws IntegrationException {
        final Optional<BlackduckRestConnection> optionalRestConnection = blackDuckProperties.createRestConnectionAndLogErrors(logger);
        if (optionalRestConnection.isPresent()) {
            try (final BlackduckRestConnection restConnection = optionalRestConnection.get()) {
                final HubServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
                final List<UserGroupView> rawGroups = blackDuckServicesFactory.createHubService().getAllResponses(ApiDiscovery.USERGROUPS_LINK_RESPONSE);

                final List<BlackDuckGroup> groups = new ArrayList<>();
                for (final UserGroupView userGroupView : rawGroups) {
                    final BlackDuckGroup blackDuckGroup = new BlackDuckGroup(userGroupView.name, userGroupView.active, userGroupView._meta.href);
                    groups.add(blackDuckGroup);
                }
                return groups;
            } catch (final IOException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            throw new AlertException("Missing global configuration.");
        }
        return Collections.emptyList();
    }

    public List<BlackDuckProject> getBlackDuckProjects() throws IntegrationException {
        final Optional<BlackduckRestConnection> optionalRestConnection = blackDuckProperties.createRestConnectionAndLogErrors(logger);
        if (optionalRestConnection.isPresent()) {
            try (final BlackduckRestConnection restConnection = optionalRestConnection.get()) {
                final HubServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
                final List<ProjectView> rawProjects = blackDuckServicesFactory.createHubService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);

                final List<BlackDuckProject> projects = new ArrayList<>();
                for (final ProjectView projectView : rawProjects) {
                    final BlackDuckProject project = new BlackDuckProject(projectView.name, projectView.description);
                    projects.add(project);
                }
                return projects;
            } catch (final IOException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            throw new AlertException("Missing global configuration.");
        }
        return Collections.emptyList();
    }

}
