/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.provider.polaris.tasks;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.database.api.NotificationManager;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.provider.polaris.PolarisProvider;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisIssueNotificationContentModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.ProjectV0;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;

@Component
public class PolarisProjectSyncTask extends ScheduledTask {
    public static final String TASK_NAME = "polaris-project-sync-task";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PolarisProperties polarisProperties;
    private final NotificationManager notificationManager;
    private final Gson gson;

    public PolarisProjectSyncTask(final TaskScheduler taskScheduler, final PolarisProperties polarisProperties, final NotificationManager notificationManager, final Gson gson) {
        super(taskScheduler, TASK_NAME);
        this.polarisProperties = polarisProperties;
        this.notificationManager = notificationManager;
        this.gson = gson;
    }

    @Override
    public void run() {
        logger.info("### Starting {}...", getTaskName());
        try {
            final AccessTokenPolarisHttpClient polarisHttpClient = polarisProperties.createPolarisHttpClient(logger);
            final Map<String, ProjectV0> remoteProjectsMap = getProjectsFromServer(polarisHttpClient);
            final Map<String, ProjectV0> storedProjectsMap = getProjectsFromDatabase();
            final Set<ProjectV0> newProjects = collectMissingProjects(storedProjectsMap, remoteProjectsMap.values());
            final Set<ProjectV0> updatedStoredProjects = storeNewProjects(storedProjectsMap, newProjects);

            cleanUpOldProjects(updatedStoredProjects, remoteProjectsMap);
            generateNotificationsForIssues(updatedStoredProjects);
        } catch (final IntegrationException e) {
            logger.error("Could not create Polaris connection", e);
        }
        logger.info("### Finished {}...", getTaskName());
    }

    private Map<String, ProjectV0> getProjectsFromServer(final AccessTokenPolarisHttpClient polarisHttpClient) {
        // FIXME implement
        return Map.of();
    }

    private Map<String, ProjectV0> getProjectsFromDatabase() {
        final List<ProjectV0> storedProjects = List.of();
        // FIXME implement
        return storedProjects
                   .stream()
                   .collect(Collectors.toMap(ProjectV0::getId, Function.identity()));
    }

    private Set<ProjectV0> storeNewProjects(final Map<String, ProjectV0> oldStoredProjects, final Set<ProjectV0> newProjects) {

        // FIXME store newProjects

        newProjects.forEach(newProject -> oldStoredProjects.put(newProject.getId(), newProject));
        return new HashSet<>(oldStoredProjects.values());
    }

    private void generateNotificationsForIssues(final Set<ProjectV0> projects) {
        final Date providerCreationDate = new Date();
        for (final ProjectV0 project : projects) {
            final Collection<AlertPolarisIssueNotificationContentModel> notifications = createNotificationsForProject(project);
            for (final AlertPolarisIssueNotificationContentModel notification : notifications) {
                final String notificationContent = gson.toJson(notification);
                final NotificationContent notificationEntity = new NotificationContent(providerCreationDate, PolarisProvider.COMPONENT_NAME, providerCreationDate, notification.getNotificationType().name(), notificationContent);
                notificationManager.saveNotification(notificationEntity);
            }
        }
    }

    private Collection<AlertPolarisIssueNotificationContentModel> createNotificationsForProject(final ProjectV0 project) {
        // FIXME implement
        return Set.of();
    }

    private void cleanUpOldProjects(final Set<ProjectV0> localProjects, final Map<String, ProjectV0> remoteProjectsMap) {
        final Set<ProjectV0> deletionCandidates = collectMissingProjects(remoteProjectsMap, localProjects);
        // FIXME delete the candidates
    }

    private Set<ProjectV0> collectMissingProjects(final Map<String, ProjectV0> baseProjectMap, final Collection<ProjectV0> missingProjectCandidates) {
        return missingProjectCandidates
                   .stream()
                   .filter(project -> !baseProjectMap.containsKey(project.getId()))
                   .collect(Collectors.toSet());
    }
}
