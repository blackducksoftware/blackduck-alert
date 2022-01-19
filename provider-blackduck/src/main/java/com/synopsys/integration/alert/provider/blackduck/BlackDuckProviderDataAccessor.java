/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckQuery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectUsersService;
import com.synopsys.integration.blackduck.service.dataservice.UserService;
import com.synopsys.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingSupplier;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpUrl;

@Component
public class BlackDuckProviderDataAccessor implements ProviderDataAccessor {
    private static final int PROJECT_DESCRIPTION_MAX_CHARS = 256;

    private final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(getClass()));
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;

    @Autowired
    public BlackDuckProviderDataAccessor(ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor, BlackDuckPropertiesFactory blackDuckPropertiesFactory) {
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
    }

    @Override
    public Optional<ProviderProject> getProjectByHref(Long providerConfigId, String projectHref) {
        try {
            Optional<ConfigurationModel> providerConfigOptional = configurationModelConfigurationAccessor.getConfigurationById(providerConfigId);
            if (providerConfigOptional.isPresent()) {
                BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(providerConfigOptional.get());
                BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();

                ProjectView foundProject = blackDuckApiClient.getResponse(new HttpUrl(projectHref), ProjectView.class);
                return convertBlackDuckProjects(List.of(foundProject), blackDuckApiClient).stream().findFirst();
            }
        } catch (IntegrationException e) {
            logger.errorAndDebug(createProjectNotFoundString(providerConfigId, e.getMessage()), e);
        }
        return Optional.empty();
    }

    @Override
    public List<ProviderProject> getProjectsByProviderConfigName(String providerConfigName) {
        try {
            Optional<ConfigurationModel> providerConfigOptional = configurationModelConfigurationAccessor.getProviderConfigurationByName(providerConfigName);
            if (providerConfigOptional.isPresent()) {
                return getProjectsForProvider(providerConfigOptional.get());
            }
        } catch (IntegrationException e) {
            logger.errorAndDebug(createProjectNotFoundString(providerConfigName, e.getMessage()), e);
        }
        return List.of();
    }

    @Override
    public List<ProviderProject> getProjectsByProviderConfigId(Long providerConfigId) {
        return configurationModelConfigurationAccessor.getConfigurationById(providerConfigId)
            .flatMap(providerConfig -> retrieveOptionalProjectData(() -> getProjectsForProvider(providerConfig)))
            .orElse(List.of());
    }

    @Override
    public AlertPagedModel<ProviderProject> getProjectsByProviderConfigId(Long providerConfigId, int pageNumber, int pageSize, String searchTerm) {
        return configurationModelConfigurationAccessor.getConfigurationById(providerConfigId)
            .flatMap(providerConfig -> retrieveOptionalProjectData(() -> retrieveProjectsForProvider(providerConfig, pageNumber, pageSize, searchTerm)))
            .orElse(AlertPagedModel.empty(pageNumber, pageSize));
    }

    @Override
    public AlertPagedModel<String> getProjectVersionNamesByHref(Long providerConfigId, String projectHref, int pageNumber) {
        Optional<ConfigurationModel> providerConfigOptional = configurationModelConfigurationAccessor.getConfigurationById(providerConfigId);
        if (providerConfigOptional.isPresent()) {
            try {
                BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(providerConfigOptional.get());
                BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
                ProjectView foundProject = blackDuckApiClient.getResponse(new HttpUrl(projectHref), ProjectView.class);

                BlackDuckPageDefinition blackDuckPageDefinition = new BlackDuckPageDefinition(BlackDuckRequestBuilder.DEFAULT_LIMIT, pageNumber * BlackDuckRequestBuilder.DEFAULT_LIMIT);
                BlackDuckMultipleRequest<ProjectVersionView> projectVersionSpec = new BlackDuckRequestBuilder()
                    .commonGet()
                    .setBlackDuckPageDefinition(blackDuckPageDefinition)
                    .buildBlackDuckRequest(foundProject.metaVersionsLink());
                BlackDuckPageResponse<ProjectVersionView> pageResponse = blackDuckApiClient.getPageResponse(projectVersionSpec);
                return new AlertPagedModel<>(pageResponse.getTotalCount(), pageNumber, BlackDuckRequestBuilder.DEFAULT_LIMIT, pageResponse.getItems()).transformContent(ProjectVersionView::getVersionName);
            } catch (IntegrationException e) {
                logger.errorAndDebug(createProjectNotFoundString(providerConfigId, e.getMessage()), e);
            }
        }
        return AlertPagedModel.empty(pageNumber, BlackDuckRequestBuilder.DEFAULT_LIMIT);
    }

    @Override
    public void deleteProjects(Collection<ProviderProject> providerProjects) {
        //ignored since we are not using the database
    }

    @Override
    public Set<String> getEmailAddressesForProjectHref(Long providerConfigId, String projectHref) {
        Optional<ConfigurationModel> providerConfigOptional = configurationModelConfigurationAccessor.getConfigurationById(providerConfigId);
        if (providerConfigOptional.isPresent()) {
            try {
                BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(providerConfigOptional.get());
                BlackDuckApiClient blackDuckService = blackDuckServicesFactory.getBlackDuckApiClient();
                ProjectView projectView = blackDuckService.getResponse(new HttpUrl(projectHref), ProjectView.class);
                return getEmailAddressesForProject(projectView, blackDuckServicesFactory.createProjectUsersService());
            } catch (IntegrationException e) {
                logger.errorAndDebug(createProjectNotFoundString(providerConfigId, e.getMessage()), e);
            }
        }
        return Set.of();
    }

    @Override
    public ProviderUserModel getProviderConfigUserById(Long providerConfigId) throws AlertConfigurationException {
        try {
            Optional<ConfigurationModel> providerConfigOptional = configurationModelConfigurationAccessor.getConfigurationById(providerConfigId);
            if (providerConfigOptional.isPresent()) {
                BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(providerConfigOptional.get());
                UserService userService = blackDuckServicesFactory.createUserService();

                UserView providerConfigUser = userService.findCurrentUser();
                return new ProviderUserModel(providerConfigUser.getEmail(), false);
            }
        } catch (IntegrationException e) {
            throw new AlertConfigurationException(createUserNotFoundString(providerConfigId, e.getMessage()), e);
        }
        throw new AlertConfigurationException(String.format("The provider config with id '%s' is invalid", providerConfigId));
    }

    @Override
    public List<ProviderUserModel> getUsersByProviderConfigId(Long providerConfigId) {
        if (null == providerConfigId) {
            return List.of();
        }

        Optional<ConfigurationModel> providerConfigOptional = configurationModelConfigurationAccessor.getConfigurationById(providerConfigId);
        if (providerConfigOptional.isPresent()) {
            try {
                return getEmailAddressesByProvider(providerConfigOptional.get());
            } catch (IntegrationException e) {
                logger.errorAndDebug(createProjectNotFoundString(providerConfigId, e.getMessage()), e);
            }
        }
        return List.of();
    }

    @Override
    public AlertPagedModel<ProviderUserModel> getUsersByProviderConfigId(Long providerConfigId, int pageNumber, int pageSize, String searchTerm) {
        return configurationModelConfigurationAccessor.getConfigurationById(providerConfigId)
            .flatMap(providerConfig -> retrieveOptionalProjectData(() -> retrieveUsersForProvider(providerConfig, pageNumber, pageSize, searchTerm)))
            .orElse(AlertPagedModel.empty(pageNumber, pageSize));
    }

    @Override
    public List<ProviderUserModel> getUsersByProviderConfigName(String providerConfigName) {
        if (StringUtils.isBlank(providerConfigName)) {
            return List.of();
        }
        try {
            Optional<ConfigurationModel> providerConfigOptional = configurationModelConfigurationAccessor.getProviderConfigurationByName(providerConfigName);
            if (providerConfigOptional.isPresent()) {
                return getEmailAddressesByProvider(providerConfigOptional.get());
            }
        } catch (IntegrationException e) {
            logger.errorAndDebug(createProjectNotFoundString(providerConfigName, e.getMessage()), e);
        }
        return List.of();
    }

    @Override
    public Optional<ProviderUserModel> findFirstUserByEmailAddress(Long providerConfigId, String emailAddress) {
        Optional<ConfigurationModel> providerConfigOptional = configurationModelConfigurationAccessor.getConfigurationById(providerConfigId);
        if (providerConfigOptional.isPresent()) {
            try {
                BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(providerConfigOptional.get());
                UserService userService = blackDuckServicesFactory.createUserService();
                return userService.findUsersByEmail(emailAddress, new BlackDuckPageDefinition(1, 0))
                    .getItems()
                    .stream()
                    .map(userView -> new ProviderUserModel(userView.getEmail(), false))
                    .findFirst();
            } catch (IntegrationException e) {
                logger.errorAndDebug(createProjectNotFoundString(providerConfigId, e.getMessage()), e);
            }
        }
        return Optional.empty();
    }

    @Override
    public void updateProjectAndUserData(Long providerConfigId, Map<ProviderProject, Set<String>> projectToUserData, Set<String> additionalRelevantUsers) {
        //ignored since are not updating the database
    }

    private <T> Optional<T> retrieveOptionalProjectData(ThrowingSupplier<T, IntegrationException> retriever) {
        try {
            return Optional.of(retriever.get());
        } catch (IntegrationException e) {
            logger.errorAndDebug(String.format("Could not get the requested projects. %s", e.getMessage()), e);
        }
        return Optional.empty();
    }

    private List<ProviderProject> getProjectsForProvider(ConfigurationModel blackDuckConfigurationModel) throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckConfigurationModel);
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        List<ProjectView> allProjects = projectService.getAllProjects();
        return convertBlackDuckProjects(allProjects, blackDuckServicesFactory.getBlackDuckApiClient());
    }

    private AlertPagedModel<ProviderProject> retrieveProjectsForProvider(ConfigurationModel blackDuckConfigurationModel, int pageNumber, int pageSize, String searchTerm) throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckConfigurationModel);
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();

        BlackDuckQuery nameQuery = new BlackDuckQuery("name", searchTerm);
        BlackDuckPageDefinition blackDuckPageDefinition = new BlackDuckPageDefinition(pageSize, pageNumber * pageSize);
        BlackDuckMultipleRequest<ProjectView> projectSpec = new BlackDuckRequestBuilder()
            .commonGet()
            .addBlackDuckQuery(nameQuery)
            .setBlackDuckPageDefinition(blackDuckPageDefinition)
            .buildBlackDuckRequest(apiDiscovery.metaProjectsLink());
        BlackDuckPageResponse<ProjectView> pageOfProjects = blackDuckApiClient.getPageResponse(projectSpec);

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

        ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();
        BlackDuckPageResponse<UserView> pageOfUsers = retrieveBlackDuckPageResponse(blackDuckServicesFactory, apiDiscovery.metaUsersLink(), pageNumber, pageSize, searchFilter);

        List<ProviderUserModel> foundUsers = pageOfUsers.getItems()
            .stream()
            .map(UserView::getEmail)
            .map(email -> new ProviderUserModel(email, false))
            .collect(Collectors.toList());
        // Due to a limitation in the blackduck-common library, the totalCount in the BlackDuckPageResponse does not represent the count the matches the searchFilter. It is the totalCount from Black Duck
        int totalPageCount = computeTotalCount(pageOfUsers, pageSize);
        return new AlertPagedModel<>(totalPageCount, pageNumber, pageSize, foundUsers);
    }

    private List<ProviderUserModel> getEmailAddressesByProvider(ConfigurationModel blackDuckConfiguration) throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckConfiguration);
        BlackDuckApiClient blackDuckService = blackDuckServicesFactory.getBlackDuckApiClient();
        ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();
        Set<String> allActiveBlackDuckEmailAddresses = getAllActiveBlackDuckEmailAddresses(blackDuckService, apiDiscovery);
        return allActiveBlackDuckEmailAddresses.stream()
            .map(email -> new ProviderUserModel(email, false))
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
                logger.errorAndDebug(createProjectOwnerNotFoundString(projectView.getName(), e.getMessage()), e);
            }
        }

        String truncatedDescription = truncateDescription(projectView.getDescription());
        return new ProviderProject(projectView.getName(), truncatedDescription, projectView.getMeta().getHref().toString(), projectOwnerEmail);
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

    private Set<String> getAllActiveBlackDuckEmailAddresses(BlackDuckApiClient blackDuckService, ApiDiscovery apiDiscovery) throws IntegrationException {
        return blackDuckService.getAllResponses(apiDiscovery.metaUsersLink())
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
        UrlMultipleResponses<T> urlMultipleResponses,
        int pageNumber,
        int pageSize,
        Predicate<T> searchFilter
    ) throws IntegrationException {
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = blackDuckServicesFactory.getBlackDuckResponsesTransformer();

        int offset = pageNumber * pageSize;
        BlackDuckMultipleRequest<T> spec = new BlackDuckRequestBuilder()
            .commonGet()
            .setLimitAndOffset(pageSize, offset)
            .buildBlackDuckRequest(urlMultipleResponses);
        return blackDuckResponsesTransformer.getSomeMatchingResponses(spec, searchFilter, pageSize);
    }

    private String truncateDescription(@Nullable String originalDescription) {
        String trimmedDescription = StringUtils.trimToEmpty(originalDescription);
        if (trimmedDescription.length() > PROJECT_DESCRIPTION_MAX_CHARS) {
            return StringUtils.truncate(trimmedDescription, PROJECT_DESCRIPTION_MAX_CHARS) + ". . .";
        }
        return trimmedDescription;
    }

    // Abstract error-message string formatting

    private String createProjectNotFoundString(String providerConfigName, String message) {
        return String.format("Could not get the project for the provider '%s'. %s", providerConfigName, message);
    }

    private String createProjectNotFoundString(Long providerConfigId, String message) {
        return String.format("Could not get the project for the provider with id '%s'. %s", providerConfigId, message);
    }

    private String createProjectOwnerNotFoundString(String projectName, String message) {
        return String.format("Could not get the project owner for Project: %s. Error: %s", projectName, message);
    }

    private String createUserNotFoundString(Long providerConfigId, String message) {
        return String.format("Could not get user for the provider config with id '%s'. %s", providerConfigId, message);
    }

}
