/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.action.task;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.rest.model.JobProviderProjectFieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckCacheHttpClientCache;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectUsersService;
import com.synopsys.integration.blackduck.service.dataservice.UserService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class AddUserToProjectsRunnable implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BlackDuckCacheHttpClientCache blackDuckHttpClientCache;
    private final BlackDuckProperties blackDuckProperties;
    private final boolean filterByProject;
    private final Collection<JobProviderProjectFieldModel> blackDuckProjectModels;

    public AddUserToProjectsRunnable(BlackDuckCacheHttpClientCache blackDuckHttpClientCache, BlackDuckProperties blackDuckProperties, boolean filterByProject,
        Collection<JobProviderProjectFieldModel> blackDuckProjectModels) {
        this.blackDuckHttpClientCache = blackDuckHttpClientCache;
        this.blackDuckProperties = blackDuckProperties;
        this.filterByProject = filterByProject;
        this.blackDuckProjectModels = blackDuckProjectModels;
    }

    @Override
    public void run() {
        try {
            Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
            BlackDuckHttpClient blackDuckHttpClient = blackDuckHttpClientCache.retrieveOrCreateBlackDuckCacheHttpClient(blackDuckProperties.getConfigId());
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, intLogger);

            BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
            UserService userService = blackDuckServicesFactory.createUserService();
            ProjectService projectService = blackDuckServicesFactory.createProjectService();
            ProjectUsersService projectUsersService = blackDuckServicesFactory.createProjectUsersService();

            UserView currentUser = userService.findCurrentUser();
            List<ProjectView> projectViews = filterByProject ? getTheseProjects(blackDuckApiClient, blackDuckProjectModels) : projectService.getAllProjects();
            updateBlackDuckProjectPermissions(projectUsersService, currentUser, projectViews);
        } catch (Exception e) {
            logger.warn("{} failed: {}", getClass().getSimpleName(), e.getMessage());
        }
    }

    private List<ProjectView> getTheseProjects(BlackDuckApiClient blackDuckApiClient, Collection<JobProviderProjectFieldModel> jobProviderProjectFieldModels) throws IntegrationException {
        Set<String> hrefStrings = jobProviderProjectFieldModels.stream()
                                      .map(JobProviderProjectFieldModel::getHref)
                                      .collect(Collectors.toSet());
        List<ProjectView> projects = new LinkedList<>();
        for (String href : hrefStrings) {
            HttpUrl httpUrl = null;
            try {
                httpUrl = new HttpUrl(href);
            } catch (IntegrationException e) {
                logger.warn("{} could not get the project for the URL: {}. {}", getClass().getSimpleName(), href, e.getMessage());
            }
            if (null != httpUrl) {
                projects.add(blackDuckApiClient.getResponse(httpUrl, ProjectView.class));
            }
        }
        return projects;
    }

    private void updateBlackDuckProjectPermissions(ProjectUsersService projectUsersService, UserView userToAdd, List<ProjectView> projectViews) throws IntegrationException {
        for (ProjectView projectView : projectViews) {
            logger.debug("Adding user to Project {}", projectView.getName());
            projectUsersService.addUserToProject(projectView, userToAdd);
        }
    }

}
