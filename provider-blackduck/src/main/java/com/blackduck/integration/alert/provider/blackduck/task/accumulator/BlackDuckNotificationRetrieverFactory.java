/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.task.accumulator;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.log.Slf4jIntLogger;

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
            logger.warn("The Black Duck configuration '{}' could not be used to retrieve notifications", blackDuckProperties.getConfigName());
        }
        return Optional.empty();
    }

}
