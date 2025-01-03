/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.issue;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.event.AlertEventHandler;
import com.blackduck.integration.alert.common.channel.issuetracker.IssueTrackerCallbackEvent;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.dataservice.IssueService;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.google.gson.Gson;

@Component
public class BlackDuckIssueTrackerCallbackEventHandler implements AlertEventHandler<IssueTrackerCallbackEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    public BlackDuckIssueTrackerCallbackEventHandler(Gson gson, BlackDuckPropertiesFactory blackDuckPropertiesFactory, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor) {
        this.gson = gson;
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    @Override
    public void handle(IssueTrackerCallbackEvent event) {
        String eventId = event.getEventId();
        logger.debug("Handling issue-tracker callback event with id '{}'", eventId);

        IssueTrackerCallbackInfo callbackInfo = event.getCallbackInfo();
        Optional<BlackDuckServicesFactory> optionalBlackDuckServicesFactory = createBlackDuckProperties(callbackInfo.getProviderConfigId())
                                                                                  .flatMap(this::createBlackDuckServicesFactory);
        if (optionalBlackDuckServicesFactory.isPresent()) {
            BlackDuckServicesFactory blackDuckServicesFactory = optionalBlackDuckServicesFactory.get();
            BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
            IssueService blackDuckIssueService = blackDuckServicesFactory.createIssueService();

            BlackDuckProviderIssueHandler blackDuckProviderIssueHandler = new BlackDuckProviderIssueHandler(gson, blackDuckApiClient, blackDuckIssueService);
            BlackDuckProviderIssueModel issueModel = createBlackDuckIssueModel(event);
            createOrUpdateBlackDuckIssue(blackDuckProviderIssueHandler, issueModel, callbackInfo);
        }
        logger.debug("Finished handling issue-tracker callback event with id '{}'", eventId);
    }

    private Optional<BlackDuckProperties> createBlackDuckProperties(Long providerConfigId) {
        return configurationModelConfigurationAccessor.getConfigurationById(providerConfigId)
                   .map(blackDuckPropertiesFactory::createProperties);
    }

    private Optional<BlackDuckServicesFactory> createBlackDuckServicesFactory(BlackDuckProperties blackDuckProperties) {
        IntLogger intLogger = new Slf4jIntLogger(logger);
        try {
            BlackDuckHttpClient blackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClient(intLogger);
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, intLogger);
            return Optional.of(blackDuckServicesFactory);
        } catch (AlertException e) {
            logger.error("Failed to create a Black Duck http client", e);
            return Optional.empty();
        }
    }

    private void createOrUpdateBlackDuckIssue(BlackDuckProviderIssueHandler blackDuckProviderIssueHandler, BlackDuckProviderIssueModel issueModel, IssueTrackerCallbackInfo callbackInfo) {
        try {
            blackDuckProviderIssueHandler.createOrUpdateBlackDuckIssue(issueModel, callbackInfo.getCallbackUrl(), callbackInfo.getBlackDuckProjectVersionUrl());
        } catch (IntegrationException e) {
            logger.error("Failed to create or update Black Duck issue: {}", issueModel, e);
        }
    }

    private BlackDuckProviderIssueModel createBlackDuckIssueModel(IssueTrackerCallbackEvent event) {
        String blackDuckIssueStatus = mapOperationToAlertStatus(event.getOperation());
        return new BlackDuckProviderIssueModel(event.getIssueKey(), blackDuckIssueStatus, event.getIssueSummary(), event.getIssueUrl());
    }

    private String mapOperationToAlertStatus(IssueOperation issueOperation) {
        switch (issueOperation) {
            case OPEN:
            case UPDATE:
                return "Created by Alert";
            case RESOLVE:
                return "Resolved by Alert";
            default:
                return "Unknown";
        }
    }

}
