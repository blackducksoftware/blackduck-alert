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
package com.synopsys.integration.alert.provider.blackduck.tasks;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.tasks.model.ProjectData;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class ProjectSyncTask extends SyncTask<ProjectData> {
    private final Logger logger = LoggerFactory.getLogger(ProjectSyncTask.class);
    private final BlackDuckProperties blackDuckProperties;
    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;

    @Autowired
    public ProjectSyncTask(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor) {
        super(taskScheduler, "blackduck-sync-project-task");
        this.blackDuckProperties = blackDuckProperties;
        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
    }

    @Override
    public Set<ProjectData> getCurrentData() throws IOException, IntegrationException {
        final Optional<BlackduckRestConnection> optionalConnection = blackDuckProperties.createRestConnectionAndLogErrors(logger);
        if (optionalConnection.isPresent()) {
            try (final BlackduckRestConnection restConnection = optionalConnection.get()) {
                if (restConnection != null) {
                    final HubServicesFactory hubServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
                    final HubService hubService = hubServicesFactory.createHubService();
                    final List<ProjectView> projectResponses = hubService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
                    final Set<ProjectData> projects = projectResponses.stream().map(projectView -> new ProjectData(projectView.name, StringUtils.trimToEmpty(projectView.description), projectView._meta.href)).collect(Collectors.toSet());
                    return projects;
                }
            }
        }
        return null;
    }

    @Override
    public List<? extends DatabaseEntity> getStoredEntities() {
        return blackDuckProjectRepositoryAccessor.readEntities();
    }

    @Override
    public Set<ProjectData> getStoredData(final List<? extends DatabaseEntity> storedEntities) {
        final List<BlackDuckProjectEntity> blackDuckProjectEntities = (List<BlackDuckProjectEntity>) storedEntities;

        final Set<ProjectData> storedGroups = blackDuckProjectEntities.stream().map(blackDuckProjectEntity -> new ProjectData(blackDuckProjectEntity.getName(), blackDuckProjectEntity.getDescription(), blackDuckProjectEntity.getHref()))
                                                  .collect(Collectors.toSet());
        return storedGroups;
    }

    @Override
    public List<Long> getEntityIdsToRemove(final List<? extends DatabaseEntity> storedEntities, final Set<ProjectData> dataToRemove) {
        final List<BlackDuckProjectEntity> blackDuckProjectEntities = (List<BlackDuckProjectEntity>) storedEntities;
        final List<Long> blackDuckProjectIdsToRemove = blackDuckProjectEntities.stream()
                                                           .filter(blackDuckProjectEntity -> {
                                                                   Optional<ProjectData> found = dataToRemove.stream()
                                                                                                     .filter(data -> data.getName().equals(blackDuckProjectEntity.getName()))
                                                                                                     .findFirst();
                                                                   return found.isPresent();
                                                               }
                                                           )
                                                           .map(blackDuckProjectEntity -> blackDuckProjectEntity.getId())
                                                           .collect(Collectors.toList());
        return blackDuckProjectIdsToRemove;
    }

    @Override
    public void deleteEntity(final Long id) {
        blackDuckProjectRepositoryAccessor.deleteEntity(id);
    }

    @Override
    public DatabaseEntity createAndSaveEntity(final ProjectData data) {
        return blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity(data.getName(), data.getDescription(), data.getHref()));
    }

}


