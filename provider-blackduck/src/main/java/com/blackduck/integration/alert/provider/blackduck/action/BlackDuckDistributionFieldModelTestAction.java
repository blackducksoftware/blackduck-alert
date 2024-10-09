/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.common.model.errors.FieldStatusSeverity;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.api.provider.state.StatefulProvider;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProvider;
import com.blackduck.integration.alert.provider.blackduck.validator.BlackDuckApiTokenValidator;
import com.blackduck.integration.alert.provider.blackduck.validator.BlackDuckSystemValidator;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.alert.common.action.FieldModelTestAction;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.FieldUtility;
import com.blackduck.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.persistence.model.ProviderProject;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.rest.model.FieldModel;

@Component
public class BlackDuckDistributionFieldModelTestAction extends FieldModelTestAction {
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckDistributionFieldModelTestAction.class);

    private final ProviderDataAccessor blackDuckDataAccessor;
    private final BlackDuckProvider blackDuckProvider;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final BlackDuckSystemValidator blackDuckSystemValidator;

    @Autowired
    public BlackDuckDistributionFieldModelTestAction(
        ProviderDataAccessor blackDuckDataAccessor,
        BlackDuckProvider blackDuckProvider,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        BlackDuckSystemValidator blackDuckSystemValidator) {
        this.blackDuckDataAccessor = blackDuckDataAccessor;
        this.blackDuckProvider = blackDuckProvider;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.blackDuckSystemValidator = blackDuckSystemValidator;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        ArrayList<AlertFieldStatus> fieldStatuses = new ArrayList<>();
        Optional<Long> optionalProviderConfigId = registeredFieldValues.getLong(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
        if (optionalProviderConfigId.isPresent()) {
            Long providerConfigId = optionalProviderConfigId.get();
            boolean canConnect = isAbleToConnect(providerConfigId);
            if (!canConnect) {
                fieldStatuses.add(AlertFieldStatus.error(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, "Unable to establish connection with BlackDuck."));
            }
            boolean filterByProjects = registeredFieldValues.getBoolean(ProviderDescriptor.KEY_FILTER_BY_PROJECT).orElse(false);
            // this part of the validation will attempt to connect to the Black Duck instance so only perform this if Alert can connect.
            if (canConnect && filterByProjects) {
                Collection<String> configuredProjects = registeredFieldValues.getAllStrings(ProviderDescriptor.KEY_CONFIGURED_PROJECT);
                validateSelectedProjectExists(providerConfigId, configuredProjects).ifPresent(fieldStatuses::add);
                Optional<String> optionalProjectNamePattern = registeredFieldValues.getString(ProviderDescriptor.KEY_PROJECT_NAME_PATTERN);
                optionalProjectNamePattern
                    .flatMap(projectNamePattern -> validatePatternMatchesProject(providerConfigId, projectNamePattern))
                    .ifPresent(fieldStatuses::add);
                registeredFieldValues.getString(ProviderDescriptor.KEY_PROJECT_VERSION_NAME_PATTERN)
                    .flatMap(projectVersionNamePattern -> validatePatternMatchesProjectVersion(
                        providerConfigId,
                        projectVersionNamePattern,
                        optionalProjectNamePattern.orElse(null),
                        configuredProjects
                    ))
                    .ifPresent(fieldStatuses::add);
            }
        } else {
            fieldStatuses.add(AlertFieldStatus.error(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, "A provider configuration is required"));
        }

        if (MessageResult.hasFieldStatusBySeverity(fieldStatuses, FieldStatusSeverity.ERROR)) {
            return new MessageResult("There were errors with the Black Duck provider fields", fieldStatuses);
        }
        return new MessageResult("Successfully tested Black Duck provider fields", fieldStatuses);
    }

    private boolean isAbleToConnect(Long providerConfigId) throws AlertException {
        BlackDuckProperties blackDuckProperties = null;
        Optional<ConfigurationModel> providerConfigurationOptional = configurationModelConfigurationAccessor.getConfigurationById(providerConfigId);
        if (providerConfigurationOptional.isPresent()) {
            ConfigurationModel providerConfiguration = providerConfigurationOptional.get();
            StatefulProvider statefulProvider = blackDuckProvider.createStatefulProvider(providerConfiguration);
            blackDuckProperties = (BlackDuckProperties) statefulProvider.getProperties();

        }
        boolean canConnect = false;
        if (null != blackDuckProperties) {
            canConnect = blackDuckSystemValidator.canConnect(blackDuckProperties, new BlackDuckApiTokenValidator(blackDuckProperties));
        }
        return canConnect;
    }

    private Optional<AlertFieldStatus> validatePatternMatchesProjectVersion(
        Long providerConfigId,
        String projectVersionNamePattern,
        String projectNamePattern,
        Collection<String> configuredProjects
    ) {
        if (StringUtils.isNotBlank(projectVersionNamePattern)) {
            Pattern compiledProjectVersionPattern = Pattern.compile(projectVersionNamePattern);
            Pattern compiledProjectNamePattern = StringUtils.isNotBlank(projectNamePattern) ? Pattern.compile(projectNamePattern) : null;

            int currentPage = AlertPagedModel.DEFAULT_PAGE_NUMBER;
            AlertPagedModel<String> projectsByProviderConfigId = filterAndMapHrefs(providerConfigId, currentPage, configuredProjects, compiledProjectNamePattern);
            boolean foundResult = false;
            while (!foundResult && currentPage < projectsByProviderConfigId.getTotalPages()) {
                List<String> providerProjects = projectsByProviderConfigId.getModels();
                foundResult = providerProjects.stream().anyMatch(href -> iteratePagesAndCheck(versionCurrentPage -> blackDuckDataAccessor.getProjectVersionNamesByHref(providerConfigId, href, versionCurrentPage),
                    versionNames -> versionNames.stream().anyMatch(versionName -> compiledProjectVersionPattern.matcher(versionName).matches()),
                    Boolean.FALSE).isEmpty());
                currentPage++;
                projectsByProviderConfigId = filterAndMapHrefs(providerConfigId, currentPage, configuredProjects, compiledProjectNamePattern);
            }

            if (!foundResult) {
                return Optional.of(AlertFieldStatus.warning(ProviderDescriptor.KEY_PROJECT_VERSION_NAME_PATTERN, "Does not match any of the project versions."));
            }
        }
        return Optional.empty();
    }

    private AlertPagedModel<String> filterAndMapHrefs(Long providerConfigId, int currentPage, Collection<String> validProjects, @Nullable Pattern projectNamePattern) {
        Predicate<ProviderProject> filterFunction = validProjects.isEmpty() ? project -> true : project -> validProjects.contains(project.getName());
        Predicate<ProviderProject> filterProjectNamePattern = (projectNamePattern == null) ? project -> true : project -> projectNamePattern.matcher(project.getName()).matches();
        AlertPagedModel<ProviderProject> projectsByProviderConfigId = blackDuckDataAccessor.getProjectsByProviderConfigId(
            providerConfigId,
            currentPage,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            ""
        );
        List<String> hrefs = projectsByProviderConfigId.getModels()
            .stream()
            .filter(filterFunction)
            .filter(filterProjectNamePattern)
            .map(ProviderProject::getHref)
            .collect(Collectors.toList());
        return new AlertPagedModel<>(projectsByProviderConfigId.getTotalPages(), projectsByProviderConfigId.getCurrentPage(), projectsByProviderConfigId.getPageSize(), hrefs);
    }

    private Optional<AlertFieldStatus> validatePatternMatchesProject(Long providerConfigId, String projectNamePattern) {
        if (StringUtils.isNotBlank(projectNamePattern)) {
            Pattern pattern = Pattern.compile(projectNamePattern);
            return iteratePagesAndCheck(
                currentPage -> blackDuckDataAccessor.getProjectsByProviderConfigId(providerConfigId, currentPage, AlertPagedModel.DEFAULT_PAGE_SIZE, ""),
                providerProjects -> providerProjects.stream().anyMatch(databaseEntity -> pattern.matcher(databaseEntity.getName()).matches()),
                AlertFieldStatus.warning(ProviderDescriptor.KEY_PROJECT_NAME_PATTERN, "Does not match any of the Projects.")
            );
        }
        return Optional.empty();
    }

    private Optional<AlertFieldStatus> validateSelectedProjectExists(Long providerConfigId, Collection<String> selectedProjects) {
        AlertPagedModel<ProviderProject> projectsByProviderConfigId = blackDuckDataAccessor.getProjectsByProviderConfigId(
            providerConfigId,
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            ""
        );
        Set<String> projectNames = projectsByProviderConfigId.getModels()
            .stream()
            .map(ProviderProject::getName)
            .collect(Collectors.toSet());
        List<String> invalidProjects = selectedProjects.stream()
            .filter(Predicate.not(projectNames::contains))
            .collect(Collectors.toList());
        int totalPages = projectsByProviderConfigId.getTotalPages();
        int currentPage = projectsByProviderConfigId.getCurrentPage();
        while (!invalidProjects.isEmpty() && currentPage < totalPages) {
            currentPage++;
            projectNames = blackDuckDataAccessor.getProjectsByProviderConfigId(providerConfigId, currentPage, AlertPagedModel.DEFAULT_PAGE_SIZE, "")
                .getModels()
                .stream()
                .map(ProviderProject::getName)
                .collect(Collectors.toSet());
            invalidProjects = invalidProjects.stream().filter(Predicate.not(projectNames::contains)).collect(Collectors.toList());
        }
        if (!invalidProjects.isEmpty()) {
            return Optional.of(AlertFieldStatus.warning(
                ProviderDescriptor.KEY_CONFIGURED_PROJECT,
                "The selected projects could not be found: " + String.join(", ", invalidProjects)
            ));
        }
        return Optional.empty();
    }

    private <U, T extends AlertPagedModel<U>, R> Optional<R> iteratePagesAndCheck(
        Function<Integer, T> getData,
        Function<Collection<U>, Boolean> findResult,
        R missingContentResult
    ) {
        int currentPage = AlertPagedModel.DEFAULT_PAGE_NUMBER;
        T retrievedData = getData.apply(currentPage);
        int totalPages = retrievedData.getTotalPages();
        while (currentPage < totalPages) {
            logger.debug("Getting page {} out of {}", currentPage + 1, totalPages);
            List<U> models = retrievedData.getModels();
            if (findResult.apply(models)) {
                return Optional.empty();
            }
            currentPage++;
            retrievedData = getData.apply(currentPage);
        }

        return Optional.of(missingContentResult);
    }

}
