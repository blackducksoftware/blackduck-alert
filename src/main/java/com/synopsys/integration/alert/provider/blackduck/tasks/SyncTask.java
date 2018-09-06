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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.workflow.scheduled.ScheduledTask;
import com.synopsys.integration.blackduck.api.core.HubView;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

public abstract class SyncTask<T> extends ScheduledTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BlackDuckProperties blackDuckProperties;
    private final RepositoryAccessor repositoryAccessor;

    public SyncTask(final TaskScheduler taskScheduler, final String taskName, final BlackDuckProperties blackDuckProperties, final RepositoryAccessor repositoryAccessor) {
        super(taskScheduler, taskName);
        this.blackDuckProperties = blackDuckProperties;
        this.repositoryAccessor = repositoryAccessor;
    }

    @Override
    public void run() {
        logger.info("### Starting {}...", getTaskName());
        try {
            final Optional<BlackduckRestConnection> optionalConnection = blackDuckProperties.createRestConnectionAndLogErrors(logger);
            if (optionalConnection.isPresent()) {
                try (final BlackduckRestConnection restConnection = optionalConnection.get()) {
                    if (restConnection != null) {
                        final HubServicesFactory hubServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
                        final HubService hubService = hubServicesFactory.createHubService();
                        final List<? extends HubView> hubViews = getHubViews(hubService);
                        final Map<T, ? extends HubView> currentDataMap = getCurrentData(hubViews);
                        List<? extends DatabaseEntity> storedEntities = getStoredEntities();
                        syncDBWithCurrentData(currentDataMap, storedEntities);

                        storedEntities = getStoredEntities();
                        addRelations(currentDataMap, storedEntities, hubService);
                    }
                }
            } else {
                logger.error("Missing BlackDuck global configuration.");
            }
        } catch (final IOException | IntegrationException e) {
            logger.error("Could not retrieve the current data from the BlackDuck server : " + e.getMessage(), e);
        }
        logger.info("### Finished {}...", getTaskName());
    }

    public abstract List<? extends HubView> getHubViews(HubService hubService) throws IntegrationException;

    public abstract Map<T, ? extends HubView> getCurrentData(List<? extends HubView> hubViews);

    public List<? extends DatabaseEntity> getStoredEntities() {
        return repositoryAccessor.readEntities();
    }

    public abstract Set<T> getStoredData(List<? extends DatabaseEntity> storedEntities);

    public abstract List<? extends DatabaseEntity> getEntitiesToRemove(List<? extends DatabaseEntity> storedEntities, Set<T> dataToRemove);

    public void deleteEntity(final Long id) {
        repositoryAccessor.deleteEntity(id);
    }

    public abstract DatabaseEntity createEntity(T data);

    public DatabaseEntity createAndSaveEntity(final T data) {
        return repositoryAccessor.saveEntity(createEntity(data));
    }

    public abstract void addRelations(final Map<T, ? extends HubView> currentDataMap, final List<? extends DatabaseEntity> storedEntities, HubService hubService) throws IOException, IntegrationException;

    public void syncDBWithCurrentData(final Map<T, ? extends HubView> currentDataMap, final List<? extends DatabaseEntity> storedEntities) {
        final Set<T> dataToAdd = new HashSet<>();
        final Set<T> dataToRemove = new HashSet<>();

        final Set<T> storedDataSet = getStoredData(storedEntities);
        final Set<T> currentDataSet = currentDataMap.keySet();

        storedDataSet.stream().forEach(storedData -> {
            // If the storedData no longer exists in the current then we need to remove the entry
            // If any of the fields have changed in the currentData, then the storedData will not be in the currentData so we will need to remove the old entry
            if (!currentDataSet.contains(storedData)) {
                dataToRemove.add(storedData);
            }
        });
        currentDataSet.stream().forEach(currentData -> {
            // If the currentData is not found in the stored data then we will need to add a new entry
            // If any of the fields have changed in the currentData, then it wont be in the stored data so we will need to add a new entry
            if (!storedDataSet.contains(currentData)) {
                dataToAdd.add(currentData);
            }
        });
        logger.info("Adding {}", dataToAdd.size());
        logger.info("Removing {}", dataToRemove.size());

        final List<? extends DatabaseEntity> entitiesToRemove = getEntitiesToRemove(storedEntities, dataToRemove);
        entitiesToRemove.stream().forEach(entityToRemove -> {
            deleteEntity(entityToRemove.getId());
        });

        for (final T data : dataToAdd) {
            createAndSaveEntity(data);
        }
    }

}
