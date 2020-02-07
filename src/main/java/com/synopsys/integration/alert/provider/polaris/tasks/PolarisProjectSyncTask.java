/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.persistence.accessor.PolarisIssueAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.PolarisIssueModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
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
import com.synopsys.integration.polaris.common.api.common.model.branch.BranchV0Attributes;
import com.synopsys.integration.polaris.common.api.common.model.branch.BranchV0Resource;
import com.synopsys.integration.polaris.common.api.common.model.project.ProjectV0Resource;
import com.synopsys.integration.polaris.common.service.BranchService;
import com.synopsys.integration.polaris.common.service.PolarisServicesFactory;
import com.synopsys.integration.polaris.common.service.ProjectService;

//@Component
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
        final IntLogger intLogger = new Slf4jIntLogger(logger);
        try {
            final String polarisUrl = polarisProperties
                                          .getUrl()
                                          .orElseThrow(() -> new AlertException("Polaris Url is not configured"));

            final PolarisServicesFactory servicesFactory = polarisProperties.createPolarisServicesFactory(intLogger);
            final ProjectService projectService = servicesFactory.createProjectService();
            final BranchService branchService = servicesFactory.createBranchService();
            final PolarisApiHelper polarisApiHelper = new PolarisApiHelper(servicesFactory.createIssueService(), servicesFactory.createRoleAssignmentService(), servicesFactory.createUserService());

            final Map<ProviderProject, Set<String>> projectUserEmailMappings = new HashMap<>();
            final Map<ProviderProject, Set<PolarisIssueModel>> issuesToUpdate = new HashMap<>();
            final Set<AlertNotificationWrapper> notificationsToSave = new LinkedHashSet<>();

            final List<ProjectV0Resource> projectResources = projectService.getAllProjects();
            logger.info("{} remote projects", projectResources.size());
            for (final ProjectV0Resource projectResource : projectResources) {
                logger.debug("Processing project {}", projectResource.getAttributes().getName());
                final List<BranchV0Resource> projectBranches = branchService.getBranchesForProject(projectResource.getId());
                logger.debug("Found {} branches for project {}", projectBranches.size(), projectResource.getAttributes().getName());
                final ProviderProject projectModel = convertToProviderProject(polarisApiHelper, projectResource, projectBranches);

                logger.debug("Gathering emails for project: {}", projectModel.getName());
                final Set<String> projectUserEmails = polarisApiHelper.getAllEmailsForProject(projectResource);
                projectUserEmailMappings.put(projectModel, projectUserEmails);
                logger.debug("Found {} users for project {}", projectUserEmails.size(), projectResource.getAttributes().getName());

                Set<PolarisIssueModel> issuesForProject = createIssuesForProject(polarisApiHelper, projectResource, projectModel, projectBranches);
                logger.debug("Creating {} issues for project {}", issuesForProject.size(), projectResource.getAttributes().getName());
                issuesForProject = updatePreviousCounts(projectModel.getHref(), issuesForProject);
                issuesToUpdate.put(projectModel, issuesForProject);

                final String projectLink = polarisApiHelper.createLinkToProject(polarisUrl, projectResource.getId()).orElse(projectModel.getHref());
                final Set<AlertNotificationWrapper> newNotificationsForProject = createNotificationContentForProject(projectLink, projectModel, issuesForProject);
                notificationsToSave.addAll(newNotificationsForProject);
            }

            persistProjectData(projectUserEmailMappings);
            persistIssues(issuesToUpdate);
            persistNotifications(notificationsToSave);
        } catch (final IntegrationException e) {
            logger.error("Problem communicating with Polaris", e);
        }
    }

    private ProviderProject convertToProviderProject(final PolarisApiHelper polarisApiHelper, final ProjectV0Resource project, final List<BranchV0Resource> branches) throws IntegrationException {
        final String branchesString = branches
                                          .stream()
                                          .map(BranchV0Resource::getAttributes)
                                          .map(BranchV0Attributes::getName)
                                          .collect(Collectors.joining(", "));
        final String name = project.getAttributes().getName();
        final String description = StringUtils.truncate(branchesString, 80);
        final String href = project.getLinks().getSelf().getHref();
        final String projectOwnerEmail = polarisApiHelper.getAdminEmailForProject(project).orElse(StringUtils.EMPTY);

        return new ProviderProject(name, description, href, projectOwnerEmail);
    }

    private Set<PolarisIssueModel> createIssuesForProject(final PolarisApiHelper polarisApiHelper, final ProjectV0Resource projectResource, final ProviderProject projectModel, final List<BranchV0Resource> branches) {
        final String projectId = projectResource.getId();
        final List<String> branchIds = polarisApiHelper.getBranchesIdsForProject(branches);

        return polarisApiHelper.createIssueModelsForProject(projectId, projectModel.getName(), branchIds);
    }

    private Set<AlertNotificationWrapper> createNotificationContentForProject(final String projectLink, final ProviderProject project, final Set<PolarisIssueModel> projectIssues) {
        final Set<AlertNotificationWrapper> notifications = new LinkedHashSet<>();
        final Date providerCreationDate = new Date();
        final Collection<AlertPolarisIssueNotificationContentModel> notificationModels = createNotificationModelsForProject(projectLink, project, projectIssues);
        for (final AlertPolarisIssueNotificationContentModel notificationModel : notificationModels) {
            final String notificationContent = gson.toJson(notificationModel);
            final AlertNotificationWrapper notification = new NotificationContent(providerCreationDate, PolarisProvider.COMPONENT_NAME, providerCreationDate, notificationModel.getNotificationType().name(), notificationContent);
            notifications.add(notification);
        }
        return notifications;
    }

    private Collection<AlertPolarisIssueNotificationContentModel> createNotificationModelsForProject(final String projectLink, final ProviderProject project, final Set<PolarisIssueModel> issues) {
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
                    new AlertPolarisIssueNotificationContentModel(notificationType, project.getName(), project.getDescription(), projectLink, issue.getIssueType(), numberChanged, issue.getCurrentIssueCount());
                notifications.add(newNotification);
            }
        }
        return notifications;
    }

    private void persistProjectData(final Map<ProviderProject, Set<String>> projectUserEmailMappings) {
        logger.info("Updating {} projects", projectUserEmailMappings.keySet().size());
        providerDataAccessor.updateProjectAndUserData(PolarisProvider.COMPONENT_NAME, projectUserEmailMappings);
    }

    private void persistIssues(final Map<ProviderProject, Set<PolarisIssueModel>> projectIssueMappings) {
        for (final Map.Entry<ProviderProject, Set<PolarisIssueModel>> projectToIssues : projectIssueMappings.entrySet()) {
            final ProviderProject project = projectToIssues.getKey();
            for (final PolarisIssueModel issueFromServer : projectToIssues.getValue()) {
                try {
                    logger.trace("Updating Polaris issue: {}", issueFromServer);
                    polarisIssueAccessor.updateIssueType(project.getHref(), issueFromServer.getIssueType(), issueFromServer.getCurrentIssueCount());
                } catch (final AlertDatabaseConstraintException e) {
                    logger.error("Problem updating issue type", e);
                }
            }
        }
    }

    private void persistNotifications(final Collection<AlertNotificationWrapper> notificationsToSave) {
        logger.info("Generating {} new notifications", notificationsToSave.size());
        notificationManager.saveAllNotifications(notificationsToSave);
    }

    private Set<PolarisIssueModel> updatePreviousCounts(final String projectHref, final Set<PolarisIssueModel> polarisIssueModels) throws AlertDatabaseConstraintException {
        final Set<PolarisIssueModel> updatedPolarisIssues = new HashSet<>();
        final Optional<ProviderProject> optionalProviderProject = providerDataAccessor.findFirstByHref(projectHref);
        if (optionalProviderProject.isEmpty()) {
            // This project is not yet in the database. Nothing to update.
            return polarisIssueModels;
        }

        for (final PolarisIssueModel polarisIssueModel : polarisIssueModels) {
            final Optional<PolarisIssueModel> optionalStoredProjectIssue = polarisIssueAccessor.getProjectIssueByIssueType(projectHref, polarisIssueModel.getIssueType());
            if (optionalStoredProjectIssue.isPresent()) {
                final PolarisIssueModel storedPolarisIssueModel = optionalStoredProjectIssue.get();
                updatedPolarisIssues.add(new PolarisIssueModel(polarisIssueModel.getIssueType(), storedPolarisIssueModel.getCurrentIssueCount(), polarisIssueModel.getCurrentIssueCount()));
            } else {
                updatedPolarisIssues.add(polarisIssueModel);
            }
        }
        return updatedPolarisIssues;
    }

}
