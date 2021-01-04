/**
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.ProviderProjectExistencePopulator;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.rest.model.JobProviderProjectFieldModel;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.response.Response;

@Component
public class BlackDuckProjectExistencePopulator implements ProviderProjectExistencePopulator {
    public final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConfigurationAccessor configurationAccessor;
    private final BlackDuckProvider blackDuckProvider;

    public BlackDuckProjectExistencePopulator(ConfigurationAccessor configurationAccessor, BlackDuckProvider blackDuckProvider) {
        this.configurationAccessor = configurationAccessor;
        this.blackDuckProvider = blackDuckProvider;
    }

    @Override
    public void populateJobProviderProjects(Long providerGlobalConfigId, List<JobProviderProjectFieldModel> configuredProviderProjects) {
        configurationAccessor.getConfigurationById(providerGlobalConfigId).ifPresent(providerGlobalConfig -> populateJobProviderProjects(providerGlobalConfig, configuredProviderProjects));
    }

    private void populateJobProviderProjects(ConfigurationModel providerGlobalConfig, List<JobProviderProjectFieldModel> configuredProviderProjects) {
        BlackDuckApiClient blackDuckApiClient;
        try {
            blackDuckApiClient = createBlackDuckApiClient(providerGlobalConfig);
        } catch (AlertException e) {
            logger.debug("Failed to initialize BlackDuck services", e);
            return;
        }

        for (JobProviderProjectFieldModel project : configuredProviderProjects) {
            boolean exists = doesProjectExist(blackDuckApiClient, project);
            project.setMissing(!exists);
        }
    }

    private BlackDuckApiClient createBlackDuckApiClient(ConfigurationModel providerGlobalConfig) throws AlertException {
        StatefulProvider statefulProvider = blackDuckProvider.createStatefulProvider(providerGlobalConfig);
        BlackDuckProperties blackDuckProperties = (BlackDuckProperties) statefulProvider.getProperties();
        IntLogger intLogger = new Slf4jIntLogger(logger);
        BlackDuckHttpClient blackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClient(intLogger);
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, intLogger);
        return blackDuckServicesFactory.getBlackDuckApiClient();
    }

    private boolean doesProjectExist(BlackDuckApiClient blackDuckApiClient, JobProviderProjectFieldModel project) {
        try {
            HttpUrl projectHttpUrl = new HttpUrl(project.getHref());
            try (Response response = blackDuckApiClient.get(projectHttpUrl)) {
                return response.isStatusCodeSuccess();
            }
        } catch (Exception e) {
            logger.debug("Could not determine if the BlackDuck project '{}' existed", project.getName(), e);
        }
        return false;
    }

}
