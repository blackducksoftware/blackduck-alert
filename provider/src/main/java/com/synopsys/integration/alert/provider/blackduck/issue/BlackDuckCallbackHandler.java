/*
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
package com.synopsys.integration.alert.provider.blackduck.issue;

import java.util.Optional;

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
        Optional<BlackDuckServicesFactory> optionalBlackDuckServicesFactory = blackDuckProperties.createBlackDuckHttpClient(intLogger)
                                                                                  .map(httpClient -> blackDuckProperties.createBlackDuckServicesFactory(httpClient, intLogger));
        if (optionalBlackDuckServicesFactory.isPresent()) {
            BlackDuckServicesFactory blackDuckServicesFactory = optionalBlackDuckServicesFactory.get();
            BlackDuckProviderIssueHandler blackDuckProviderIssueHandler = new BlackDuckProviderIssueHandler(gson, blackDuckServicesFactory.getBlackDuckService(), blackDuckServicesFactory.getRequestFactory());

            BlackDuckProviderIssueModel issueModel = createBlackDuckIssueModel(event);
            blackDuckProviderIssueHandler.createOrUpdateBlackDuckIssue(event.getCallbackUrl(), issueModel);
        } else {
            logger.error("Cannot instantiate the BlackDuck services from a seemingly valid properties object. Config: id='{}', name='{}'", statefulProvider.getConfigId(), statefulProvider.getConfigName());
        }
    }

    private BlackDuckProviderIssueModel createBlackDuckIssueModel(ProviderCallbackEvent event) {
        String blackDuckIssueStatus = mapOperationToAlertStatus(event.getOperation());
        return new BlackDuckProviderIssueModel(event.getChannelDestinationId(), blackDuckIssueStatus, event.getChannelActionSummary(), event.getChannelDestinationLink().orElse(null));
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
