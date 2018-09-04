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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.workflow.scheduled.ScheduledTask;
import com.synopsys.integration.exception.IntegrationException;

public abstract class SyncTask<T> extends ScheduledTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public SyncTask(final TaskScheduler taskScheduler, final String taskName) {
        super(taskScheduler, taskName);
    }

    @Override
    public void run() {
        logger.info("### Starting {}...", getTaskName());
        try {
            final Set<T> currentDataSet = getCurrentData();
            syncDBWithCurrentData(currentDataSet);
        } catch (final IOException | IntegrationException e) {
            logger.error("Could not retrieve the current data from the BlackDuck server : " + e.getMessage(), e);
        }
        logger.info("### Finished {}...", getTaskName());
    }

    public abstract Set<T> getCurrentData() throws IOException, IntegrationException;

    public abstract List<? extends DatabaseEntity> getStoredEntities();

    public abstract Set<T> getStoredData(List<? extends DatabaseEntity> storedEntities);

    public abstract List<Long> getEntityIdsToRemove(List<? extends DatabaseEntity> storedEntities, Set<T> dataToRemove);

    public abstract void deleteEntity(Long id);

    public abstract DatabaseEntity createAndSaveEntity(T data);

    public void syncDBWithCurrentData(final Set<T> currentDataSet) {
        final Set<T> dataToAdd = new HashSet<>();
        final Set<T> dataToRemove = new HashSet<>();

        final List<? extends DatabaseEntity> storedEntities = getStoredEntities();

        final Set<T> storedDataSet = getStoredData(storedEntities);
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

        final List<Long> entityIdsToRemove = getEntityIdsToRemove(storedEntities, dataToRemove);
        entityIdsToRemove.stream().forEach(entityIdToRemove -> deleteEntity(entityIdToRemove));

        dataToAdd.stream().forEach(data -> createAndSaveEntity(data));
    }

}
