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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.database.api.NotificationManager;
import com.synopsys.integration.alert.database.api.ProviderDataAccessor;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.provider.polaris.PolarisProvider;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisIssueNotificationContentModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;

@Component
public class PolarisProjectSyncTask extends ScheduledTask {
    public static final String TASK_NAME = "polaris-project-sync-task";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PolarisProperties polarisProperties;
    private final ProviderDataAccessor projectRepositoryAccessor;
    private final NotificationManager notificationManager;
    private final Gson gson;

    public PolarisProjectSyncTask(final TaskScheduler taskScheduler, final PolarisProperties polarisProperties, final ProviderDataAccessor projectRepositoryAccessor, final NotificationManager notificationManager,
        final Gson gson) {
        super(taskScheduler, TASK_NAME);
        this.polarisProperties = polarisProperties;
        this.projectRepositoryAccessor = projectRepositoryAccessor;
        this.notificationManager = notificationManager;
        this.gson = gson;
    }

    @Override
    public void run() {
        logger.info("### Starting {}...", getTaskName());
        try {
            final AccessTokenPolarisHttpClient polarisHttpClient = polarisProperties.createPolarisHttpClient(logger);
            final Map<String, ProviderProject> remoteProjectsMap = getProjectsFromServer(polarisHttpClient);
            final Map<String, ProviderProject> storedProjectsMap = getProjectsFromDatabase();
            final Set<ProviderProject> newProjects = collectMissingProjects(storedProjectsMap, remoteProjectsMap.values());
            final Set<ProviderProject> updatedStoredProjects = storeNewProjects(storedProjectsMap, newProjects);

            cleanUpOldProjects(updatedStoredProjects, remoteProjectsMap);
            generateNotificationsForIssues(updatedStoredProjects);
        } catch (final IntegrationException e) {
            logger.error("Could not create Polaris connection", e);
        }
        logger.info("### Finished {}...", getTaskName());
    }

    private Map<String, ProviderProject> getProjectsFromServer(final AccessTokenPolarisHttpClient polarisHttpClient) {
        // FIXME implement
        return Map.of();
    }

    private Map<String, ProviderProject> getProjectsFromDatabase() {
        return projectRepositoryAccessor.findByProviderName(PolarisProvider.COMPONENT_NAME)
                   .stream()
                   .collect(Collectors.toMap(ProviderProject::getHref, Function.identity()));
    }

    private Set<ProviderProject> storeNewProjects(final Map<String, ProviderProject> oldStoredProjects, final Set<ProviderProject> newProjects) {
        newProjects.forEach(project -> projectRepositoryAccessor.saveProject(PolarisProvider.COMPONENT_NAME, project));

        newProjects.forEach(newProject -> oldStoredProjects.put(newProject.getHref(), newProject));
        return new HashSet<>(oldStoredProjects.values());
    }

    private void generateNotificationsForIssues(final Set<ProviderProject> projects) {
        final Date providerCreationDate = new Date();
        for (final ProviderProject project : projects) {
            final Collection<AlertPolarisIssueNotificationContentModel> notifications = createNotificationsForProject(project);
            for (final AlertPolarisIssueNotificationContentModel notification : notifications) {
                final String notificationContent = gson.toJson(notification);
                final NotificationContent notificationEntity = new NotificationContent(providerCreationDate, PolarisProvider.COMPONENT_NAME, providerCreationDate, notification.getNotificationType().name(), notificationContent);
                notificationManager.saveNotification(notificationEntity);
            }
        }
    }

    private Collection<AlertPolarisIssueNotificationContentModel> createNotificationsForProject(final ProviderProject project) {
        // FIXME implement
        return Set.of();
    }

    private void cleanUpOldProjects(final Set<ProviderProject> localProjects, final Map<String, ProviderProject> remoteProjectsMap) {
        final Set<ProviderProject> deletionCandidates = collectMissingProjects(remoteProjectsMap, localProjects);
        deletionCandidates
            .stream()
            .map(ProviderProject::getHref)
            .forEach(projectRepositoryAccessor::deleteByHref);
    }

    private Set<ProviderProject> collectMissingProjects(final Map<String, ProviderProject> baseProjectMap, final Collection<ProviderProject> missingProjectCandidates) {
        return missingProjectCandidates
                   .stream()
                   .filter(project -> !baseProjectMap.containsKey(project.getHref()))
                   .collect(Collectors.toSet());
    }
}
