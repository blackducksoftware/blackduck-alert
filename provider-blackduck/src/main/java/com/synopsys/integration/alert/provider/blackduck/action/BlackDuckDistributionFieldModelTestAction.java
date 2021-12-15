/*
 * provider-blackduck
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.api.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.action.FieldModelTestAction;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckApiTokenValidator;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class BlackDuckDistributionFieldModelTestAction extends FieldModelTestAction {
    private final ProviderDataAccessor blackDuckDataAccessor;
    private final BlackDuckProvider blackDuckProvider;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @Autowired
    public BlackDuckDistributionFieldModelTestAction(ProviderDataAccessor blackDuckDataAccessor, BlackDuckProvider blackDuckProvider, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor) {
        this.blackDuckDataAccessor = blackDuckDataAccessor;
        this.blackDuckProvider = blackDuckProvider;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        ArrayList<AlertFieldStatus> fieldStatuses = new ArrayList<>();
        Optional<Long> optionalProviderConfigId = registeredFieldValues.getLong(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
        if (optionalProviderConfigId.isPresent()) {
            Long providerConfigId = optionalProviderConfigId.get();
            boolean filterByProjects = registeredFieldValues.getBoolean(ProviderDescriptor.KEY_FILTER_BY_PROJECT).orElse(false);
            if (filterByProjects) {
                Collection<String> configuredProjects = registeredFieldValues.getAllStrings(ProviderDescriptor.KEY_CONFIGURED_PROJECT);
                validateSelectedProjectExists(providerConfigId, configuredProjects).ifPresent(fieldStatuses::add);
                registeredFieldValues.getString(ProviderDescriptor.KEY_PROJECT_NAME_PATTERN)
                    .flatMap(projectNamePattern -> validatePatternMatchesProject(providerConfigId, projectNamePattern))
                    .ifPresent(fieldStatuses::add);
            }

            BlackDuckProperties blackDuckProperties = null;
            Optional<ConfigurationModel> providerConfigurationOptional = configurationModelConfigurationAccessor.getConfigurationById(providerConfigId);
            if (providerConfigurationOptional.isPresent()) {
                ConfigurationModel providerConfiguration = providerConfigurationOptional.get();
                StatefulProvider statefulProvider = blackDuckProvider.createStatefulProvider(providerConfiguration);
                blackDuckProperties = (BlackDuckProperties) statefulProvider.getProperties();

            }
            if (null != blackDuckProperties) {
                BlackDuckApiTokenValidator blackDuckAPITokenValidator = new BlackDuckApiTokenValidator(blackDuckProperties);
                if (!blackDuckAPITokenValidator.isApiTokenValid()) {
                    fieldStatuses.add(AlertFieldStatus.error(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, "User permission failed, cannot read notifications from Black Duck."));
                }
            }
        } else {
            fieldStatuses.add(AlertFieldStatus.error(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, "A provider configuration is required"));
        }

        if (MessageResult.hasFieldStatusBySeverity(fieldStatuses, FieldStatusSeverity.ERROR)) {
            return new MessageResult("There were errors with the BlackDuck provider fields", fieldStatuses);
        }
        return new MessageResult("Successfully tested BlackDuck provider fields", fieldStatuses);
    }

    private Optional<AlertFieldStatus> validatePatternMatchesProject(Long providerConfigId, String projectNamePattern) {
        if (StringUtils.isNotBlank(projectNamePattern)) {
            return iteratePagesAndCheck(
                (currentPage) -> blackDuckDataAccessor.getProjectsByProviderConfigId(providerConfigId, currentPage, AlertPagedModel.DEFAULT_PAGE_SIZE, ""),
                (providerProjects) -> providerProjects.stream().anyMatch(databaseEntity -> databaseEntity.getName().matches(projectNamePattern)),
                AlertFieldStatus.warning(ProviderDescriptor.KEY_PROJECT_NAME_PATTERN, "Does not match any of the Projects.")
            );
        }
        return Optional.empty();
    }

    private Optional<AlertFieldStatus> validateSelectedProjectExists(Long providerConfigId, Collection<String> selectedProjects) {
        AlertPagedModel<ProviderProject> projectsByProviderConfigId = blackDuckDataAccessor.getProjectsByProviderConfigId(providerConfigId, AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, "");
        Set<String> projectNames = projectsByProviderConfigId.getModels()
            .stream()
            .map(ProviderProject::getName)
            .collect(Collectors.toSet());
        List<String> invalidProjects = selectedProjects.stream()
            .filter(Predicate.not(projectNames::contains))
            .collect(Collectors.toList());
        int totalPages = projectsByProviderConfigId.getTotalPages();
        int currentPage = projectsByProviderConfigId.getCurrentPage();
        while (!invalidProjects.isEmpty() && currentPage <= totalPages) {
            currentPage++;
            projectNames = blackDuckDataAccessor.getProjectsByProviderConfigId(providerConfigId, currentPage, AlertPagedModel.DEFAULT_PAGE_SIZE, "")
                .getModels()
                .stream()
                .map(ProviderProject::getName)
                .collect(Collectors.toSet());
            invalidProjects = invalidProjects.stream().filter(Predicate.not(projectNames::contains)).collect(Collectors.toList());
        }
        if (!invalidProjects.isEmpty()) {
            return Optional.of(AlertFieldStatus.warning(ProviderDescriptor.KEY_CONFIGURED_PROJECT, "The selected projects could not be found: " + String.join(", ", invalidProjects)));
        }
        return Optional.empty();
    }

    private <U extends AlertSerializableModel, T extends AlertPagedModel<U>> Optional<AlertFieldStatus> iteratePagesAndCheck(Function<Integer, T> getData, Function<Collection<U>, Boolean> checkValidity, AlertFieldStatus alertFieldStatus) {
        int currentPage = AlertPagedModel.DEFAULT_PAGE_NUMBER;
        T retrievedData = getData.apply(currentPage);
        int totalPages = retrievedData.getTotalPages();
        boolean invalid = false;
        while (!invalid && currentPage <= totalPages) {
            List<U> models = retrievedData.getModels();
            invalid = checkValidity.apply(models);
            currentPage++;
            retrievedData = getData.apply(currentPage);
        }
        if (!invalid) {
            return Optional.of(alertFieldStatus);
        }
        return Optional.empty();
    }

}
