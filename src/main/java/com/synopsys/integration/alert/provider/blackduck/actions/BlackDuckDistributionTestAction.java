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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class BlackDuckDistributionTestAction extends TestAction {
    private final ProviderDataAccessor blackDuckDataAccessor;

    @Autowired
    public BlackDuckDistributionTestAction(ProviderDataAccessor blackDuckDataAccessor) {
        this.blackDuckDataAccessor = blackDuckDataAccessor;
    }

    @Override
    public MessageResult testConfig(String configId, String description, FieldAccessor fieldAccessor) throws IntegrationException {
        Optional<String> optionalProviderConfigName = fieldAccessor.getString(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        if (optionalProviderConfigName.isPresent()) {
            Optional<String> projectNamePattern = fieldAccessor.getString(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN);
            if (projectNamePattern.isPresent()) {
                validatePatternMatchesProject(optionalProviderConfigName.get(), projectNamePattern.get());
            }
        } else {
            throw AlertFieldException.singleFieldError(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, "A provider configuration is required");
        }
        return new MessageResult("Successfully tested BlackDuck provider fields");
    }

    private void validatePatternMatchesProject(String providerConfigName, String projectNamePattern) throws AlertFieldException {
        List<ProviderProject> blackDuckProjects = blackDuckDataAccessor.getProjectsByProviderConfigName(providerConfigName);
        boolean noProjectsMatchPattern = blackDuckProjects.stream().noneMatch(databaseEntity -> databaseEntity.getName().matches(projectNamePattern));
        if (noProjectsMatchPattern && StringUtils.isNotBlank(projectNamePattern)) {
            throw AlertFieldException.singleFieldError(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN, "Does not match any of the Projects.");
        }
    }

}
