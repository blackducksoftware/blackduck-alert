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
import java.util.Optional;
import java.util.Set;
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
import com.synopsys.integration.alert.provider.polaris.PolarisApiHelper;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.provider.polaris.PolarisProvider;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisIssueNotificationContentModel;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisNotificationTypeEnum;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.polaris.common.api.common.branch.BranchV0Attributes;
import com.synopsys.integration.polaris.common.api.common.branch.BranchV0Resource;
import com.synopsys.integration.polaris.common.api.common.project.ProjectV0Resource;
import com.synopsys.integration.polaris.common.service.PolarisServicesFactory;

@Component
public class PolarisProjectSyncTask extends ScheduledTask {
    public static final String TASK_NAME = "polaris-project-sync-task";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PolarisProperties polarisProperties;
    private final ProviderDataAccessor providerDataAccessor;
    private final PolarisIssueAccessor polarisIssueAccessor;
    private final NotificationManager notificationManager;
    private final Gson gson;

    public PolarisProjectSyncTask(final TaskScheduler taskScheduler, final PolarisProperties polarisProperties, final ProviderDataAccessor providerDataAccessor, final PolarisIssueAccessor polarisIssueAccessor,
        final DefaultNotificationManager notificationManager, final Gson gson) {
        super(taskScheduler, TASK_NAME);
        this.polarisProperties = polarisProperties;
        this.providerDataAccessor = providerDataAccessor;
        this.polarisIssueAccessor = polarisIssueAccessor;
        this.notificationManager = notificationManager;
        this.gson = gson;
    }

    @Override
    public void runTask() {
        try {
            polarisProperties
                .getUrl()
                .orElseThrow(() -> new AlertException("Polaris Url is not configured"));
            final IntLogger intLogger = new Slf4jIntLogger(logger);
            final PolarisServicesFactory servicesFactory = polarisProperties.createPolarisServicesFactory(intLogger);
            final PolarisApiHelper polarisApiHelper = new PolarisApiHelper(
                servicesFactory.createProjectService(), servicesFactory.createBranchService(), servicesFactory.createIssueService(), servicesFactory.createRoleAssignmentsService(), servicesFactory.createUserService());

            final Map<ProjectV0Resource, List<BranchV0Resource>> projectToBranchMappings = polarisApiHelper.getProjectToBranchMappings();
            final Map<String, ProviderProject> hrefToRemoteProjectsMap = convertToProviderProjects(polarisApiHelper, projectToBranchMappings);
            logger.info("{} remote projects", hrefToRemoteProjectsMap.size());

            final Map<ProviderProject, Set<String>> serverStateMappings = createProjectToUsersMap(polarisApiHelper, projectToBranchMappings.keySet(), hrefToRemoteProjectsMap);
            providerDataAccessor.updateProjectAndUserData(PolarisProvider.COMPONENT_NAME, serverStateMappings);

            final Map<ProviderProject, Set<PolarisIssueModel>> projectIssuesMap = updateIssuesForProjects(polarisApiHelper, serverStateMappings.keySet(), projectToBranchMappings);
            generateNotificationsForProjectIssues(projectIssuesMap);
        } catch (final IntegrationException e) {
            logger.error("Could not create Polaris connection", e);
        }
    }

    private Map<String, ProviderProject> convertToProviderProjects(final PolarisApiHelper polarisApiHelper, final Map<ProjectV0Resource, List<BranchV0Resource>> projectToBranchMappings) throws IntegrationException {
        final Map<String, ProviderProject> providerProjects = new HashMap<>();
        for (final ProjectV0Resource serverProject : projectToBranchMappings.keySet()) {
            final List<BranchV0Resource> branchesForProject = projectToBranchMappings.get(serverProject);
            final String branchesString = branchesForProject
                                              .stream()
                                              .map(BranchV0Resource::getAttributes)
                                              .map(BranchV0Attributes::getName)
                                              .collect(Collectors.joining(", "));
            final String name = serverProject.getAttributes().getName();
            final String description = StringUtils.truncate(branchesString, 80);
            final String href = serverProject.getLinks().getSelf().getHref();
            final String projectOwnerEmail = polarisApiHelper.getAdminEmailForProject(serverProject).orElse(StringUtils.EMPTY);

            final ProviderProject providerProject = new ProviderProject(name, description, href, projectOwnerEmail);
            providerProjects.put(href, providerProject);
        }
        return providerProjects;
    }

