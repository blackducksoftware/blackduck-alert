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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.core.HubView;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailSyncTask extends SyncTask<String> {
    private final Logger logger = LoggerFactory.getLogger(EmailSyncTask.class);
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;

    @Autowired
    public EmailSyncTask(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor) {
        super(taskScheduler, "blackduck-sync-email-task", blackDuckProperties);
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
    }

    @Override
    public List<UserView> getHubViews(final HubService hubService) throws IntegrationException {
        return hubService.getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE);
    }

    @Override
    public Map<String, ? extends HubView> getCurrentData(final List<? extends HubView> hubViews) {
        final List<UserView> userViews = (List<UserView>) hubViews;

        final Map<String, ? extends HubView> emailAddressMap = new HashMap<>();
        for (final UserView userView : userViews) {
            if (StringUtils.isNotBlank(userView.email)) {
                // We do not need the HubView's here because that is used for adding relations and we wont be adding relations in the EmailSyncTask
                emailAddressMap.put(userView.email, null);
            }
        }
        return emailAddressMap;
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
    public List<? extends DatabaseEntity> getEntitiesToRemove(final List<? extends DatabaseEntity> storedEntities, final Set<String> dataToRemove) {
        final List<BlackDuckUserEntity> blackDuckUserEntities = (List<BlackDuckUserEntity>) storedEntities;
        final List<BlackDuckUserEntity> blackDuckUsersToRemove = blackDuckUserEntities.stream()
                                                                     .filter(blackDuckUserEntity -> dataToRemove.contains(blackDuckUserEntity.getEmailAddress()))
                                                                     .collect(Collectors.toList());
        return blackDuckUsersToRemove;
    }

    @Override
    public void deleteEntity(final Long id) {
        blackDuckUserRepositoryAccessor.deleteEntity(id);
    }

    @Override
    public DatabaseEntity createAndSaveEntity(final String data) {
        return blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(data, false));
    }

    @Override
    public void addRelations(final Map<String, ? extends HubView> currentDataMap, final List<? extends DatabaseEntity> storedEntities, final HubService hubService) throws IOException, IntegrationException {

    }

}
