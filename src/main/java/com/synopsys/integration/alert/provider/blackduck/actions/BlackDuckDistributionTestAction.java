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
package com.synopsys.integration.alert.provider.blackduck.actions;

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
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.validators.BlackDuckApiTokenValidator;
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
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldAccessor registeredFieldValues) throws IntegrationException {
        ArrayList<AlertFieldStatus> fieldStatuses = new ArrayList<>();
        Optional<String> optionalProviderConfigName = registeredFieldValues.getString(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        if (optionalProviderConfigName.isPresent()) {
            String providerConfigName = optionalProviderConfigName.get();
            registeredFieldValues.getString(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN)
                .flatMap(projectNamePattern -> validatePatternMatchesProject(providerConfigName, projectNamePattern))
                .ifPresent(fieldStatuses::add);

            BlackDuckProperties blackDuckProperties = null;
            Optional<ConfigurationModel> providerConfigurationByNameOptional = configurationAccessor.getProviderConfigurationByName(providerConfigName);
            if (providerConfigurationByNameOptional.isPresent()) {
                ConfigurationModel providerConfiguration = providerConfigurationByNameOptional.get();
                StatefulProvider statefulProvider = blackDuckProvider.createStatefulProvider(providerConfiguration);
                blackDuckProperties = (BlackDuckProperties) statefulProvider.getProperties();

            }

            if (null != blackDuckProperties) {
                BlackDuckApiTokenValidator blackDuckAPITokenValidator = new BlackDuckApiTokenValidator(blackDuckProperties);
                if (!blackDuckAPITokenValidator.isApiTokenValid()) {
                    fieldStatuses.add(AlertFieldStatus.error(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, "User permission failed, cannot read notifications from Black Duck."));
                }
            }
        } else {
            fieldStatuses.add(AlertFieldStatus.error(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, "A provider configuration is required"));
        }

        if (MessageResult.hasFieldStatusBySeverity(fieldStatuses, FieldStatusSeverity.ERROR)) {
            return new MessageResult("There were errors with the BlackDuck provider fields", fieldStatuses);
        }
        return new MessageResult("Successfully tested BlackDuck provider fields", fieldStatuses);
    }

    private Optional<AlertFieldStatus> validatePatternMatchesProject(String providerConfigName, String projectNamePattern) {
        List<ProviderProject> blackDuckProjects = blackDuckDataAccessor.getProjectsByProviderConfigName(providerConfigName);
        boolean noProjectsMatchPattern = blackDuckProjects.stream().noneMatch(databaseEntity -> databaseEntity.getName().matches(projectNamePattern));
        if (noProjectsMatchPattern && StringUtils.isNotBlank(projectNamePattern)) {
            return Optional.of(AlertFieldStatus.warning(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN, "Does not match any of the Projects."));
        }
        return Optional.empty();
    }

}
