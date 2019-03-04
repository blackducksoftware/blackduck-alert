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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.PolarisIssueModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.database.api.NotificationManager;
import com.synopsys.integration.alert.database.api.PolarisIssueAccessor;
import com.synopsys.integration.alert.database.api.ProviderDataAccessor;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.provider.polaris.PolarisProvider;
import com.synopsys.integration.alert.provider.polaris.PolarisRequestHelper;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisIssueNotificationContentModel;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisNotificationTypeEnum;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.BranchV0;
import com.synopsys.integration.polaris.common.api.BranchV0Attributes;
import com.synopsys.integration.polaris.common.api.ProjectV0;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;

@Component
public class PolarisProjectSyncTask extends ScheduledTask {
    public static final String TASK_NAME = "polaris-project-sync-task";
    public static final String DEFAULT_CRON_EXPRESSION = BlackDuckAccumulator.DEFAULT_CRON_EXPRESSION;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PolarisProperties polarisProperties;
    private final ProviderDataAccessor projectRepositoryAccessor;
    private final PolarisIssueAccessor polarisIssueAccessor;
    private final NotificationManager notificationManager;
    private final Gson gson;

    public PolarisProjectSyncTask(final TaskScheduler taskScheduler, final PolarisProperties polarisProperties, final ProviderDataAccessor projectRepositoryAccessor, final PolarisIssueAccessor polarisIssueAccessor,
        final NotificationManager notificationManager, final Gson gson) {
        super(taskScheduler, TASK_NAME);
        this.polarisProperties = polarisProperties;
        this.projectRepositoryAccessor = projectRepositoryAccessor;
        this.polarisIssueAccessor = polarisIssueAccessor;
        this.notificationManager = notificationManager;
        this.gson = gson;
    }

    @Override
    public void run() {
        logger.info("### Starting {}...", getTaskName());
        try {
            final String baseUrl = polarisProperties
                                       .getUrl()
                                       .orElseThrow(() -> new AlertException("Polaris Url is not configured"));
            final AccessTokenPolarisHttpClient polarisHttpClient = polarisProperties.createPolarisHttpClient(logger);

            final Map<String, ProviderProject> remoteProjectsMap = getProjectsFromServer(polarisHttpClient, baseUrl);
            logger.info("{} remote projects", remoteProjectsMap.size());
            final Map<String, ProviderProject> storedProjectsMap = getProjectsFromDatabase();
            logger.info("{} local projects", storedProjectsMap.size());
            final Set<ProviderProject> newProjects = collectMissingProjects(storedProjectsMap, remoteProjectsMap.values());
            logger.info("{} new projects", newProjects.size());

            final Set<ProviderProject> updatedStoredProjects = storeNewProjects(storedProjectsMap, newProjects);
            cleanUpOldProjects(updatedStoredProjects, remoteProjectsMap);

            final Map<ProviderProject, Set<PolarisIssueModel>> projectIssuesMap = updateIssuesForProjects(polarisHttpClient, updatedStoredProjects);
            generateNotificationsForProjectIssues(projectIssuesMap);
        } catch (final IntegrationException e) {
            logger.error("Could not create Polaris connection", e);
        }
        logger.info("### Finished {}...", getTaskName());
    }

