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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class BlackDuckDistributionTestAction extends TestAction {
    private final ProviderDataAccessor blackDuckDataAccessor;

    @Autowired
    public BlackDuckDistributionTestAction(final ProviderDataAccessor blackDuckDataAccessor) {
        this.blackDuckDataAccessor = blackDuckDataAccessor;
    }

    @Override
    public String testConfig(final TestConfigModel testConfig) throws IntegrationException {
        final FieldAccessor fieldAccessor = testConfig.getFieldAccessor();
        final Optional<String> projectNamePattern = fieldAccessor.getString(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN);
        if (projectNamePattern.isPresent()) {
            validatePatternMatchesProject(projectNamePattern.get());
        }
        return "Successfully tested BlackDuck provider fields";
    }

    private void validatePatternMatchesProject(final String projectNamePattern) throws AlertFieldException {
        final List<ProviderProject> blackDuckProjects = blackDuckDataAccessor.findByProviderName(BlackDuckProvider.COMPONENT_NAME);
        final boolean noProjectsMatchPattern = blackDuckProjects.stream().noneMatch(databaseEntity -> databaseEntity.getName().matches(projectNamePattern));
        if (noProjectsMatchPattern && StringUtils.isNotBlank(projectNamePattern)) {
            final Map<String, String> fieldErrors = new HashMap<>();
            fieldErrors.put(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN, "Does not match any of the Projects.");
            throw new AlertFieldException(fieldErrors);
        }
    }
}
