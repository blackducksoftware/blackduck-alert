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
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserGroupRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class EmailSyncTask extends SyncTask<String> {
    private final Logger logger = LoggerFactory.getLogger(EmailSyncTask.class);
    private final BlackDuckProperties blackDuckProperties;
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    private final UserGroupRelationRepositoryAccessor userGroupRelationRepositoryAccessor;

    @Autowired
    public EmailSyncTask(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor,
        final UserGroupRelationRepositoryAccessor userGroupRelationRepositoryAccessor) {
        super(taskScheduler, "blackduck-sync-email-task");
        this.blackDuckProperties = blackDuckProperties;
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
        this.userGroupRelationRepositoryAccessor = userGroupRelationRepositoryAccessor;
    }

    @Override
    public Set<String> getCurrentData() throws IOException, IntegrationException {
        final Optional<BlackduckRestConnection> optionalConnection = blackDuckProperties.createRestConnectionAndLogErrors(logger);
        if (optionalConnection.isPresent()) {
            try (final BlackduckRestConnection restConnection = optionalConnection.get()) {
                if (restConnection != null) {
                    final HubServicesFactory hubServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
                    final HubService hubService = hubServicesFactory.createHubService();
                    final List<UserView> userResponses = hubService.getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE);
                    final Set<String> emailAddresses = userResponses.stream().filter(userView -> StringUtils.isNotBlank(userView.email)).map(userView -> userView.email).collect(Collectors.toSet());
                    return emailAddresses;
                }
            }
        }
        return null;
    }

    @Override
    public List<? extends DatabaseEntity> getStoredEntities() {
        return blackDuckUserRepositoryAccessor.readEntities();
    }

    @Override
    public Set<String> getStoredData(final List<? extends DatabaseEntity> storedEntities) {
        final List<BlackDuckUserEntity> blackDuckUserEntities = (List<BlackDuckUserEntity>) storedEntities;

        final Set<String> storedEmails = blackDuckUserEntities.stream().map(blackDuckUserEntity -> blackDuckUserEntity.getEmailAddress()).collect(Collectors.toSet());
        return storedEmails;
    }

    @Override
    public List<Long> getEntityIdsToRemove(final List<? extends DatabaseEntity> storedEntities, final Set<String> dataToRemove) {
        final List<BlackDuckUserEntity> blackDuckUserEntities = (List<BlackDuckUserEntity>) storedEntities;
        final List<Long> blackDuckUserIdsToRemove = blackDuckUserEntities.stream()
                                                        .filter(blackDuckUserEntity -> dataToRemove.contains(blackDuckUserEntity.getEmailAddress()))
                                                        .map(blackDuckUserEntity -> blackDuckUserEntity.getId())
                                                        .collect(Collectors.toList());
        return blackDuckUserIdsToRemove;
    }

    @Override
    public void deleteEntity(final Long id) {
        blackDuckUserRepositoryAccessor.deleteEntity(id);
        userGroupRelationRepositoryAccessor.deleteRelationByUserId(id);
    }

    @Override
    public DatabaseEntity createAndSaveEntity(final String data) {
        return blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(data, false));
    }
}
