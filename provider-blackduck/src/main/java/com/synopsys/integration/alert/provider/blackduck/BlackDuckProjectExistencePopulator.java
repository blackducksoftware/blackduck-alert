/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.provider.ProviderProjectExistencePopulator;
import com.synopsys.integration.alert.api.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
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

    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final BlackDuckProvider blackDuckProvider;

    public BlackDuckProjectExistencePopulator(ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor, BlackDuckProvider blackDuckProvider) {
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.blackDuckProvider = blackDuckProvider;
    }

    @Override
    public void populateJobProviderProjects(Long providerGlobalConfigId, List<JobProviderProjectFieldModel> configuredProviderProjects) {
        configurationModelConfigurationAccessor.getConfigurationById(providerGlobalConfigId).ifPresent(providerGlobalConfig -> populateJobProviderProjects(providerGlobalConfig, configuredProviderProjects));
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
