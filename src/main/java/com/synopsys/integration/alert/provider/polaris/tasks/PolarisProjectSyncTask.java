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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.persistence.accessor.PolarisIssueAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.PolarisIssueModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.database.api.DefaultNotificationManager;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.provider.polaris.PolarisDataHelper;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.provider.polaris.PolarisProvider;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisIssueNotificationContentModel;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisNotificationTypeEnum;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.polaris.common.api.generated.common.BranchV0;
import com.synopsys.integration.polaris.common.api.generated.common.BranchV0Attributes;
import com.synopsys.integration.polaris.common.api.generated.common.ProjectV0;
import com.synopsys.integration.polaris.common.model.QueryIssue;
import com.synopsys.integration.polaris.common.service.BranchService;
import com.synopsys.integration.polaris.common.service.IssueService;
import com.synopsys.integration.polaris.common.service.PolarisServicesFactory;
import com.synopsys.integration.polaris.common.service.ProjectService;

@Component
public class PolarisProjectSyncTask extends ScheduledTask {
    public static final String TASK_NAME = "polaris-project-sync-task";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PolarisProperties polarisProperties;
    private final ProviderDataAccessor projectRepositoryAccessor;
    private final PolarisIssueAccessor polarisIssueAccessor;
    private final PolarisDataHelper polarisDataHelper;
    private final NotificationManager notificationManager;
    private final Gson gson;

    public PolarisProjectSyncTask(final TaskScheduler taskScheduler, final PolarisProperties polarisProperties, final ProviderDataAccessor projectRepositoryAccessor, final PolarisIssueAccessor polarisIssueAccessor,
        final PolarisDataHelper polarisDataHelper, final DefaultNotificationManager notificationManager, final Gson gson) {
        super(taskScheduler, TASK_NAME);
        this.polarisProperties = polarisProperties;
        this.projectRepositoryAccessor = projectRepositoryAccessor;
        this.polarisIssueAccessor = polarisIssueAccessor;
        this.polarisDataHelper = polarisDataHelper;
        this.notificationManager = notificationManager;
        this.gson = gson;
    }

    @Override
    public void run() {
        logger.info("### Starting {}...", getTaskName());
        try {
            polarisProperties
                .getUrl()
                .orElseThrow(() -> new AlertException("Polaris Url is not configured"));
            final IntLogger intLogger = new Slf4jIntLogger(logger);
            final PolarisServicesFactory polarisServicesFactory = polarisProperties.createPolarisServicesFactory(intLogger);

            final ProjectService projectService = polarisServicesFactory.createProjectService();
            final BranchService branchService = polarisServicesFactory.createBranchService();
            final IssueService issueService = polarisServicesFactory.createIssueService();

            final Map<ProjectV0, List<BranchV0>> projectToBranchMappings = getProjectToBranchMappings(projectService, branchService);

            final Map<String, ProviderProject> hrefToRemoteProjectsMap = convertToProviderProjects(projectToBranchMappings);
            logger.info("{} remote projects", hrefToRemoteProjectsMap.size());
            final Map<String, ProviderProject> hrefToStoredProjectsMap = getProjectsFromDatabase();
            logger.info("{} local projects", hrefToStoredProjectsMap.size());
            final Set<ProviderProject> newProjects = collectMissingProjects(hrefToStoredProjectsMap, hrefToRemoteProjectsMap.values());
            logger.info("{} new projects", newProjects.size());

            final Set<ProviderProject> updatedStoredProjects = storeNewProjects(hrefToStoredProjectsMap, newProjects);
            cleanUpOldProjects(updatedStoredProjects, hrefToRemoteProjectsMap);

            final Map<ProviderProject, Set<PolarisIssueModel>> projectIssuesMap = updateIssuesForProjects(projectService, branchService, issueService, updatedStoredProjects, projectToBranchMappings);
            generateNotificationsForProjectIssues(projectIssuesMap);
        } catch (final IntegrationException e) {
            logger.error("Could not create Polaris connection", e);
        }
        logger.info("### Finished {}...", getTaskName());
    }

    private Map<ProjectV0, List<BranchV0>> getProjectToBranchMappings(final ProjectService projectService, final BranchService branchService) {
        final Map<ProjectV0, List<BranchV0>> projectToBranchMappings = new HashMap<>();
        try {
            final List<ProjectV0> allProjects = projectService.getAllProjects();
            for (final ProjectV0 project : allProjects) {
                final List<BranchV0> branchesForProject = branchService.getBranchesForProject(project.getId());
                projectToBranchMappings.put(project, branchesForProject);
            }
        } catch (final IntegrationException e) {
            logger.error("Failed to get projects from Polaris", e);
        }
        return projectToBranchMappings;
    }

