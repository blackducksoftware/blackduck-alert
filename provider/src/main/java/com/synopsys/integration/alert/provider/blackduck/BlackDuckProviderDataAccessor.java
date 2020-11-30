/**
 * provider
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
package com.synopsys.integration.alert.provider.blackduck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.PagedRequest;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectUsersService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingSupplier;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;

@Component
public class BlackDuckProviderDataAccessor implements ProviderDataAccessor {
    private final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(BlackDuckProviderDataAccessor.class));
    private final ConfigurationAccessor configurationAccessor;
    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;

    @Autowired
    public BlackDuckProviderDataAccessor(ConfigurationAccessor configurationAccessor, BlackDuckPropertiesFactory blackDuckPropertiesFactory) {
        this.configurationAccessor = configurationAccessor;
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
    }

    @Override
    public List<ProviderProject> getProjectsByProviderConfigName(String providerConfigName) {
        try {
            Optional<ConfigurationModel> providerConfigOptional = configurationAccessor.getProviderConfigurationByName(providerConfigName);
            if (providerConfigOptional.isPresent()) {
                return getProjectsForProvider(providerConfigOptional.get());
            }
        } catch (IntegrationException e) {
            logger.error(String.format("Could not get the project for the provider '%s'. %s", providerConfigName, e.getMessage()));
            logger.debug(e.getMessage(), e);
        }
        return List.of();
    }

    @Override
    public List<ProviderProject> getProjectsByProviderConfigId(Long providerConfigId) {
        return configurationAccessor.getConfigurationById(providerConfigId)
                   .flatMap(providerConfig -> retrieveOptionalProjectData(() -> getProjectsForProvider(providerConfig)))
                   .orElse(List.of());
    }

    @Override
    public AlertPagedModel<ProviderProject> getProjectsByProviderConfigId(Long providerConfigId, int pageNumber, int pageSize, String searchTerm) {
        return configurationAccessor.getConfigurationById(providerConfigId)
                   .flatMap(providerConfig -> retrieveOptionalProjectData(() -> retrieveProjectsForProvider(providerConfig, pageNumber, pageSize, searchTerm)))
                   .orElse(new AlertPagedModel<>(0, pageNumber, pageSize, List.of()));
    }

    @Override
    public void deleteProjects(Collection<ProviderProject> providerProjects) {
        //ignored since we are not using the database
    }

    @Override
    public Set<String> getEmailAddressesForProjectHref(Long providerConfigId, String projectHref) {
        Optional<ConfigurationModel> providerConfigOptional = configurationAccessor.getConfigurationById(providerConfigId);
        if (providerConfigOptional.isPresent()) {
            try {
                BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(providerConfigOptional.get());
                BlackDuckApiClient blackDuckService = blackDuckServicesFactory.getBlackDuckService();
                ProjectView projectView = blackDuckService.getResponse(new HttpUrl(projectHref), ProjectView.class);
                return getEmailAddressesForProject(projectView, blackDuckServicesFactory.createProjectUsersService());
            } catch (IntegrationException e) {
                logger.error(String.format("Could not get the project for the provider with id '%s'. %s", providerConfigId, e.getMessage()));
                logger.debug(e.getMessage(), e);
            }
        }
        return Set.of();
    }

    @Override
    public List<ProviderUserModel> getUsersByProviderConfigId(Long providerConfigId) {
        if (null == providerConfigId) {
            return List.of();
        }

        Optional<ConfigurationModel> providerConfigOptional = configurationAccessor.getConfigurationById(providerConfigId);
        if (providerConfigOptional.isPresent()) {
            try {
                return getEmailAddressesByProvider(providerConfigOptional.get());
            } catch (IntegrationException e) {
                logger.error(String.format("Could not get the project for the provider with id '%s'. %s", providerConfigId, e.getMessage()));
                logger.debug(e.getMessage(), e);
            }
        }
        return List.of();
    }

    @Override
    public AlertPagedModel<ProviderUserModel> getUsersByProviderConfigId(Long providerConfigId, int pageNumber, int pageSize, String searchTerm) {
        return configurationAccessor.getConfigurationById(providerConfigId)
                   .flatMap(providerConfig -> retrieveOptionalProjectData(() -> retrieveUsersForProvider(providerConfig, pageNumber, pageSize, searchTerm)))
                   .orElse(new AlertPagedModel<>(0, pageNumber, pageSize, List.of()));
    }

    @Override
    public List<ProviderUserModel> getUsersByProviderConfigName(String providerConfigName) {
        if (StringUtils.isBlank(providerConfigName)) {
            return List.of();
        }
        try {
            Optional<ConfigurationModel> providerConfigOptional = configurationAccessor.getProviderConfigurationByName(providerConfigName);
            if (providerConfigOptional.isPresent()) {
                return getEmailAddressesByProvider(providerConfigOptional.get());
            }
        } catch (IntegrationException e) {
            logger.error(String.format("Could not get the project for the provider '%s'. %s", providerConfigName, e.getMessage()));
            logger.debug(e.getMessage(), e);
        }
        return List.of();
    }

    @Override
    public void updateProjectAndUserData(Long providerConfigId, Map<ProviderProject, Set<String>> projectToUserData, Set<String> additionalRelevantUsers) {
        //ignored since are not updating the database
    }

    private <T> Optional<T> retrieveOptionalProjectData(ThrowingSupplier<T, IntegrationException> retriever) {
        try {
            return Optional.of(retriever.get());
        } catch (IntegrationException e) {
            logger.error(String.format("Could not get the requested projects. %s", e.getMessage()));
            logger.debug(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private List<ProviderProject> getProjectsForProvider(ConfigurationModel blackDuckConfigurationModel) throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckConfigurationModel);
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        List<ProjectView> allProjects = projectService.getAllProjects();
        return convertBlackDuckProjects(allProjects, blackDuckServicesFactory.getBlackDuckService());
    }

    private AlertPagedModel<ProviderProject> retrieveProjectsForProvider(ConfigurationModel blackDuckConfigurationModel, int pageNumber, int pageSize, String searchTerm) throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckConfigurationModel);
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckService();

        HttpUrl projectsUrl = blackDuckApiClient.getUrl(ApiDiscovery.PROJECTS_LINK);
        BlackDuckRequestBuilder requestBuilder = new BlackDuckRequestBuilder(new Request.Builder())
                                                     .url(projectsUrl)
                                                     .addQueryParameter("q", "name:" + searchTerm);
        BlackDuckPageDefinition blackDuckPageDefinition = new BlackDuckPageDefinition(pageSize, pageNumber * pageSize);
        BlackDuckPageResponse<ProjectView> pageOfProjects = blackDuckApiClient.getPageResponse(requestBuilder, ProjectView.class, blackDuckPageDefinition);

        List<ProviderProject> foundProjects = convertBlackDuckProjects(pageOfProjects.getItems(), blackDuckApiClient);
        int totalPageCount = computeTotalCount(pageOfProjects, pageSize);
        return new AlertPagedModel<>(totalPageCount, pageNumber, pageSize, foundProjects);
    }

    private AlertPagedModel<ProviderUserModel> retrieveUsersForProvider(ConfigurationModel blackDuckConfigurationModel, int pageNumber, int pageSize, String searchTerm) throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckConfigurationModel);

        Predicate<UserView> searchFilter = userView -> StringUtils.isNotBlank(userView.getEmail());
        if (StringUtils.isNotBlank(searchTerm)) {
            searchFilter = searchFilter.and(userView -> StringUtils.containsIgnoreCase(userView.getEmail(), searchTerm));
        }

        BlackDuckPageResponse<UserView> pageOfUsers = retrieveBlackDuckPageResponse(blackDuckServicesFactory, ApiDiscovery.USERS_LINK_RESPONSE, pageNumber, pageSize, searchFilter);

        List<ProviderUserModel> foundProjects = pageOfUsers.getItems()
                                                    .stream()
                                                    .map(UserView::getEmail)
                                                    .map(email -> new ProviderUserModel(email, false))
                                                    .collect(Collectors.toList());
        int totalPageCount = computeTotalCount(pageOfUsers, pageSize);
        return new AlertPagedModel<>(totalPageCount, pageNumber, pageSize, foundProjects);
    }

    private List<ProviderUserModel> getEmailAddressesByProvider(ConfigurationModel blackDuckConfiguration) throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckConfiguration);
        BlackDuckApiClient blackDuckService = blackDuckServicesFactory.getBlackDuckService();
        Set<String> allActiveBlackDuckUserEmailAddresses = getAllActiveBlackDuckUserEmailAddresses(blackDuckService);
        return allActiveBlackDuckUserEmailAddresses.stream()
                   .map(emailAddress -> new ProviderUserModel(emailAddress, false))
                   .collect(Collectors.toList());
    }

    private List<ProviderProject> convertBlackDuckProjects(List<ProjectView> projectViews, BlackDuckApiClient blackDuckService) {
        List<ProviderProject> providerProjects = new ArrayList<>();
        for (ProjectView projectView : projectViews) {
            ProviderProject providerProject = createProviderProject(projectView, blackDuckService);
            providerProjects.add(providerProject);
        }
        return providerProjects;
    }

    private ProviderProject createProviderProject(ProjectView projectView, BlackDuckApiClient blackDuckService) {
        String projectOwnerEmail = null;
        if (StringUtils.isNotBlank(projectView.getProjectOwner())) {
            try {
                HttpUrl projectOwnerHttpUrl = new HttpUrl(projectView.getProjectOwner());
                UserView projectOwner = blackDuckService.getResponse(projectOwnerHttpUrl, UserView.class);
                projectOwnerEmail = projectOwner.getEmail();
            } catch (IntegrationException e) {
                logger.error(String.format("Could not get the project owner for Project: %s. Error: %s", projectView.getName(), e.getMessage()), e);
            }
        }
        return new ProviderProject(projectView.getName(), StringUtils.trimToEmpty(projectView.getDescription()), projectView.getMeta().getHref().toString(), projectOwnerEmail);
    }

    private BlackDuckServicesFactory createBlackDuckServicesFactory(ConfigurationModel blackDuckConfiguration) throws AlertException {
        BlackDuckProperties properties = blackDuckPropertiesFactory.createProperties(blackDuckConfiguration);
        BlackDuckHttpClient blackDuckHttpClient = properties.createBlackDuckHttpClient(logger);
        return properties.createBlackDuckServicesFactory(blackDuckHttpClient, logger);
    }

    private Set<String> getEmailAddressesForProject(ProjectView projectView, ProjectUsersService projectUsersService) throws IntegrationException {
        return projectUsersService.getAllActiveUsersForProject(projectView)
                   .stream()
                   .filter(UserView::getActive)
                   .map(UserView::getEmail)
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toSet());

    }

    private Set<String> getAllActiveBlackDuckUserEmailAddresses(BlackDuckApiClient blackDuckService) throws IntegrationException {
        return blackDuckService.getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE)
                   .stream()
                   .filter(UserView::getActive)
                   .map(UserView::getEmail)
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toSet());
    }

    private int computeTotalCount(BlackDuckPageResponse<?> blackDuckPageResponse, int pageSize) {
        return (blackDuckPageResponse.getTotalCount() + (pageSize - 1)) / pageSize;
    }

    private <T extends BlackDuckResponse> BlackDuckPageResponse<T> retrieveBlackDuckPageResponse(
        BlackDuckServicesFactory blackDuckServicesFactory,
        BlackDuckPathResponse<T> blackDuckPathResponse,
        int pageNumber,
        int pageSize,
        Predicate<T> searchFilter
    ) throws IntegrationException {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckService();
        BlackDuckRequestFactory requestFactory = blackDuckServicesFactory.getRequestFactory();

        int offset = pageNumber * pageSize;
        HttpUrl requestUrl = blackDuckApiClient.getUrl(blackDuckPathResponse.getBlackDuckPath());
        BlackDuckRequestBuilder blackDuckRequestBuilder = requestFactory.createCommonGetRequestBuilder().url(requestUrl);

        PagedRequest pagedRequest = new PagedRequest(blackDuckRequestBuilder, offset, pageSize);
        return retrievePage(blackDuckServicesFactory, blackDuckPathResponse.getResponseClass(), pagedRequest, searchFilter);
    }

    private <T extends BlackDuckResponse> BlackDuckPageResponse<T> retrievePage(BlackDuckServicesFactory blackDuckServicesFactory, Class<T> responseClass, PagedRequest pagedRequest, Predicate<T> searchFilter) throws IntegrationException {
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(blackDuckServicesFactory.getGson(), blackDuckServicesFactory.getObjectMapper(), blackDuckServicesFactory.getLogger());
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckServicesFactory.getBlackDuckHttpClient(), blackDuckJsonTransformer);
        return blackDuckResponsesTransformer.getSomeMatchingResponses(pagedRequest, responseClass, searchFilter, pagedRequest.getLimit());
    }

}
