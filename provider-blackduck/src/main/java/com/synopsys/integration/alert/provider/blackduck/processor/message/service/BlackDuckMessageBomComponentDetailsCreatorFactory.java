/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreatorFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

@Component
public class BlackDuckMessageBomComponentDetailsCreatorFactory {
    private final BlackDuckComponentVulnerabilityDetailsCreator vulnerabilityDetailsCreator;
    private final BlackDuckComponentPolicyDetailsCreatorFactory blackDuckComponentPolicyDetailsCreatorFactory;

    @Autowired
    public BlackDuckMessageBomComponentDetailsCreatorFactory(
        BlackDuckComponentVulnerabilityDetailsCreator vulnerabilityDetailsCreator,
        BlackDuckComponentPolicyDetailsCreatorFactory blackDuckComponentPolicyDetailsCreatorFactory
    ) {
        this.vulnerabilityDetailsCreator = vulnerabilityDetailsCreator;
        this.blackDuckComponentPolicyDetailsCreatorFactory = blackDuckComponentPolicyDetailsCreatorFactory;
    }

    public BlackDuckMessageBomComponentDetailsCreator createBomComponentDetailsCreator(BlackDuckServicesFactory blackDuckServicesFactory) {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        BlackDuckComponentPolicyDetailsCreator policyDetailsCreator = blackDuckComponentPolicyDetailsCreatorFactory.createBlackDuckComponentPolicyDetailsCreator(blackDuckApiClient);
        return new BlackDuckMessageBomComponentDetailsCreator(blackDuckApiClient, vulnerabilityDetailsCreator, policyDetailsCreator);
    }
}
