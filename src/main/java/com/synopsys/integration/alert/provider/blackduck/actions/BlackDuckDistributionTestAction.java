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

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
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
    private final Logger logger = LoggerFactory.getLogger(BlackDuckDistributionTestAction.class);

    @Autowired
    public BlackDuckDistributionTestAction(ProviderDataAccessor blackDuckDataAccessor, BlackDuckProvider blackDuckProvider, ConfigurationAccessor configurationAccessor) {
        this.blackDuckDataAccessor = blackDuckDataAccessor;
        this.blackDuckProvider = blackDuckProvider;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldAccessor registeredFieldValues) throws IntegrationException {
        Optional<String> optionalProviderConfigName = registeredFieldValues.getString(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        if (optionalProviderConfigName.isPresent()) {
            String providerConfigName = optionalProviderConfigName.get();
            Optional<String> projectNamePattern = registeredFieldValues.getString(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN);
            if (projectNamePattern.isPresent()) {
                validatePatternMatchesProject(providerConfigName, projectNamePattern.get());
            }

            Optional<BlackDuckProperties> optionalBlackDuckProperties = configurationAccessor.getProviderConfigurationByName(providerConfigName)
                                                                            .map(blackDuckProvider::createStatefulProvider)
                                                                            .map(statefulProvider -> (BlackDuckProperties) statefulProvider.getProperties());
            if (optionalBlackDuckProperties.isPresent()) {
                BlackDuckProperties blackDuckProperties = optionalBlackDuckProperties.get();

                BlackDuckApiTokenValidator blackDuckAPITokenValidator = new BlackDuckApiTokenValidator(blackDuckProperties);
                if (!blackDuckAPITokenValidator.isApiTokenValid()) {
                    throw AlertFieldException.singleFieldError(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, AlertFieldStatus.error("User permission failed, cannot read notifications from Black Duck."));
                }
            }
        } else {
            throw AlertFieldException.singleFieldError(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, AlertFieldStatus.error("A provider configuration is required"));
        }

        return new MessageResult("Successfully tested BlackDuck provider fields");
    }

    private void validatePatternMatchesProject(String providerConfigName, String projectNamePattern) throws AlertFieldException {
        List<ProviderProject> blackDuckProjects = blackDuckDataAccessor.getProjectsByProviderConfigName(providerConfigName);
        boolean noProjectsMatchPattern = blackDuckProjects.stream().noneMatch(databaseEntity -> databaseEntity.getName().matches(projectNamePattern));
        if (noProjectsMatchPattern && StringUtils.isNotBlank(projectNamePattern)) {
            throw AlertFieldException.singleFieldError(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN, AlertFieldStatus.error("Does not match any of the Projects."));
        }
    }

}