    private Map<ProviderProject, Set<String>> createProjectToUsersMap(final PolarisApiHelper polarisApiHelper, final Set<ProjectV0Resource> projectsFromServer, final Map<String, ProviderProject> hrefToRemoteProjects) {
        final Map<ProviderProject, Set<String>> projectToUsers = new HashMap<>();
        for (final ProjectV0Resource projectResource : projectsFromServer) {
            final String projectHref = polarisApiHelper.getProjectHref(projectResource);
            final ProviderProject providerProject = hrefToRemoteProjects.get(projectHref);
            if (null != providerProject) {
                final Set<String> emailsForProject;
                try {
                    emailsForProject = polarisApiHelper.getAllEmailsForProject(projectResource);
                    projectToUsers.put(providerProject, emailsForProject);
                } catch (final IntegrationException e) {
                    logger.error("Failed to get emails for project '{}': ", providerProject.getName(), e.getMessage());
                }
            }
        }
        return projectToUsers;
    }

    private Map<ProviderProject, Set<PolarisIssueModel>> updateIssuesForProjects(final PolarisApiHelper polarisApiHelper, final Set<ProviderProject> projects, final Map<ProjectV0Resource, List<BranchV0Resource>> projectToBranchMappings) {
        final Map<ProviderProject, Set<PolarisIssueModel>> projectIssuesMap = new HashMap<>();
        for (final ProviderProject project : projects) {
            final String projectHref = project.getHref();
            final String projectName = project.getName();

            final String projectId;
            final List<String> branchIds;
            try {
                final Optional<ProjectV0Resource> optionalProjectV0Resource = polarisApiHelper.retrieveProjectByHrefOrName(projectToBranchMappings.keySet(), projectHref, projectName);
                if (optionalProjectV0Resource.isPresent()) {
                    final ProjectV0Resource ProjectV0Resource = optionalProjectV0Resource.get();
                    projectId = ProjectV0Resource.getId();
                    branchIds = polarisApiHelper.getBranchesIdsForProject(projectToBranchMappings, ProjectV0Resource);
                } else {
                    continue;
                }
            } catch (final IntegrationException e) {
                logger.error("Problem getting project data from Polaris: {}", projectName, e);
                continue;
            }

            final List<PolarisIssueModel> issueModelsFromServer = polarisApiHelper.createIssueModelsForProject(projectId, projectName, branchIds);
            final Set<PolarisIssueModel> projectIssues = updateIssues(projectHref, issueModelsFromServer);
            projectIssuesMap.put(project, projectIssues);
        }
        return projectIssuesMap;
    }

    private Set<PolarisIssueModel> updateIssues(final String projectHref, final List<PolarisIssueModel> issuesToStore) {
        final Set<PolarisIssueModel> actionableIssues = new HashSet<>();
        for (final PolarisIssueModel issueFromServer : issuesToStore) {
            try {
                logger.trace("Updating Polaris issue: {}", issueFromServer);
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
            final Collection<AlertPolarisIssueNotificationContentModel> notifications = createNotificationModelsForProject(projectIssueEntry.getKey(), projectIssueEntry.getValue());
            for (final AlertPolarisIssueNotificationContentModel notification : notifications) {
                final String notificationContent = gson.toJson(notification);
                final NotificationContent notificationEntity = new NotificationContent(providerCreationDate, PolarisProvider.COMPONENT_NAME, providerCreationDate, notification.getNotificationType().name(), notificationContent);
                notificationManager.saveNotification(notificationEntity);
            }
        }
    }

    private Collection<AlertPolarisIssueNotificationContentModel> createNotificationModelsForProject(final ProviderProject project, final Set<PolarisIssueModel> issues) {
        final Set<AlertPolarisIssueNotificationContentModel> notifications = new HashSet<>();
        for (final PolarisIssueModel issue : issues) {
            AlertPolarisNotificationTypeEnum notificationType = null;
            if (issue.isIssueCountIncreasing()) {
                notificationType = AlertPolarisNotificationTypeEnum.ISSUE_COUNT_INCREASED;
            } else if (issue.isIssueCountDecreasing()) {
                notificationType = AlertPolarisNotificationTypeEnum.ISSUE_COUNT_DECREASED;
            }

            if (notificationType != null) {
                final Integer numberChanged = Math.abs(issue.getCurrentIssueCount() - issue.getPreviousIssueCount());
                final AlertPolarisIssueNotificationContentModel newNotification =
                    new AlertPolarisIssueNotificationContentModel(notificationType, project.getName(), project.getDescription(), project.getHref(), issue.getIssueType(), numberChanged, issue.getCurrentIssueCount());
                notifications.add(newNotification);
            }
        }
        return notifications;
    }

}
