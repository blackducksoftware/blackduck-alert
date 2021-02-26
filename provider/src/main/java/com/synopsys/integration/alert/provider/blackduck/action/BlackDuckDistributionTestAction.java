/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckApiTokenValidator;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class BlackDuckDistributionTestAction extends TestAction {
    private final ProviderDataAccessor blackDuckDataAccessor;
    private final BlackDuckProvider blackDuckProvider;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public BlackDuckDistributionTestAction(ProviderDataAccessor blackDuckDataAccessor, BlackDuckProvider blackDuckProvider, ConfigurationAccessor configurationAccessor) {
        this.blackDuckDataAccessor = blackDuckDataAccessor;
        this.blackDuckProvider = blackDuckProvider;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        ArrayList<AlertFieldStatus> fieldStatuses = new ArrayList<>();
        Optional<Long> optionalProviderConfigId = registeredFieldValues.getLong(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
        if (optionalProviderConfigId.isPresent()) {
            Long providerConfigId = optionalProviderConfigId.get();
            registeredFieldValues.getString(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN)
                .flatMap(projectNamePattern -> validatePatternMatchesProject(providerConfigId, projectNamePattern))
                .ifPresent(fieldStatuses::add);

            BlackDuckProperties blackDuckProperties = null;
            Optional<ConfigurationModel> providerConfigurationOptional = configurationAccessor.getConfigurationById(providerConfigId);
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
        List<ProviderProject> blackDuckProjects = blackDuckDataAccessor.getProjectsByProviderConfigId(providerConfigId);
        boolean noProjectsMatchPattern = blackDuckProjects.stream().noneMatch(databaseEntity -> databaseEntity.getName().matches(projectNamePattern));
        if (noProjectsMatchPattern && StringUtils.isNotBlank(projectNamePattern)) {
            return Optional.of(AlertFieldStatus.warning(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN, "Does not match any of the Projects."));
        }
        return Optional.empty();
    }

}
