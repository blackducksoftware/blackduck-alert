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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckGroupEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckGroupRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.tasks.model.GroupData;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class GroupSyncTask extends SyncTask<GroupData> {
    private final Logger logger = LoggerFactory.getLogger(GroupSyncTask.class);
    private final BlackDuckProperties blackDuckProperties;
    private final BlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor;

    @Autowired
    public GroupSyncTask(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final BlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor) {
        super(taskScheduler, "blackduck-sync-group-task");
        this.blackDuckProperties = blackDuckProperties;
        this.blackDuckGroupRepositoryAccessor = blackDuckGroupRepositoryAccessor;
    }

    @Override
    public Set<GroupData> getCurrentData() throws IOException, IntegrationException {
        final Optional<BlackduckRestConnection> optionalConnection = blackDuckProperties.createRestConnectionAndLogErrors(logger);
        if (optionalConnection.isPresent()) {
            try (final BlackduckRestConnection restConnection = optionalConnection.get()) {
                if (restConnection != null) {
                    final HubServicesFactory hubServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
                    final HubService hubService = hubServicesFactory.createHubService();
                    final List<UserGroupView> group = hubService.getAllResponses(ApiDiscovery.USERGROUPS_LINK_RESPONSE);
                    final Set<GroupData> groups = group.stream().map(groupView -> new GroupData(groupView.name, groupView.active, groupView._meta.href)).collect(Collectors.toSet());
                    return groups;
                }
            }
        }
        return null;
    }

    @Override
    public List<? extends DatabaseEntity> getStoredEntities() {
        return blackDuckGroupRepositoryAccessor.readEntities();
    }

    @Override
    public Set<GroupData> getStoredData(final List<? extends DatabaseEntity> storedEntities) {
        final List<BlackDuckGroupEntity> blackDuckGroupEntities = (List<BlackDuckGroupEntity>) storedEntities;

        final Set<GroupData> storedGroups = blackDuckGroupEntities.stream().map(blackDuckGroupEntity -> new GroupData(blackDuckGroupEntity.getName(), blackDuckGroupEntity.getActive(), blackDuckGroupEntity.getHref()))
                                                .collect(Collectors.toSet());
        return storedGroups;
    }

    @Override
    public List<Long> getEntityIdsToRemove(final List<? extends DatabaseEntity> storedEntities, final Set<GroupData> dataToRemove) {
        final List<BlackDuckGroupEntity> blackDuckGroupEntities = (List<BlackDuckGroupEntity>) storedEntities;
        final List<Long> blackDuckUserIdsToRemove = blackDuckGroupEntities.stream()
                                                        .filter(blackDuckGroupEntity -> {
                                                                Optional<GroupData> found = dataToRemove.stream()
                                                                                                .filter(data -> data.getName().equals(blackDuckGroupEntity.getName()))
                                                                                                .findFirst();
                                                                return found.isPresent();
                                                            }
                                                        )
                                                        .map(blackDuckGroupEntity -> blackDuckGroupEntity.getId())
                                                        .collect(Collectors.toList());
        return blackDuckUserIdsToRemove;
    }

    @Override
    public void deleteEntity(final Long id) {
        blackDuckGroupRepositoryAccessor.deleteEntity(id);
    }

    @Override
    public DatabaseEntity createAndSaveEntity(final GroupData data) {
        return blackDuckGroupRepositoryAccessor.saveEntity(new BlackDuckGroupEntity(data.getName(), data.getActive(), data.getHref()));
    }
}