    private Map<String, ProviderProject> convertToProviderProjects(final Map<ProjectV0, List<BranchV0>> projectToBranchMappings) {
        final Map<String, ProviderProject> providerProjects = new HashMap<>();
        for (final ProjectV0 serverProject : projectToBranchMappings.keySet()) {
            final List<BranchV0> branchesForProject = projectToBranchMappings.get(serverProject);
            final String branchesString = branchesForProject
                                              .stream()
                                              .map(BranchV0::getAttributes)
                                              .map(BranchV0Attributes::getName)
                                              .collect(Collectors.joining(", "));
            final String name = polarisDataHelper.getProjectName(serverProject);
            final String description = StringUtils.truncate("Branches: " + branchesString, 80);
            final String href = polarisDataHelper.getProjectHref(serverProject);
            final String projectOwnerEmail = "noreply@synopsys.com"; // FIXME Project > Members > Administrator > Email Address

            final ProviderProject providerProject = new ProviderProject(name, description, href, projectOwnerEmail);
            providerProjects.put(href, providerProject);
        }
        return providerProjects;
    }

    private Map<String, ProviderProject> getProjectsFromDatabase() {
        return projectRepositoryAccessor.findByProviderName(PolarisProvider.COMPONENT_NAME)
                   .stream()
                   .collect(Collectors.toMap(ProviderProject::getHref, Function.identity()));
    }

    // FIXME also store users
    private Set<ProviderProject> storeNewProjects(final Map<String, ProviderProject> oldStoredProjects, final Set<ProviderProject> newProjects) {
        newProjects.forEach(project -> projectRepositoryAccessor.saveProject(PolarisProvider.COMPONENT_NAME, project));

        newProjects.forEach(newProject -> oldStoredProjects.put(newProject.getHref(), newProject));
        return new HashSet<>(oldStoredProjects.values());
    }

    private Map<ProviderProject, Set<PolarisIssueModel>> updateIssuesForProjects(final ProjectService projectService, final BranchService branchService, final IssueService issueService, final Set<ProviderProject> projects,
        final Map<ProjectV0, List<BranchV0>> projectToBranchMappings) {

        final Map<ProviderProject, Set<PolarisIssueModel>> projectIssuesMap = new HashMap<>();
        for (final ProviderProject project : projects) {
            final String projectHref = project.getHref();
            final String projectName = project.getName();

            final String projectId;
            final List<String> branchIds;
            try {
                final Optional<ProjectV0> optionalProjectV0 = polarisDataHelper.getProjectByHrefOrName(projectToBranchMappings.keySet(), projectHref, projectName, projectService);
                if (optionalProjectV0.isPresent()) {
                    final ProjectV0 projectV0 = optionalProjectV0.get();
                    projectId = projectV0.getId();
                    branchIds = polarisDataHelper.getBranchesIdsForProject(projectToBranchMappings, projectV0, branchService);
                } else {
                    continue;
                }
            } catch (final IntegrationException e) {
                logger.error("Problem getting project data from Polaris: {}", projectName, e);
                continue;
            }

            final List<PolarisIssueModel> issuesForProjectFromServer = new ArrayList<>();
            for (final String branchId : branchIds) {
                try {
                    final List<QueryIssue> foundIssues = issueService.getIssuesForProjectAndBranch(projectId, branchId);
                    final Map<String, Integer> issueTypeCounts = polarisDataHelper.mapIssueTypeToCount(foundIssues);

                    for (final Map.Entry<String, Integer> issueTypeEntry : issueTypeCounts.entrySet()) {
                        final PolarisIssueModel newIssue = new PolarisIssueModel(issueTypeEntry.getKey(), 0, issueTypeEntry.getValue());
                        issuesForProjectFromServer.add(newIssue);
                    }
                } catch (final IntegrationException e) {
                    logger.error("Problem getting issues from Polaris: {}", project.getName(), e);
                    continue;
                }
            }

            final Set<PolarisIssueModel> projectIssues = updateIssues(projectHref, issuesForProjectFromServer);
            projectIssuesMap.put(project, projectIssues);
        }
        return projectIssuesMap;
    }

    private Set<PolarisIssueModel> updateIssues(final String projectHref, final List<PolarisIssueModel> issuesToStore) {
        final Set<PolarisIssueModel> actionableIssues = new HashSet<>();
        for (final PolarisIssueModel issueFromServer : issuesToStore) {
            try {
                // FIXME make this debug
                logger.info("Updating Polaris issue: {}", issueFromServer);
                final PolarisIssueModel updatedIssue = polarisIssueAccessor.updateIssueType(projectHref, issueFromServer.getIssueType(), issueFromServer.getCurrentIssueCount());
                actionableIssues.add(updatedIssue);
            } catch (final AlertDatabaseConstraintException e) {
                logger.error("Problem updating issue type", e);
            }
        }
        return actionableIssues;
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
