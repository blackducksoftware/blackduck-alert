/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreator;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;

@Component
public class BlackDuckMessageBomComponentDetailsCreatorFactory {
    private final BlackDuckComponentVulnerabilityDetailsCreator vulnerabilityDetailsCreator;
    private final BlackDuckComponentPolicyDetailsCreator policyDetailsCreator;

    @Autowired
    public BlackDuckMessageBomComponentDetailsCreatorFactory(
        BlackDuckComponentVulnerabilityDetailsCreator vulnerabilityDetailsCreator,
        BlackDuckComponentPolicyDetailsCreator policyDetailsCreator
    ) {
        this.vulnerabilityDetailsCreator = vulnerabilityDetailsCreator;
        this.policyDetailsCreator = policyDetailsCreator;
    }

    public BlackDuckMessageBomComponentDetailsCreator createBomComponentDetailsCreator(BlackDuckApiClient blackDuckApiClient) {
        return new BlackDuckMessageBomComponentDetailsCreator(blackDuckApiClient, vulnerabilityDetailsCreator, policyDetailsCreator);
    }

}
