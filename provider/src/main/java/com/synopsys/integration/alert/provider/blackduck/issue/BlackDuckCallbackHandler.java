/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.issue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.event.ProviderCallbackEvent;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.workflow.ProviderCallbackHandler;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckCallbackHandler extends ProviderCallbackHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;

    @Autowired
    public BlackDuckCallbackHandler(BlackDuckProvider blackDuckProvider, ConfigurationAccessor configurationAccessor, Gson gson) {
        super(blackDuckProvider, configurationAccessor, gson);
        this.gson = gson;
    }

    @Override
    protected void performProviderCallback(ProviderCallbackEvent event, StatefulProvider statefulProvider) throws IntegrationException {
        BlackDuckProperties blackDuckProperties = (BlackDuckProperties) statefulProvider.getProperties();
        IntLogger intLogger = new Slf4jIntLogger(logger);
        BlackDuckHttpClient blackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClient(intLogger);
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, intLogger);

        BlackDuckProviderIssueHandler blackDuckProviderIssueHandler = new BlackDuckProviderIssueHandler(gson, blackDuckServicesFactory.getBlackDuckApiClient(), blackDuckServicesFactory.createProjectService());

        BlackDuckProviderIssueModel issueModel = createBlackDuckIssueModel(event);
        blackDuckProviderIssueHandler.createOrUpdateBlackDuckIssue(event.getCallbackUrl(), issueModel, event.getProviderContentKey());
    }

    private BlackDuckProviderIssueModel createBlackDuckIssueModel(ProviderCallbackEvent event) {
        String blackDuckIssueStatus = mapOperationToAlertStatus(event.getOperation());
        return new BlackDuckProviderIssueModel(event.getIssueId(), blackDuckIssueStatus, event.getIssueSummary(), event.getIssueUrl().orElse(null));
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
