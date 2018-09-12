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

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.model.BlackDuckGroup;
import com.synopsys.integration.alert.provider.blackduck.model.BlackDuckProject;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckDataActions {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckDataActions.class);
    private final BlackDuckProperties blackDuckProperties;
    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;

    @Autowired
    public BlackDuckDataActions(final BlackDuckProperties blackDuckProperties, final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor) {
        this.blackDuckProperties = blackDuckProperties;
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
    }

    public List<BlackDuckGroup> getBlackDuckGroups() throws IntegrationException {
        //TODO JR remove the group configuration
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

    public List<BlackDuckProject> getBlackDuckProjects() {
        final List<BlackDuckProjectEntity> blackDuckProjectEntities = (List<BlackDuckProjectEntity>) blackDuckProjectRepositoryAccessor.readEntities();
        if (!blackDuckProjectEntities.isEmpty()) {
            final List<BlackDuckProject> projects = new ArrayList<>();
            for (final BlackDuckProjectEntity blackDuckProjectEntity : blackDuckProjectEntities) {
                final BlackDuckProject project = new BlackDuckProject(blackDuckProjectEntity.getName(), blackDuckProjectEntity.getDescription(), blackDuckProjectEntity.getHref());
                projects.add(project);
            }
            return projects;
        } else {
            logger.info("No BlackDuck projects found in the database.");
        }
        return Collections.emptyList();
    }

}
