/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.task.accumulator;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckNotificationRetrieverFactory {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Optional<BlackDuckNotificationRetriever> createBlackDuckNotificationRetriever(BlackDuckProperties blackDuckProperties) {
        Optional<BlackDuckHttpClient> optionalBlackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClientAndLogErrors(logger);
        if (optionalBlackDuckHttpClient.isPresent()) {
            BlackDuckHttpClient blackDuckHttpClient = optionalBlackDuckHttpClient.get();
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, new Slf4jIntLogger(logger));
            BlackDuckNotificationRetriever notificationRetriever = new BlackDuckNotificationRetriever(blackDuckServicesFactory.getBlackDuckApiClient(), blackDuckServicesFactory.getApiDiscovery());
            return Optional.of(notificationRetriever);
        } else {
            logger.warn("The BlackDuck configuration '{}' could not be used to retrieve notifications", blackDuckProperties.getConfigName());
        }
        return Optional.empty();
    }

}