    private Map<String, ProviderProject> getProjectsFromServer(final AccessTokenPolarisHttpClient polarisHttpClient, final String baseUrl) {
        final PolarisRequestHelper polarisRequestHelper = new PolarisRequestHelper(polarisHttpClient, baseUrl, gson);

        final Map<String, ProviderProject> projectsFromServer = new HashMap<>();
        try {
            final List<ProjectV0> allProjects = polarisRequestHelper.getAllProjects();
            for (final ProjectV0 project : allProjects) {
                final List<BranchV0> branchesForProject = polarisRequestHelper.getBranchesForProject(project.getId());
                final String branchesString = branchesForProject
                                                  .stream()
                                                  .map(BranchV0::getAttributes)
                                                  .map(BranchV0Attributes::getName)
                                                  .collect(Collectors.joining(", "));
                final String name = project
                                        .getAttributes()
                                        .getName();
                final String description = StringUtils.truncate("Branches: " + branchesString, 80);
                final String href = project
                                        .getLinks()
                                        .getSelf()
                                        .getHref();
                final String projectOwnerEmail = "noreply@synopsys.com"; // FIXME Project > Members > Administrator > Email Address
                final ProviderProject providerProject = new ProviderProject(name, description, href, projectOwnerEmail);
                projectsFromServer.put(href, providerProject);
            }
        } catch (final IntegrationException e) {
            logger.error("Failed to get projects from Polaris", e);
        }
        return projectsFromServer;
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

    private void generateNotificationsForProjectIssues(final Map<ProviderProject, Set<PolarisIssueModel>> projectIssuesMap) {
        final Date providerCreationDate = new Date();
        for (final Map.Entry<ProviderProject, Set<PolarisIssueModel>> projectIssueEntry : projectIssuesMap.entrySet()) {
            final Collection<AlertPolarisIssueNotificationContentModel> notifications = createNotificationsForProject(projectIssueEntry.getKey(), projectIssueEntry.getValue());
            for (final AlertPolarisIssueNotificationContentModel notification : notifications) {
                final String notificationContent = gson.toJson(notification);
                final NotificationContent notificationEntity = new NotificationContent(providerCreationDate, PolarisProvider.COMPONENT_NAME, providerCreationDate, notification.getNotificationType().name(), notificationContent);
                notificationManager.saveNotification(notificationEntity);
            }
        }
    }

    private Collection<AlertPolarisIssueNotificationContentModel> createNotificationsForProject(final ProviderProject project, final Set<PolarisIssueModel> issues) {
        final Set<AlertPolarisIssueNotificationContentModel> notifications = new HashSet<>();
        for (final PolarisIssueModel issue : issues) {
            AlertPolarisNotificationTypeEnum notificationType = null;
            if (issue.isIssueCountIncreasing()) {
                notificationType = AlertPolarisNotificationTypeEnum.ISSUE_COUNT_INCREASED;
            } else if (issue.isIssueCountDecreasing()) {
                notificationType = AlertPolarisNotificationTypeEnum.ISSUE_COUNT_DECREASED;
            }

            if (notificationType != null) {
                final AlertPolarisIssueNotificationContentModel newNotification = new AlertPolarisIssueNotificationContentModel(notificationType, project.getName(), project.getHref(), issue.getIssueType(), issue.getCurrentIssueCount());
                notifications.add(newNotification);
            }
        }
        return notifications;
    }

    private Map<ProviderProject, Set<PolarisIssueModel>> updateIssuesForProjects(final AccessTokenPolarisHttpClient polarisHttpClient, final Set<ProviderProject> projects) {
        final Map<ProviderProject, Set<PolarisIssueModel>> projectIssuesMap = new HashMap<>();
        for (final ProviderProject project : projects) {
            final String projectHref = project.getHref();
            final Set<PolarisIssueModel> projectIssues = new HashSet<>();
            projectIssuesMap.put(project, projectIssues);

            // FIXME implement
            final List<PolarisIssueModel> issuesForProjectFromServer = List.of(); // TODO get this from the server

            for (final PolarisIssueModel issueFromServer : issuesForProjectFromServer) {
                try {
                    final PolarisIssueModel actionableIssue = polarisIssueAccessor.updateIssueType(projectHref, issueFromServer.getIssueType(), issueFromServer.getCurrentIssueCount());
                    projectIssues.add(actionableIssue);
                } catch (final AlertDatabaseConstraintException e) {
                    logger.error("Problem updating issue type", e);
                }
            }
        }
        return projectIssuesMap;
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
